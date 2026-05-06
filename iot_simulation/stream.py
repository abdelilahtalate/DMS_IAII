from fastapi import APIRouter, Response, Depends
from db import SessionLocal, Device
import cv2
import camera
from fastapi.responses import StreamingResponse
import time
import asyncio
import websockets
import json
import requests
from camera import start_camera, stop_camera

router = APIRouter()

# Keep track of the background analysis task
analysis_task = None

async def stream_to_backend():
    """Background task to send frames to the AI microservice via WebSocket with auto-reconnect."""
    uri = "ws://localhost:8002/stream"
    
    while True:
        print(f"[Simulation] Connecting to AI Microservice at {uri}...")
        try:
            async with websockets.connect(uri) as websocket:
                print("[Simulation] Connected to Backend! Sending frames for analysis...")
                while True:
                    if camera.latest_frame is not None:
                        # Encode frame as JPEG
                        success, buffer = cv2.imencode('.jpg', camera.latest_frame)
                        if success:
                            jpeg_bytes = buffer.tobytes()
                            # Send bytes over websocket
                            await websocket.send(jpeg_bytes)
                            
                            # Receive the analysis JSON from the backend
                            # Receive the analysis JSON from the backend
                            result_str = await websocket.recv()
                            result = json.loads(result_str)
                            print(f"[Simulation] AI Result: {result}")
                            
                            # Push results to Main Backend (8000) for the Android WebSocket bridge
                            try:
                                requests.post("http://127.0.0.1:8000/api/v1/ai/update", json=result, timeout=0.05)
                            except:
                                pass
                    
                    # Control the frame rate (e.g., ~20 FPS)
                    await asyncio.sleep(0.03)
        except asyncio.CancelledError:
            print("[Simulation] Analysis task stopped.")
            break
        except websockets.exceptions.ConnectionClosedOK:
            print("[Simulation] Connection closed normally by server. Reconnecting in 1s...")
            await asyncio.sleep(1)
        except Exception as e:
            print(f"[Simulation] WebSocket Error: {e}. Retrying in 5s...")
            await asyncio.sleep(5)

def gen_frames():
    """Video streaming generator function."""
    while True:
        if camera.latest_frame is not None:
            # Encode the frame as JPEG
            success, buffer = cv2.imencode('.jpg', camera.latest_frame)
            if success:
                frame = buffer.tobytes()
                # Yield the frame in MJPEG format
                yield (b'--frame\r\n'
                       b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')
        
        # Limit frame rate to ~30 FPS to save bandwidth/CPU
        time.sleep(0.03)

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.get("/device/{device_id}")
def get_device(device_id: str, db = Depends(get_db)):
    device = db.query(Device).filter(Device.device_id == device_id).first()

    if not device:
        return {"error": "device not found"}

    return {
        "device_id": device.device_id,
        "secret": device.secret,
        "paired": device.paired
    }

@router.post("/pair")
def pair_device(device_id: str, secret: str, db = Depends(get_db)):
    device = db.query(Device).filter(Device.device_id == device_id).first()

    if not device:
        return {"error": "device not found"}

    if device.secret != secret:
        return {"error": "invalid secret"}

    device.paired = True
    db.commit()

    return {
        "message": "device paired successfully",
        "device_id": device_id
    }

@router.get("/preview")
def preview():
    start_camera()
    """Returns a continuous MJPEG stream."""
    return StreamingResponse(gen_frames(), 
                            media_type="multipart/x-mixed-replace; boundary=frame")

@router.get("/stop-preview")
def stop_preview():
    """Stops the camera hardware."""
    stop_camera()
    return {"status": "preview stopped"}

@router.get("/start-analysis")
async def start_analysis():
    start_camera()
    """Starts the background task that sends frames to the backend."""
    global analysis_task
    if analysis_task and not analysis_task.done():
        return {"status": "analysis already running"}
    
    analysis_task = asyncio.create_task(stream_to_backend())
    return {"status": "analysis started"}

@router.get("/stop-analysis")
async def stop_analysis():
    """Stops the background analysis task."""
    stop_camera()
    global analysis_task
    if analysis_task:
        analysis_task.cancel()
        analysis_task = None
        return {"status": "analysis stopped"}
    return {"status": "no analysis running"}