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

class LocationResponse(BaseModel):
    id: int
    latitude: float
    longitude: float
    speed: float
    timestamp: datetime

class AlertResponse(BaseModel):
    id: int
    alert_type: str
    latitude: float
    longitude: float
    timestamp: datetime

class SessionSummaryResponse(BaseModel):
    id: int
    user_id: int
    device_id: int
    status: str
    start_time: datetime
    end_time: Optional[datetime] = None
    alert_count: int
    location_count: int

class SessionDetailResponse(BaseModel):
    id: int
    user_id: int
    device_id: int
    status: str
    start_time: datetime
    end_time: Optional[datetime] = None
    locations: List[LocationResponse]
    alerts: List[AlertResponse]
