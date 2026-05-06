from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from app.db.session import get_db
from app.schemas.profile_schema import (
    ProfileDeviceCreate,
    ProfileDeviceResponse,
    UserProfileResponse,
    UserProfileUpdate,
)
from app.services.profile_service import ProfileService


router = APIRouter()
profile_service = ProfileService()


@router.get("/{user_id}", response_model=UserProfileResponse)
def get_profile(user_id: int, db: Session = Depends(get_db)):
    result = profile_service.get_profile(db, user_id)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result


@router.post("/{user_id}/sync", response_model=UserProfileResponse)
def sync_profile(user_id: int, db: Session = Depends(get_db)):
    result = profile_service.get_profile(db, user_id)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result


@router.post("/{user_id}", response_model=UserProfileResponse)
def update_profile(
    user_id: int,
    data: UserProfileUpdate,
    db: Session = Depends(get_db),
):
    result = profile_service.update_profile(db, user_id, data)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result


@router.get("/{user_id}/devices", response_model=List[ProfileDeviceResponse])
def get_profile_devices(user_id: int, db: Session = Depends(get_db)):
    result = profile_service.list_devices(db, user_id)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result


@router.post("/{user_id}/devices", response_model=ProfileDeviceResponse)
def add_profile_device(
    user_id: int,
    data: ProfileDeviceCreate,
    db: Session = Depends(get_db),
):
    result = profile_service.add_device(db, user_id, data)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result
