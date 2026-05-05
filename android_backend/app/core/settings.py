from pydantic import BaseModel
from dotenv import load_dotenv
import os

load_dotenv()

class Settings(BaseModel):
    APP_NAME: str = os.getenv("APP_NAME", "DriveMind AI")
    DEBUG: bool = os.getenv("DEBUG", "False") == "True"
    API_PREFIX: str = os.getenv("API_PREFIX", "/api/v1")

settings = Settings()