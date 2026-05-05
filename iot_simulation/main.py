from fastapi import FastAPI
from stream import router
from camera import start_camera

app = FastAPI()

   # 👈 THIS IS THE KEY FIX

app.include_router(router)

@app.get("/")
def root():
    return {"status": "backend running of iot simulation"}
