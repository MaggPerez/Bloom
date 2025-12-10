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


@router.post("/insights")
async def generate_insights(request: ChatRequest):
    """Generate AI-powered financial insights from a user's financial summary.

    Takes a summary of the user's financial data (income, expenses, spending patterns)
    and returns personalized insights and recommendations.
    """
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY is not configured")

    client = genai.Client(api_key=api_key)

    system_instruction = (
        "SYSTEM: You are a Financial Insights Advisor for college students and low-income individuals."
        " Your goal is to provide personalized, actionable insights based on the user's spending patterns."
        " Analyze the financial summary provided and give 3-4 specific insights or recommendations."
        " Focus on: spending patterns, budget optimization, savings opportunities, and financial health."
        " Be encouraging and supportive while being honest about areas for improvement."
        " Keep your response concise and actionable. Format with bullet points or numbered lists."
    )

    full_prompt = f"{system_instruction}\n\n{request.message}"

    try:
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=full_prompt
        )
        return {"message": response.text}
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"Gemini insights call failed: {e}")


@router.post("/healthScore")
async def healthScore(request: ChatRequest):
    """Evaluate the user's financial health score based on their financial summary.

    Provides a score out of 100 along with brief reasoning and 4-5 actionable recommendations.
    Returns structured JSON with score, breakdown, and recommendations.
    """
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY is not configured")

    client = genai.Client(api_key=api_key)

    system_instruction = (
        "SYSTEM: You are a Financial Health Evaluator. You will receive raw financial data "
        "(budget, transactions, expenses, savings). You must analyze this data and CALCULATE "
        "a financial health score (0-100) and a score breakdown.\n\n"
        "SCORING CRITERIA (Total 100):\n"
        "1. Budget Adherence (Max 40): Do they stay within budget?\n"
        "2. Savings Rate (Max 30): Are they saving money? (Income vs Expense)\n"
        "3. Spending Consistency (Max 20): Is spending predictable?\n"
        "4. Emergency Fund (Max 10): Do they have savings buffer?\n\n"
        "You MUST respond in EXACTLY this format:\n\n"
        "SCORE: [number]\n"
        "BREAKDOWN:\n"
        "Budget Adherence: [number]\n"
        "Savings Rate: [number]\n"
        "Spending Consistency: [number]\n"
        "Emergency Fund: [number]\n"
        "RECOMMENDATIONS:\n"
        "1. [First recommendation]\n"
        "2. [Second recommendation]\n"
        "3. [Third recommendation]\n"
        "4. [Fourth recommendation]\n\n"
        "Be strict with the format. No markdown."
    )

    full_prompt = f"{system_instruction}\n\nUSER DATA:\n{request.message}"

    try:
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=full_prompt
        )
        response_text = response.text
        print("Health Score Response:", response_text)

        # Parse the response
        score = 0
        budget_score = 0
        savings_score = 0
        consistency_score = 0
        emergency_score = 0
        recommendations = ""

        lines = response_text.strip().split('\n')
        current_section = None

        for line in lines:
            line = line.strip()
            if not line:
                continue

            if line.startswith("SCORE:"):
                try:
                    score = int(''.join(filter(str.isdigit, line.split(":")[1])))
                except: pass
            elif line.startswith("BREAKDOWN:"):
                current_section = "BREAKDOWN"
            elif line.startswith("RECOMMENDATIONS:"):
                current_section = "RECOMMENDATIONS"
                continue # Skip the header line

            if current_section == "BREAKDOWN":
                if "Budget Adherence:" in line:
                    try: budget_score = int(''.join(filter(str.isdigit, line.split(":")[1])))
                    except: pass
                elif "Savings Rate:" in line:
                    try: savings_score = int(''.join(filter(str.isdigit, line.split(":")[1])))
                    except: pass
                elif "Spending Consistency:" in line:
                    try: consistency_score = int(''.join(filter(str.isdigit, line.split(":")[1])))
                    except: pass
                elif "Emergency Fund:" in line:
                    try: emergency_score = int(''.join(filter(str.isdigit, line.split(":")[1])))
                    except: pass
            
            elif current_section == "RECOMMENDATIONS":
                recommendations += line + "\n"

        return {
            "score": score,
            "budgetAdherenceScore": budget_score,
            "savingsRateScore": savings_score,
            "spendingConsistencyScore": consistency_score,
            "emergencyFundScore": emergency_score,
            "recommendations": recommendations.strip(),
            "message": response_text
        }
    except Exception as e:
        print(f"Error in healthScore: {e}")
        raise HTTPException(status_code=502, detail=f"Gemini health score call failed: {e}")


