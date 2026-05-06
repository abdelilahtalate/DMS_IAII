from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List

from app.db.session import get_db
from app.schemas.insight_schema import DayScore, InsightOverview, InsightStats
from app.services.insight_service import InsightService


router = APIRouter()
insight_service = InsightService()


@router.get("/{user_id}/stats", response_model=InsightStats)
def get_insight_stats(user_id: int, db: Session = Depends(get_db)):
    result = insight_service.get_stats(db, user_id)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result


@router.get("/{user_id}/day-scores", response_model=List[DayScore])
def get_day_scores(user_id: int, db: Session = Depends(get_db)):
    result = insight_service.get_day_scores(db, user_id)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result


@router.get("/{user_id}/overview", response_model=InsightOverview)
def get_insight_overview(user_id: int, db: Session = Depends(get_db)):
    result = insight_service.get_overview(db, user_id)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result


@router.post("/{user_id}/sync", response_model=InsightOverview)
def sync_insights(user_id: int, db: Session = Depends(get_db)):
    result = insight_service.get_overview(db, user_id)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result
