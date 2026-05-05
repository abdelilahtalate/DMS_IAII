from fastapi import APIRouter

router = APIRouter()

@router.get("/")
def alerts_root():
    return {"module": "alerts"}