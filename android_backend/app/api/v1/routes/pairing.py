from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.integrations.iot_client import IoTClient
from app.schemas.pairing import PairRequest
from app.services.profile_service import ProfileService
from app.schemas.profile_schema import ProfileDeviceCreate
from app.models.user import User

router = APIRouter()
iot = IoTClient()
profile_service = ProfileService()

@router.post("/pair")
def pair_device(request: PairRequest, db: Session = Depends(get_db)):
    # 1. Verify with IoT Simulation
    result = iot.verify_device(request.device_id, request.secret)

    if "error" in result:
        return result

    # 2. Get User ID from email
    user = db.query(User).filter(User.email == request.user_email).first()
    if not user:
        return {"error": "User not found"}

    # 3. Register in database
    profile_service.add_device(
        db=db,
        user_id=user.id,
        data=ProfileDeviceCreate(
            id=request.device_id,
            name=request.device_id, # Default name
            secret=request.secret,
            isConnected=True,
            type="CAMERA"
        )
    )

    return {
        "message": "Device paired successfully",
        "device": {
            "device_id": request.device_id,
            "preview_stream": iot.get_preview()
        }
    }