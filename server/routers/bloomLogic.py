from fastapi import APIRouter, UploadFile, File, Form
from pydantic import BaseModel
import os
from dotenv import load_dotenv
from google import genai
from google.genai import types
import httpx

router = APIRouter()

load_dotenv()

def readFile(file_content: bytes, user_question: str):
    print("hello")
    
    
@router.post("/uploadFile")
def uploadFile():
    print("hello")
    
client = genai.Client(api_key=os.getenv('GEMINI_API_KEY'))

response = client.models.generate_content(
    model="gemini-2.5-flash", contents="Why is the sky blue?"
)

print(response.text)


