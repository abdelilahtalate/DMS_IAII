from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime

class SessionStart(BaseModel):
    device_id: str # device_id from simulation/db
    user_id: int

class SessionResponse(BaseModel):
    session_id: int
    status: str
    start_time: datetime

class LocationCreate(BaseModel):
    session_id: int
    latitude: float
    longitude: float
    speed: float

class AlertCreate(BaseModel):
    session_id: int
    alert_type: str
    latitude: float
    longitude: float
