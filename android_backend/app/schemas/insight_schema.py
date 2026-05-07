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
    totalDistance: float = 0.0
    totalDuration: int = 0  # in minutes


class InsightOverview(BaseModel):
    stats: InsightStats
    dayScores: List[DayScore]
    heatmapData: List[List[int]]
