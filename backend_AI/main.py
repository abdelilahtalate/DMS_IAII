from fastapi import FastAPI
import uvicorn
from api.routes.stream import router as stream_router

app = FastAPI(title="DMS API")

# Include our websocket stream route
app.include_router(stream_router)

@app.get("/")
def read_root():
    return {"message": "DMS API is running for AI !"}

if __name__ == "__main__":
    # Start the server
    uvicorn.run("main:app", host="0.0.0.0", port=8002, reload=True)