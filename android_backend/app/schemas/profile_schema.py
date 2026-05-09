from pydantic import BaseModel, EmailStr
from typing import Literal, Optional


DeviceType = Literal["CAMERA", "OBD"]


class UserProfileResponse(BaseModel):
    id: int
    name: str
    email: EmailStr
    isPro: bool = False
    totalTrips: int = 0
    focusScore: int = 0
    daysActive: int = 0


class UserProfileUpdate(BaseModel):
    name: Optional[str] = None
    email: Optional[EmailStr] = None
    isPro: bool = False


class ProfileDeviceResponse(BaseModel):
    id: int
    device_id: str
    name: str
    type: DeviceType
    isConnected: bool
    paired: bool = True


class ProfileDeviceCreate(BaseModel):
    id: str
    name: Optional[str] = None
    type: DeviceType = "CAMERA"
    secret: str = "profile-device"
    isConnected: bool = False
