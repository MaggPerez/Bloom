from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from pydantic import BaseModel
import os
from dotenv import load_dotenv
from google import genai
import io
import csv
from typing import Optional

try:
    from PyPDF2 import PdfReader
except Exception:
    PdfReader = None

router = APIRouter()

load_dotenv()


def readFile(file_content: bytes, filename: str) -> str:
    """Extract text from PDF or CSV bytes and return a text representation.

    - For PDF: requires `PyPDF2`; extracts page text.
    - For CSV: decodes and returns header + first N rows as CSV text.
    """
    filename = (filename or "").lower()
    if filename.endswith(".pdf"):
        if PdfReader is None:
            raise RuntimeError("PyPDF2 is required to parse PDF files. Install it with `pip install PyPDF2`.")
        reader = PdfReader(io.BytesIO(file_content))
        texts = []
        for page in reader.pages:
            try:
                texts.append(page.extract_text() or "")
            except Exception:
                # best-effort continue
                continue
        return "\n\n".join(texts)
    elif filename.endswith(".csv"):
        decoded = file_content.decode(errors="replace")
        # Read CSV and limit to first 50 rows to avoid huge payloads
        stream = io.StringIO(decoded)
        reader = csv.reader(stream)
        rows = []
        try:
            for i, row in enumerate(reader):
                rows.append(row)
                if i >= 49:
                    break
        except Exception:
            # fallback: return raw decoded text
            return decoded
        # format rows as simple pipe-separated lines for readability
        out_lines = []
        for r in rows:
            out_lines.append(
                ", ".join([c if c is not None else "" for c in r])
            )
        return "\n".join(out_lines)
    else:
        raise ValueError("Unsupported file type. Only PDF and CSV are supported.")


@router.post("/processFile")
async def uploadFile(file: UploadFile = File(...), user_question: Optional[str] = Form(None)):
    """Accept a PDF or CSV file and a user question, then use Gemini to extract/summarize
    and answer the question. Makes two Gemini calls:
      1) Summarize/extract the financial content of the file
      2) Answer the user's question based on that summary

    The system prompt instructs Gemini to ONLY process financial data and to refuse non-financial files.
    """
    raw = await file.read()
    filename = getattr(file, "filename", "unknown")

    try:
        extracted_text = readFile(raw, filename)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))

    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY is not configured")

    client = genai.Client(api_key=api_key)

    # System prompt enforcing financial-only behavior
    system_instruction = (
        "SYSTEM: You are a Financial Data Assistant. You must only read and analyze financial"
        " data (e.g., revenues, expenses, balance sheets, transactions, P&L, cash flow, account" 
        " statements, financial metrics). If the provided file does not contain financial data"
        " or the content is not financial in nature, respond exactly: 'I cannot help with the"
        " provided file because it does not contain financial data.' Do not attempt to answer"
        " non-financial questions or hallucinate financial details. Keep answers concise and"
        " focused on the financial content present."
    )

    # Limit the amount of file text we send in a single request to avoid very large payloads
    max_chars = 30000
    send_text = extracted_text[:max_chars]

    # First Gemini call: extract & summarize
    extract_prompt = (
        f"{system_instruction}\n\nHere is the file content (truncated to {max_chars} chars):\n{send_text}\n\n"
        "Please extract and summarize the financial information present in the file. If there is"
        " no financial information, follow the system instruction and say you cannot help."
    )

    try:
        first_resp = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=extract_prompt,
        )
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Gemini extraction call failed: {e}")

    first_text = getattr(first_resp, "text", str(first_resp))

    # If Gemini already refused due to non-financial content, return that message
    if "I cannot help with the provided file" in first_text:
        return {"message": first_text}

    # Second Gemini call: answer user's question based on extracted summary
    if not user_question:
        user_question = "Please provide a concise summary of the financial data."

    answer_prompt = (
        f"{system_instruction}\n\nI have already extracted and summarized the file as follows:\n{first_text}\n\n"
        f"Now answer the user's question using only the information in the summary above."
        f" User question: {user_question}"
    )

    try:
        second_resp = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=answer_prompt,
        )
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Gemini answer call failed: {e}")

    second_text = getattr(second_resp, "text", str(second_resp))

    return {"message": second_text}
    
    
    
class ChatRequest(BaseModel):
    message: str


@router.post("/chat")
async def chat(request: ChatRequest):
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY is not configured")

    client = genai.Client(api_key=api_key)

    system_instruction = (
        "SYSTEM: You are a Financial Data Assistant. You assist with financial"
        " data (e.g., revenues, expenses, balance sheets, transactions, P&L, cash flow, account"
        " statements, financial metrics). If the user asks about non-financial topics,"
        " respond exactly: 'I cannot help with that request as I only handle financial data.'"
        " Do not attempt to answer non-financial questions. Keep answers concise and"
        " focused on financial concepts."
    )

    full_prompt = f"{system_instruction}\n\nUser: {request.message}"

    try:
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=full_prompt
        )
        return {"message": response.text}
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Gemini chat call failed: {e}")
    
    
    
    
# @router.get("/geminiResponse")
# async def geminiResponse():
#     client = genai.Client(api_key=os.getenv('GEMINI_API_KEY'))

#     response = client.models.generate_content(
#         model="gemini-2.5-flash", contents="Why is the sky blue?"
#     )

#     print(response.text)
#     return {"message": response.text}


    
# client = genai.Client(api_key=os.getenv('GEMINI_API_KEY'))

# response = client.models.generate_content(
#     model="gemini-2.5-flash", contents="Why is the sky blue?"
# )

# print(response.text)