@router.post("/importCSV")
async def importCSV(file: UploadFile = File(...)):
    """Import and validate CSV file containing transaction data.
    
    Validates that the CSV has required columns: Transaction Name, Amount, 
    Transaction Type, Date, Description, and Payment method.
    Uses AI to verify data quality and filter out irrelevant rows.
    Returns an array of validated transaction objects.
    """
    # Read the uploaded file
    raw = await file.read()
    filename = getattr(file, "filename", "unknown")
    
    # Only accept CSV files
    if not filename.lower().endswith(".csv"):
        raise HTTPException(status_code=400, detail="Only CSV files are supported for import")
    
    # Parse CSV content
    try:
        decoded = raw.decode('utf-8', errors='replace')
        stream = io.StringIO(decoded)
        reader = csv.DictReader(stream)
        
        # Get the fieldnames (column headers)
        fieldnames = reader.fieldnames
        if not fieldnames:
            raise HTTPException(status_code=400, detail="CSV file is empty or has no headers")
        
        # Read all rows
        rows = list(reader)
        if not rows:
            raise HTTPException(status_code=400, detail="CSV file contains no data rows")
            
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Failed to parse CSV file: {str(e)}")
    
    # Get Gemini API key
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        raise HTTPException(status_code=500, detail="GEMINI_API_KEY is not configured")
    
    client = genai.Client(api_key=api_key)
    
    # Prepare CSV data for AI analysis
    # Limit to first 100 rows to avoid token limits
    sample_rows = rows[:100]
    csv_preview = f"Column Headers: {', '.join(fieldnames)}\n\n"
    csv_preview += "Sample Rows (first 10):\n"
    for i, row in enumerate(sample_rows[:10]):
        csv_preview += f"Row {i+1}: {row}\n"
    
    # System prompt for AI validation
    system_instruction = (
        "SYSTEM: You are a Transaction Data Validator. Your job is to analyze CSV files "
        "containing financial transaction data and validate their structure and content.\n\n"
        "REQUIRED COLUMNS (must be present, case-insensitive):\n"
        "1. Transaction Name (or similar: name, transaction, description)\n"
        "2. Amount (or similar: price, cost, value)\n"
        "3. Transaction Type (or similar: type, category) - should contain 'Expense' or 'Income'\n"
        "4. Date (or similar: transaction date, date)\n"
        "5. Description (or similar: notes, memo, details)\n"
        "6. Payment Method (or similar: payment, method, payment type)\n\n"
        "VALIDATION RULES:\n"
        "1. Check if the CSV has columns that match the required columns (fuzzy matching allowed)\n"
        "2. Verify that the data is about financial transactions (expenses, income, purchases, etc.)\n"
        "3. Check if rows contain relevant transaction data, not random/irrelevant information\n"
        "4. Identify which columns map to the required fields\n\n"
        "RESPOND IN THIS EXACT FORMAT:\n"
        "STATUS: [VALID or INVALID or IRRELEVANT]\n"
        "REASON: [Brief explanation]\n"
        "COLUMN_MAPPING:\n"
        "Transaction Name: [actual column name or MISSING]\n"
        "Amount: [actual column name or MISSING]\n"
        "Transaction Type: [actual column name or MISSING]\n"
        "Date: [actual column name or MISSING]\n"
        "Description: [actual column name or MISSING]\n"
        "Payment Method: [actual column name or MISSING]\n\n"
        "Use STATUS: VALID only if all required columns are present (with fuzzy matching).\n"
        "Use STATUS: INVALID if required columns are missing.\n"
        "Use STATUS: IRRELEVANT if the data is not about financial transactions."
    )
    
    validation_prompt = f"{system_instruction}\n\nAnalyze this CSV:\n\n{csv_preview}"
    
    # Call AI to validate CSV structure
    try:
        validation_response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=validation_prompt
        )
        validation_text = validation_response.text
        print("Validation Response:", validation_text)
        
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"AI validation failed: {str(e)}")
    
    # Parse validation response
    lines = validation_text.strip().split('\n')
    status = None
    reason = None
    column_mapping = {}
    
    current_section = None
    for line in lines:
        line = line.strip()
        if not line:
            continue
            
        if line.startswith("STATUS:"):
            status = line.split(":", 1)[1].strip()
        elif line.startswith("REASON:"):
            reason = line.split(":", 1)[1].strip()
        elif line.startswith("COLUMN_MAPPING:"):
            current_section = "MAPPING"
            continue
            
        if current_section == "MAPPING" and ":" in line:
            parts = line.split(":", 1)
            key = parts[0].strip()
            value = parts[1].strip()
            if value != "MISSING":
                column_mapping[key] = value
    
    # Check validation status
    if status == "IRRELEVANT":
        raise HTTPException(
            status_code=400, 
            detail="The CSV file contains irrelevant or non-transaction data. Please upload a file with financial transaction data."
        )
    
    if status == "INVALID" or not column_mapping:
        required_cols = [
            "Transaction Name", "Amount", "Transaction Type", 
            "Date", "Description", "Payment Method"
        ]
        missing = [col for col in required_cols if col not in column_mapping]
        raise HTTPException(
            status_code=400,
            detail=f"CSV file must have the following columns: {', '.join(required_cols)}. Missing: {', '.join(missing) if missing else 'columns not found'}"
        )
    
    # Process and filter rows
    valid_transactions = []
    skipped_count = 0
    
    for row in rows:
        # Extract values using column mapping
        try:
            transaction_name = row.get(column_mapping.get("Transaction Name", ""), "").strip()
            amount = row.get(column_mapping.get("Amount", ""), "").strip()
            transaction_type = row.get(column_mapping.get("Transaction Type", ""), "").strip()
            date = row.get(column_mapping.get("Date", ""), "").strip()
            description = row.get(column_mapping.get("Description", ""), "").strip()
            payment_method = row.get(column_mapping.get("Payment Method", ""), "").strip()
            
            # Validate required fields are not empty
            if not transaction_name or not amount or not transaction_type or not date:
                skipped_count += 1
                continue
            
            # Build transaction object
            transaction = {
                "transactionName": transaction_name,
                "amount": amount,
                "transactionType": transaction_type,
                "date": date,
                "description": description,
                "paymentMethod": payment_method
            }
            
            valid_transactions.append(transaction)
            
        except Exception as e:
            # Skip rows that cause errors
            skipped_count += 1
            continue
    
    if not valid_transactions:
        raise HTTPException(
            status_code=400,
            detail="No valid transactions found in CSV. Ensure rows have Transaction Name, Amount, Transaction Type, and Date filled."
        )
    
    return {
        "success": True,
        "message": f"Successfully imported {len(valid_transactions)} transactions. Skipped {skipped_count} invalid rows.",
        "transactions": valid_transactions,
        "totalRows": len(rows),
        "validRows": len(valid_transactions),
        "skippedRows": skipped_count
    }


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


