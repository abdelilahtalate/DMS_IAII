# backend/ai/distraction/detector.py

import numpy as np
from typing import List, Dict, Any, Optional
from ai.base import BaseTemporalDetector
from pipeline.frame_context import FrameContext


class DistractionDetector(BaseTemporalDetector):
    """
    Détecteur de distraction basé sur:
    - Head pose (yaw, pitch, roll)
    - Gaze direction
    """
    
    def __init__(self, buffer_size: int = 20, stride: int = 3):
        super().__init__(buffer_size=buffer_size, stride=stride)
        
        # Seuils de distraction
        self.yaw_threshold = 30  # degrés
        self.pitch_threshold = 20  # degrés
        self.gaze_threshold = 0.3  # score
        
    def compute(self) -> Dict[str, Any]:
        """
        Calcule le score de distraction à partir du buffer.
        Cette méthode est appelée automatiquement par BaseTemporalDetector
        quand le buffer est plein et que le stride est atteint.
        """
        if len(self.buffer) == 0:
            return {
                "score": 0.0,
                "confidence": 0.0,
                "label": "no_data",
                "details": {"frames_used": 0}
            }
        
        scores = []
        

        for ctx in self.buffer:
            score = self._compute_single_frame_score(ctx)
            
            scores.append(score)
        
        # Moyenne des scores
        avg_score = np.mean(scores)
        
        # Déterminer le label
        if avg_score < 0.3:
            label = "attentive"
            confidence = 1.0 - avg_score
        elif avg_score < 0.7:
            label = "moderately_distracted"
            confidence = avg_score
        else:
            label = "highly_distracted"
            confidence = avg_score
        
        # Calculer le pourcentage de frames avec distraction
        distracted_frames = sum(1 for s in scores if s > 0.5)
        distraction_ratio = distracted_frames / len(scores) if scores else 0
        
        return {
            "score": float(avg_score),
            "confidence": float(confidence),
            "label": label,
            "details": {
                "frames_used": len(self.buffer),
                "distraction_ratio": round(distraction_ratio, 2),
                "avg_head_pose_score": float(np.mean([self._get_head_score(ctx) for ctx in self.buffer])),
                "avg_gaze_score": float(np.mean([self._get_gaze_score(ctx) for ctx in self.buffer])),
            }
        }
    
    def _compute_single_frame_score(self, ctx: FrameContext) -> float:
        """
        Calcule le score pour une frame unique.
        """
        head_score = self._get_head_score(ctx)
        gaze_score = self._get_gaze_score(ctx)
        
        # Score combiné (70% head, 30% gaze)
        total_score = (head_score * 0.7) + (gaze_score * 0.3)
        
        return min(1.0, total_score)
    
    def _get_head_score(self, ctx: FrameContext) -> float:
        """Calcule le score basé sur la position de la tête."""
        # Récupérer les landmarks
        landmarks = getattr(ctx, 'landmarks', {})
        head_pose = landmarks.get("head_pose")
        
        if not head_pose:
            return 0.0
        
        yaw = abs(head_pose.get("yaw", 0))
        pitch = abs(head_pose.get("pitch", 0))
        
        # Score proportionnel aux angles
        yaw_score = min(1.0, yaw / self.yaw_threshold)
        pitch_score = min(1.0, pitch / self.pitch_threshold)
        
        return max(yaw_score, pitch_score)
    
    def _get_gaze_score(self, ctx: FrameContext) -> float:
        """Calcule le score basé sur la direction du regard."""
        landmarks = getattr(ctx, 'landmarks', {})
        gaze = landmarks.get("gaze")
        
        if not gaze:
            return 0.0
        
        direction = gaze.get("direction", "center")
        
        if direction == "center":
            return 0.0
        elif direction in ["left", "right"]:
            return 0.8
        else:  # up, down
            return 0.6
    
    def predict(self, frames: List[FrameContext]) -> Dict[str, Any]:
        """
        Prédit la distraction sur une séquence de frames.
        """
        # Vider le buffer
        self.buffer.clear()
        self.step = 0
        
        # Ajouter chaque frame
        for frame in frames:
            result = self.push_frame(frame)
        
        # Retourner le dernier résultat ou calculer un nouveau
        if self.last_output is not None:
            return self.last_output
        else:
            return self.compute()