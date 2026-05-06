from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List, Optional
from app.db.session import get_db
from app.services.drive_service import DriveService
from app.schemas.session_schema import (
    AlertCreate,
    AlertResponse,
    LocationCreate,
    LocationResponse,
    SessionDetailResponse,
    SessionStart,
    SessionSummaryResponse,
)

router = APIRouter()
drive_service = DriveService()

@router.post("/start")
def start_session(data: SessionStart, db: Session = Depends(get_db)):
    print(f"DEBUG: start_session call - user_id={data.user_id}, device_id='{data.device_id}'")
    result = drive_service.start_session(db, data)
    print(f"DEBUG: result={result}")
    if isinstance(result, dict) and "error" in result:
        print(f"DEBUG: Returning 404 because of error: {result['error']}")
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

@router.get("/", response_model=List[SessionSummaryResponse])
def list_sessions(user_id: Optional[int] = None, db: Session = Depends(get_db)):
    return drive_service.list_sessions(db, user_id)

@router.get("/{session_id}", response_model=SessionDetailResponse)
def get_session_detail(session_id: int, db: Session = Depends(get_db)):
    result = drive_service.get_session_detail(db, session_id)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result

@router.get("/{session_id}/locations", response_model=List[LocationResponse])
def get_session_locations(session_id: int, db: Session = Depends(get_db)):
    result = drive_service.get_session_locations(db, session_id)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result

@router.get("/{session_id}/alerts", response_model=List[AlertResponse])
def get_session_alerts(session_id: int, db: Session = Depends(get_db)):
    result = drive_service.get_session_alerts(db, session_id)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result
