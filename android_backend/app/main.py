from fastapi import FastAPI
from app.core.config import Config
from app.api.v1.api import api_router

app = FastAPI(
    title=Config.APP_NAME,
    debug=Config.DEBUG
)

app.include_router(api_router, prefix=Config.API_PREFIX)

@app.get("/")
def root():
    return {"status": "backend running of Android "}