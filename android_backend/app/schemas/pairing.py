from pydantic import BaseModel

class PairRequest(BaseModel):
    device_id: str
    secret: str
    user_email: str