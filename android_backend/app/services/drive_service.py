from sqlalchemy.orm import Session
from app.models.session import DrivingSession
from app.models.location import LocationPoint
from app.models.alert import Alert
from app.models.device import Device
from app.schemas.session_schema import SessionStart, LocationCreate, AlertCreate, DistanceUpdate, ScoreUpdate, LocationBatch
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
            status="active",
            total_distance=0,
            average_focus=100
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

    def update_distance(self, db: Session, data: DistanceUpdate):
        session = db.query(DrivingSession).filter(DrivingSession.id == data.session_id).first()
        if session:
            session.total_distance = data.distance_meters
            db.commit()
            return {"message": "Distance updated", "total_distance": session.total_distance}
        return {"error": "Session not found"}

    def update_score(self, db: Session, data: ScoreUpdate):
        session = db.query(DrivingSession).filter(DrivingSession.id == data.session_id).first()
        if session:
            session.average_focus = data.average_focus
            db.commit()
            return {"message": "Score updated", "average_focus": session.average_focus}
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

    def log_locations(self, db: Session, data: LocationBatch):
        points = [
            LocationPoint(
                session_id=data.session_id,
                latitude=loc.latitude,
                longitude=loc.longitude,
                speed=loc.speed
            )
            for loc in data.locations
        ]
        db.bulk_save_objects(points)
        db.commit()
        return {"message": f"{len(points)} locations logged"}

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

    def list_sessions(self, db: Session, user_id: int = None):
        query = db.query(DrivingSession)
        if user_id is not None:
            query = query.filter(DrivingSession.user_id == user_id)

        sessions = query.order_by(DrivingSession.start_time.desc()).all()
        return [
            {
                "id": session.id,
                "user_id": session.user_id,
                "device_id": session.device_id,
                "status": session.status,
                "start_time": session.start_time,
                "end_time": session.end_time,
                "total_distance": session.total_distance,
                "average_focus": session.average_focus,
                "alert_count": len(session.alerts),
                "location_count": len(session.location_points),
            }
            for session in sessions
        ]

    def get_session_detail(self, db: Session, session_id: int):
        session = db.query(DrivingSession).filter(DrivingSession.id == session_id).first()
        if not session:
            return {"error": "Session not found"}

        return {
            "id": session.id,
            "user_id": session.user_id,
            "device_id": session.device_id,
            "status": session.status,
            "start_time": session.start_time,
            "end_time": session.end_time,
            "total_distance": session.total_distance,
            "average_focus": session.average_focus,
            "locations": [
                {
                    "id": point.id,
                    "latitude": point.latitude,
                    "longitude": point.longitude,
                    "speed": point.speed,
                    "timestamp": point.timestamp,
                }
                for point in session.location_points
            ],
            "alerts": [
                {
                    "id": alert.id,
                    "alert_type": alert.alert_type,
                    "latitude": alert.latitude,
                    "longitude": alert.longitude,
                    "timestamp": alert.timestamp,
                }
                for alert in session.alerts
            ],
        }

    def get_session_locations(self, db: Session, session_id: int):
        session = db.query(DrivingSession).filter(DrivingSession.id == session_id).first()
        if not session:
            return {"error": "Session not found"}

        return [
            {
                "id": point.id,
                "latitude": point.latitude,
                "longitude": point.longitude,
                "speed": point.speed,
                "timestamp": point.timestamp,
            }
            for point in session.location_points
        ]

    def get_session_alerts(self, db: Session, session_id: int):
        session = db.query(DrivingSession).filter(DrivingSession.id == session_id).first()
        if not session:
            return {"error": "Session not found"}

        return [
            {
                "id": alert.id,
                "alert_type": alert.alert_type,
                "latitude": alert.latitude,
                "longitude": alert.longitude,
                "timestamp": alert.timestamp,
            }
            for alert in session.alerts
        ]
