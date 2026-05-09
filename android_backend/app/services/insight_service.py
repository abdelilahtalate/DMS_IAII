from collections import defaultdict
from sqlalchemy.orm import Session
from app.models.session import DrivingSession
from app.models.user import User
from datetime import datetime

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

        day_stats = defaultdict(lambda: {"sessions": 0, "total_focus": 0})
        for session in sessions:
            if session.start_time is None:
                continue

            day = session.start_time.strftime("%a")
            day_stats[day]["sessions"] += 1
            day_stats[day]["total_focus"] += (session.average_focus or 100)

        return [
            {
                "day": day,
                "score": int(values["total_focus"] / values["sessions"]) if values["sessions"] > 0 else 100,
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
        total_distance_meters = sum(session.total_distance or 0 for session in sessions)
        total_distance_km = round(total_distance_meters / 1000.0, 2)
        
        total_duration_min = 0
        for session in sessions:
            if session.start_time and session.end_time:
                duration = session.end_time - session.start_time
                total_duration_min += int(duration.total_seconds() / 60)
        
        average_score = self._average_score(day_scores)
        best = max(day_scores, key=lambda item: item["score"], default={"day": "", "score": 0})

        midpoint = max(total_trips // 2, 1)
        previous_sessions = sessions[:midpoint]
        recent_sessions = sessions[midpoint:]
        
        previous_score = self._calculate_average_from_sessions(previous_sessions)
        recent_score = self._calculate_average_from_sessions(recent_sessions) if recent_sessions else previous_score

        return {
            "averageScore": average_score,
            "scoreChange": recent_score - previous_score,
            "bestDay": best["day"],
            "bestScore": best["score"],
            "totalTrips": total_trips,
            "eventCount": event_count,
            "totalDistance": total_distance_km,
            "totalDuration": total_duration_min,
        }

    def get_overview(self, db: Session, user_id: int):
        stats = self.get_stats(db, user_id)
        if isinstance(stats, dict) and "error" in stats:
            return stats

        day_scores = self.get_day_scores(db, user_id)
        if isinstance(day_scores, dict) and "error" in day_scores:
            return day_scores

        heatmap = self.get_heatmap_data(db, user_id)

        return {
            "stats": stats,
            "dayScores": day_scores,
            "heatmapData": heatmap
        }

    def get_heatmap_data(self, db: Session, user_id: int):
        """Returns a 7x8 matrix of risk levels (0-5) based on alert density."""
        sessions = (
            db.query(DrivingSession)
            .filter(DrivingSession.user_id == user_id)
            .all()
        )
        
        # 7 days (Mon-Sun), 8 blocks (3h each)
        matrix = [[0 for _ in range(8)] for _ in range(7)]
        counts = [[0 for _ in range(8)] for _ in range(7)]
        
        for session in sessions:
            for alert in session.alerts:
                if alert.timestamp:
                    # Monday is 0, Sunday is 6
                    day = alert.timestamp.weekday()
                    # 0-23 -> 0-7
                    hour_block = alert.timestamp.hour // 3
                    
                    if 0 <= day < 7 and 0 <= hour_block < 8:
                        counts[day][hour_block] += 1

        # Map counts to risk levels 0-5
        for d in range(7):
            for h in range(8):
                c = counts[d][h]
                if c == 0: matrix[d][h] = 0
                elif c < 2: matrix[d][h] = 1
                elif c < 4: matrix[d][h] = 2
                elif c < 6: matrix[d][h] = 3
                elif c < 10: matrix[d][h] = 4
                else: matrix[d][h] = 5
        
        return matrix

    def _calculate_average_from_sessions(self, sessions):
        if not sessions:
            return 100
        total = sum(session.average_focus or 100 for session in sessions)
        return int(total / len(sessions))

    def _average_score(self, day_scores):
        if not day_scores:
            return 100

        total = sum(item["score"] for item in day_scores)
        return int(total / len(day_scores))
