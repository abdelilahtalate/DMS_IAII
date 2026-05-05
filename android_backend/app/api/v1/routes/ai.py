from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from app.integrations.iot_client import IoTClient
import asyncio

router = APIRouter()
iot = IoTClient()

# In-memory storage for the latest AI results
latest_scores = {}

@router.get("/")
def ai_root():
    return {"module": "ai", "status": "orchestrator running"}

@router.post("/update")
async def update_scores(data: dict):
    """Internal endpoint: The IOT simulation posts AI results here."""
    global latest_scores
    latest_scores = data
    return {"status": "success"}

@router.post("/start")
def start_ai_analysis():
    """Explicitly start the IOT device camera and analysis."""
    return iot.start_analysis()

@router.post("/stop")
def stop_ai_analysis():
    """Explicitly stop the IOT device and release hardware."""
    return iot.stop_analysis()

@router.websocket("/stream_analytics")
async def ai_data_stream_ws(websocket: WebSocket):
    """Real-time data stream for the Android app. Does not control hardware."""
    await websocket.accept()
    print("[Backend] Android app connected to data stream.")
    
    try:
        last_sent = None
        while True:
            # Forward latest scores if they have updated
            if latest_scores != last_sent:
                await websocket.send_json(latest_scores)
                last_sent = latest_scores.copy() if latest_scores else None
            
            await asyncio.sleep(0.05)
            
    except WebSocketDisconnect:
        print("[Backend] Android app disconnected from stream.")