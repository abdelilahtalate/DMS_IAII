from fastapi import APIRouter
from app.services.auth_service import AuthService
from app.schemas.auth_schema import SignupRequest, LoginRequest

router = APIRouter()

auth_service = AuthService()


@router.post("/signup")
def signup(data: SignupRequest):
    return auth_service.signup(
        full_name=data.full_name,
        email=data.email,
        password=data.password,
        gender=data.gender,
        age=data.age
    )


@router.post("/login")
def login(data: LoginRequest):
    return auth_service.login(data.email, data.password)