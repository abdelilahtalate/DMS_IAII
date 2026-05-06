from fastapi import APIRouter

from app.api.v1.routes import auth, devices, pairing, ai, alerts, sessions, profile, insights

api_router = APIRouter()

api_router.include_router(auth.router, prefix="/auth", tags=["Auth"])
api_router.include_router(devices.router, prefix="/devices", tags=["Devices"])
api_router.include_router(pairing.router, prefix="/pairing", tags=["Pairing"])
api_router.include_router(ai.router, prefix="/ai", tags=["AI"])
api_router.include_router(alerts.router, prefix="/alerts", tags=["Alerts"])
api_router.include_router(sessions.router, prefix="/sessions", tags=["Sessions"])
api_router.include_router(profile.router, prefix="/profile", tags=["Profile"])
api_router.include_router(insights.router, prefix="/insights", tags=["Insights"])
