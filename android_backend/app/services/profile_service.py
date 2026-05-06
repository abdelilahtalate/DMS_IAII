from sqlalchemy.orm import Session

from app.models.device import Device
from app.models.session import DrivingSession
from app.models.user import User
from app.schemas.profile_schema import ProfileDeviceCreate, UserProfileUpdate
from app.services.device_store import devices_db


class ProfileService:

    def get_profile(self, db: Session, user_id: int):
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            return {"error": "User not found"}

        sessions = db.query(DrivingSession).filter(DrivingSession.user_id == user_id).all()
        active_days = {
            session.start_time.date()
            for session in sessions
            if session.start_time is not None
        }

        return {
            "name": user.full_name,
            "email": user.email,
            "isPro": False,
            "totalTrips": len(sessions),
            "focusScore": 0,
            "daysActive": len(active_days),
        }

    def update_profile(self, db: Session, user_id: int, data: UserProfileUpdate):
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            return {"error": "User not found"}

        if data.name is not None:
            user.full_name = data.name
        if data.email is not None:
            user.email = data.email

        db.commit()
        db.refresh(user)
        return self.get_profile(db, user_id)

    def list_devices(self, db: Session, user_id: int):
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            return {"error": "User not found"}

        devices = db.query(Device).all()
        return [self._device_response(device, user.email) for device in devices]

    def add_device(self, db: Session, user_id: int, data: ProfileDeviceCreate):
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            return {"error": "User not found"}

        device = db.query(Device).filter(Device.device_id == data.id).first()
        if device is None:
            device = Device(
                device_id=data.id,
                secret=data.secret,
                paired=data.isConnected,
            )
            db.add(device)
        else:
            device.secret = data.secret
            device.paired = data.isConnected

        db.commit()
        db.refresh(device)

        if data.id in devices_db:
            devices_db[data.id]["owner"] = user.email
            devices_db[data.id]["status"] = "online" if data.isConnected else "offline"

        return self._device_response(device, user.email, data.name, data.type)

    def _device_response(
        self,
        device: Device,
        user_email: str,
        name: str = None,
        device_type: str = "CAMERA",
    ):
        store_device = devices_db.get(device.device_id)
        store_owner = store_device.get("owner") if store_device else None
        store_status = store_device.get("status") if store_device else None

        return {
            "id": device.device_id,
            "name": name or device.device_id,
            "type": device_type,
            "isConnected": bool(device.paired) or (
                store_owner == user_email and store_status == "online"
            ),
        }
