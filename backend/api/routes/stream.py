from fastapi import APIRouter, WebSocket, WebSocketDisconnect

from pipeline.preprocessor import preprocess
from pipeline.aggregator import aggregator  # assuming singleton or instance
from core.logger import logger

router = APIRouter()


@router.websocket("/stream")
async def stream(websocket: WebSocket):
    await websocket.accept()
    logger.info("WebSocket connected")

    try:
        while True:
            # 1. Receive raw JPEG bytes (from webcam_sender or ESP32)
            jpeg_bytes = await websocket.receive_bytes()

            # 2. Preprocess → FrameContext
            frame_context = preprocess(jpeg_bytes)

            # 3. Send to aggregator → final prediction JSON
            result = aggregator.process(frame_context)

            # 4. Send result back to client
            await websocket.send_json(result)

    except WebSocketDisconnect:
        logger.info("WebSocket disconnected")

    except Exception as e:
        logger.error(f"Stream error: {e}")
        try:
            await websocket.close()
        except:
            pass