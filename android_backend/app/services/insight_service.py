from collections import defaultdict

from sqlalchemy.orm import Session

from app.models.session import DrivingSession
from app.models.user import User


class InsightService:

    def get_day_scores(self, db: Session, user_id: int):
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            return {"error": "User not found"}

        sessions = (
            db.query(DrivingSession)
            .filter(DrivingSession.user_id == user_id)
            .order_by(DrivingSession.start_time.asc())
            .all()
        )

        day_stats = defaultdict(lambda: {"trips": 0, "events": 0})
        for session in sessions:
            if session.start_time is None:
                continue

            day = session.start_time.strftime("%a")
            day_stats[day]["trips"] += 1
            day_stats[day]["events"] += len(session.alerts)

        return [
            {
                "day": day,
                "score": self._score_from_events(values["events"], values["trips"]),
            }
            for day, values in day_stats.items()
        ]

    def get_stats(self, db: Session, user_id: int):
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            return {"error": "User not found"}

        sessions = (
            db.query(DrivingSession)
            .filter(DrivingSession.user_id == user_id)
            .order_by(DrivingSession.start_time.asc())
            .all()
        )
        day_scores = self.get_day_scores(db, user_id)
        if isinstance(day_scores, dict) and "error" in day_scores:
            return day_scores

        total_trips = len(sessions)
        event_count = sum(len(session.alerts) for session in sessions)
        average_score = self._average_score(day_scores)
        best = max(day_scores, key=lambda item: item["score"], default={"day": "", "score": 0})

        midpoint = max(total_trips // 2, 1)
        previous_sessions = sessions[:midpoint]
        recent_sessions = sessions[midpoint:]
        previous_score = self._score_for_sessions(previous_sessions)
        recent_score = self._score_for_sessions(recent_sessions) if recent_sessions else previous_score

        return {
            "averageScore": average_score,
            "scoreChange": recent_score - previous_score,
            "bestDay": best["day"],
            "bestScore": best["score"],
            "totalTrips": total_trips,
            "eventCount": event_count,
        }

    def get_overview(self, db: Session, user_id: int):
        stats = self.get_stats(db, user_id)
        if isinstance(stats, dict) and "error" in stats:
            return stats

        day_scores = self.get_day_scores(db, user_id)
        if isinstance(day_scores, dict) and "error" in day_scores:
            return day_scores

        return {
            "stats": stats,
            "dayScores": day_scores,
        }

    def _score_for_sessions(self, sessions):
        if not sessions:
            return 0

        event_count = sum(len(session.alerts) for session in sessions)
        return self._score_from_events(event_count, len(sessions))

    def _score_from_events(self, event_count: int, trip_count: int):
        if trip_count <= 0:
            return 0

        return max(0, min(100, 100 - int((event_count / trip_count) * 15)))

    def _average_score(self, day_scores):
        if not day_scores:
            return 0

        total = sum(item["score"] for item in day_scores)
        return int(total / len(day_scores))
