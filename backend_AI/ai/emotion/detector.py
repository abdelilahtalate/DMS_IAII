from ultralytics import YOLO
from collections import Counter
import numpy as np
from typing import Optional, Dict, Any, List

from ai.BaseTemporalDetector import BaseTemporalDetector


class EmotionDetector(BaseTemporalDetector):
    """
    Temporal Emotion Detector using YOLOv8-cls
    
    Classifies face crops and applies temporal smoothing
    over sliding window for stable predictions.
    """

    def __init__(
        self,
        model_path: str,
        buffer_size: int = 15,
        stride: int = 3,
        confidence_threshold: float = 0.4,
        smoothing: str = "probability",  # "probability" | "majority"
    ):
        """
        Args:
            model_path: Path to YOLOv8-cls model (e.g., "emotion_cls.pt")
            buffer_size: Number of frames in sliding window
            stride: Compute every N frames
            confidence_threshold: Min confidence to consider prediction
            smoothing: Temporal smoothing method
        """
        super().__init__(buffer_size=buffer_size, stride=stride)

        # =========================
        # 🧠 MODEL
        # =========================
        self.model = YOLO(model_path)
        self.class_names = self.model.names  # e.g., {0: 'happy', 1: 'sad', ...}

        # =========================
        # ⚙️ CONFIG
        # =========================
        self.confidence_threshold = confidence_threshold
        self.smoothing = smoothing

    # =========================================================
    # 🔍 SINGLE FRAME CLASSIFICATION
    # =========================================================
    def _classify_crop(self, face_crop: np.ndarray) -> Optional[Dict]:
        """Run YOLOv8-cls on a single face crop."""
        
        if face_crop is None:
            return None

        results = self.model(face_crop, verbose=False)

        if not results:
            return None

        result = results[0]
        
        # Extract top-1 prediction
        top1_idx = result.probs.top1
        top1_conf = result.probs.top1conf.item()
        
        # Extract full probability distribution
        probs_array = result.probs.data.cpu().numpy()
        probabilities = {
            self.class_names[i]: float(probs_array[i])
            for i in range(len(probs_array))
        }

        return {
            "emotion": self.class_names[top1_idx],
            "confidence": top1_conf,
            "probabilities": probabilities,
        }

    # =========================================================
    # 🧠 COMPUTE (called by push_frame when conditions met)
    # =========================================================
    def compute(self) -> Optional[Dict[str, Any]]:
        """
        Process sliding window buffer.
        
        Returns:
            Dict with emotion, confidence, and metadata
            None if no valid faces in buffer
        """

        # -----------------------------------------
        # 1. Collect valid face crops from buffer
        # -----------------------------------------
        valid_crops = []
        for ctx in self.buffer:
            if ctx.face_detected and ctx.face_crop is not None:
                valid_crops.append(ctx.face_crop)

        if len(valid_crops) == 0:
            return None

        # -----------------------------------------
        # 2. Classify each crop
        # -----------------------------------------
        predictions = []
        for crop in valid_crops:
            pred = self._classify_crop(crop)
            if pred and pred["confidence"] >= self.confidence_threshold:
                predictions.append(pred)

        if len(predictions) == 0:
            return None

        # -----------------------------------------
        # 3. Apply temporal smoothing
        # -----------------------------------------
        if self.smoothing == "majority":
            return self._smooth_majority(predictions)
        else:
            return self._smooth_probability(predictions)

    # =========================================================
    # 📊 SMOOTHING METHODS
    # =========================================================
    def _smooth_majority(self, predictions: List[Dict]) -> Dict:
        """Majority voting - most frequent emotion wins."""
        
        emotions = [p["emotion"] for p in predictions]
        counter = Counter(emotions)
        
        winner_emotion, vote_count = counter.most_common(1)[0]
        
        # Average confidence for winner
        winner_confs = [p["confidence"] for p in predictions if p["emotion"] == winner_emotion]
        avg_conf = float(np.mean(winner_confs))

        return {
            "emotion": winner_emotion,
            "confidence": avg_conf,
            "method": "majority_voting",
            "votes": dict(counter),
            "valid_frames": len(predictions),
            "total_frames": len(self.buffer),
        }

    def _smooth_probability(self, predictions: List[Dict]) -> Dict:
        """Average probability distributions - smoother output."""
        
        # Get all emotion classes
        all_emotions = list(predictions[0]["probabilities"].keys())
        
        # Average probabilities
        avg_probs = {emo: 0.0 for emo in all_emotions}
        for pred in predictions:
            for emo, prob in pred["probabilities"].items():
                avg_probs[emo] += prob
        
        for emo in avg_probs:
            avg_probs[emo] /= len(predictions)

        # Find top emotion
        top_emotion = max(avg_probs, key=avg_probs.get)
        top_confidence = avg_probs[top_emotion]

        return {
            "emotion": top_emotion,
            "confidence": float(top_confidence),
            "probabilities": avg_probs,
            "method": "probability_averaging",
            "valid_frames": len(predictions),
            "total_frames": len(self.buffer),
        }