from pydantic import BaseModel
from typing import List


class DayScore(BaseModel):
    day: str
    score: int


class InsightStats(BaseModel):
    averageScore: int
    scoreChange: int
    bestDay: str
    bestScore: int
    totalTrips: int
    eventCount: int


class InsightOverview(BaseModel):
    stats: InsightStats
    dayScores: List[DayScore]
