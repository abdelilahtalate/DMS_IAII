from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.db.session import get_db
from app.models.settings import UserSettings
from app.schemas.settings_schema import UserSettingsResponse, UserSettingsUpdate

router = APIRouter()

@router.get("/{user_id}", response_model=UserSettingsResponse)
def get_user_settings(user_id: int, db: Session = Depends(get_db)):
    settings = db.query(UserSettings).filter(UserSettings.user_id == user_id).first()
    if not settings:
        # Create default settings if they don't exist
        settings = UserSettings(user_id=user_id)
        db.add(settings)
        db.commit()
        db.refresh(settings)
    return settings

@router.post("/{user_id}", response_model=UserSettingsResponse)
def update_user_settings(user_id: int, data: UserSettingsUpdate, db: Session = Depends(get_db)):
    settings = db.query(UserSettings).filter(UserSettings.user_id == user_id).first()
    if not settings:
        settings = UserSettings(user_id=user_id)
        db.add(settings)
    
    for key, value in data.dict().items():
        setattr(settings, key, value)
    
    db.commit()
    db.refresh(settings)
    return settings
