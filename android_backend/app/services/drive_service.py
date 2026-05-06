from sqlalchemy.orm import Session
from app.models.session import DrivingSession
from app.models.location import LocationPoint
from app.models.alert import Alert
from app.models.device import Device
from app.schemas.session_schema import SessionStart, LocationCreate, AlertCreate
from datetime import datetime

class DriveService:

    def start_session(self, db: Session, data: SessionStart):
        # Find device primary key from device_id string
        device = db.query(Device).filter(Device.device_id == data.device_id).first()
        if not device:
            return {"error": "Device not found"}
            
        new_session = DrivingSession(
            user_id=data.user_id,
            device_id=device.id,
            status="active"
        )
        db.add(new_session)
        db.commit()
        db.refresh(new_session)
        return new_session

    def stop_session(self, db: Session, session_id: int):
        session = db.query(DrivingSession).filter(DrivingSession.id == session_id).first()
        if session:
            session.end_time = datetime.now()
            session.status = "completed"
            db.commit()
            return {"message": "Session stopped"}
        return {"error": "Session not found"}

    def log_location(self, db: Session, data: LocationCreate):
        new_point = LocationPoint(
            session_id=data.session_id,
            latitude=data.latitude,
            longitude=data.longitude,
            speed=data.speed
        )
        db.add(new_point)
        db.commit()
        return {"message": "Location logged"}

    def log_alert(self, db: Session, data: AlertCreate):
        new_alert = Alert(
            session_id=data.session_id,
            alert_type=data.alert_type,
            latitude=data.latitude,
            longitude=data.longitude
        )
        db.add(new_alert)
        db.commit()
        return {"message": "Alert logged"}
