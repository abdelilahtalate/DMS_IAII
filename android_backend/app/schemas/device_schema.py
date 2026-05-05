from pydantic import BaseModel


class PairDeviceRequest(BaseModel):
    device_id: str
    secret: str
    user_email: str