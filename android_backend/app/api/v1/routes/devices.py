from fastapi import APIRouter
from app.integrations.iot_client import IoTClient

router = APIRouter()

iot = IoTClient()


@router.get("/iot/test")
def test_iot():
    return {
        "device_info": iot.get_device_info(),
        "status": iot.get_device_status()
    }