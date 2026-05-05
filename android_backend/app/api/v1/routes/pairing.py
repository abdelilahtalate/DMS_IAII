from fastapi import APIRouter
from app.integrations.iot_client import IoTClient
from app.schemas.pairing import PairRequest

router = APIRouter()

iot = IoTClient()

@router.post("/pair")
def pair_device(request: PairRequest):

    result = iot.verify_device(request.device_id, request.secret)

    if "error" in result:
        return result

    return {
        "message": "Device paired successfully",
        "device": {
            "device_id": request.device_id,
            "preview_stream": iot.get_preview()
        }
    }