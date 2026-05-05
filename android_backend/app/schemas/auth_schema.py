from pydantic import BaseModel, EmailStr
from typing import Optional


class SignupRequest(BaseModel):
    full_name: str
    email: EmailStr
    password: str
    gender: str  # "male" | "female" 
    age: Optional[int] = None


class LoginRequest(BaseModel):
    email: EmailStr
    password: str