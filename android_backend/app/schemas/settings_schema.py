from pydantic import BaseModel
from typing import Optional

class UserSettingsBase(BaseModel):
    fatigue_sensitivity: str = "HIGH"
    phone_sensitivity: str = "MEDIUM"
    distraction_sensitivity: str = "HIGH"
    voice_alerts: bool = True
    haptic_feedback: bool = True
    critical_only: bool = False
    preferred_voice: str = "DEFAULT"

class UserSettingsUpdate(UserSettingsBase):
    pass

class UserSettingsResponse(UserSettingsBase):
    user_id: int

    class Config:
        from_attributes = True
