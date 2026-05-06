from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.services.drive_service import DriveService
from app.schemas.session_schema import SessionStart, LocationCreate, AlertCreate

router = APIRouter()
drive_service = DriveService()

@router.post("/start")
def start_session(data: SessionStart, db: Session = Depends(get_db)):
    result = drive_service.start_session(db, data)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result

@router.post("/stop/{session_id}")
def stop_session(session_id: int, db: Session = Depends(get_db)):
    result = drive_service.stop_session(db, session_id)
    if "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result

@router.post("/location")
def log_location(data: LocationCreate, db: Session = Depends(get_db)):
    return drive_service.log_location(db, data)

@router.post("/alert")
def log_alert(data: AlertCreate, db: Session = Depends(get_db)):
    return drive_service.log_alert(db, data)