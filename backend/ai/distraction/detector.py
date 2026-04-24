# backend/ai/distraction/detector.py

from ai.base import BaseTemporalDetector
from pipeline.frame_context import FrameContext
from .head_pose import compute_head_pose_score
from .gaze_direction import compute_gaze_score


class DistractionDetector(BaseTemporalDetector):

    name = "distraction"
    required_frames = 20   # ~2 secondes à 10 fps
    min_frames      = 7    # scorer après ~0.7 sec

    def load_model(self) -> None:
        # Pas de modèle lourd pour l'instant
        # Ajoute ici le chargement de poids si besoin
        pass

    def predict(self, frames: list[FrameContext]) -> dict:
        """
        frames = liste de jusqu'à 20 FrameContext (oldest → newest)
        Analyse la tendance de distraction sur la fenêtre temporelle.
        """

        # Extraire les données valides sur toute la fenêtre
        head_pose_data = [
            f.landmarks["head_pose"]
            for f in frames
            if f.landmarks["head_pose"] is not None
        ]
        gaze_data = [
            f.landmarks["gaze"]
            for f in frames
            if f.landmarks["gaze"] is not None
        ]

        # Pas assez de données → pas de visage détecté
        if len(head_pose_data) < 3 and len(gaze_data) < 3:
            return {
                "score":      0.0,
                "confidence": 0.0,
                "label":      "no_face",
                "details":    {"frames_used": len(frames)},
            }

        # Score head pose sur la fenêtre temporelle
        head_score, head_details = compute_head_pose_score(head_pose_data)

        # Score gaze sur la fenêtre temporelle
        gaze_score, gaze_details = compute_gaze_score(gaze_data)

        # Pondération : head_pose 60%, gaze 40%
        final_score = (head_score * 0.6) + (gaze_score * 0.4)
        final_score = round(min(max(final_score, 0.0), 1.0), 4)

        # Confidence basée sur la couverture des données
        total_possible = len(frames) * 2  # head + gaze par frame
        total_valid    = len(head_pose_data) + len(gaze_data)
        data_coverage  = total_valid / total_possible if total_possible > 0 else 0.0
        confidence     = round(min(0.5 + data_coverage * 0.45, 0.95), 4)

        # Label
        if final_score >= 0.75:
            label = "highly_distracted"
        elif final_score >= 0.45:
            label = "slightly_distracted"
        else:
            label = "attentive"

        return {
            "score":      final_score,
            "confidence": confidence,
            "label":      label,
            "details": {
                "head_yaw":         head_details.get("avg_yaw"),
                "head_pitch":       head_details.get("avg_pitch"),
                "head_score":       round(head_score, 4),
                "gaze":             gaze_details.get("dominant_direction"),
                "gaze_score":       round(gaze_score, 4),
                "frames_used":      len(frames),
                "head_frames":      len(head_pose_data),
                "gaze_frames":      len(gaze_data),
            },
        }