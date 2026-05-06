import time
from typing import Dict, Any
from ai.fatigue.detector import FatigueDetector
from ai.distraction.detector import DistractDetector
# from ai.gaze.detector import GazeDetector
# from ai.emotion.detector import EmotionDetector
# from ai.phone_usage.detector import PhoneDetector


class Aggregator:

    def __init__(self):

        # =========================
        # 🧠 ALL MODELS (TEMPORAL)
        # =========================
        self.models = {
             "fatigue": FatigueDetector(),
             "distraction": DistractDetector(),
            # "emotion": EmotionDetector(buffer_size=15, stride=2,model_path= "C:\\Users\\crasy\\OneDrive\\Bureau\\s2 ia2i\\mobile\\DMS\\backend\\ai\\phone_usage\\yolov8n.pt"),
            # "phone": PhoneDetector(),
        }

        # =========================
        # 💾 LAST KNOWN RESULTS
        # =========================
        self.last_scores: Dict[str, Any] = {}

    # =========================================================
    # 🎯 FOCUS CALCULATION
    # =========================================================
    def _calculate_focus(self, scores: Dict[str, Any]) -> float:
        """
        Calculates a global focus score (0-100) based on individual detector scores.
        Uses specific coefficients to weigh the importance of each metric.
        """
        coefficients = {
            "fatigue": 0.4,
            "distraction": 0.6,
            "phone": 0.8,
            "emotion": 0.2
        }
        
        weighted_score_sum = 0.0
        active_coeff_sum = 0.0
        
        for name, data in scores.items():
            if name in coefficients and data and "score" in data:
                score = data["score"]
                weight = coefficients[name]
                
                weighted_score_sum += score * weight
                active_coeff_sum += weight
        
        if active_coeff_sum == 0:
            return 100.0 # Default to fully focused if no data
            
        # Focus is the inverse of the weighted disturbance
        avg_disturbance = weighted_score_sum / active_coeff_sum
        focus_value = (1.0 - avg_disturbance) * 100
        
        return round(max(0.0, min(100.0, focus_value)), 2)

    # =========================================================
    # 🚀 MAIN ENTRY POINT
    # =========================================================
    def process(self, frame_context):
        frame_id = frame_context.frame_id
        timestamp = frame_context.timestamp
        
        scores = {}
        
        # =========================
        # 🟠 PUSH FRAME TO ALL MODELS
        # =========================
        for name, model in self.models.items():

            try:
                result = model.push_frame(frame_context)  # heta nta2akad waxe paralle calucle or not 

                # update only if model produced new output
                if result is not None:
                    self.last_scores[name] = result

            except Exception as e:
                print(f"[Aggregator Error] {name}: {e}")

            # always return last known value (stability)
            scores[name] = self.last_scores.get(name)
            
        # =========================
        # 🎯 CALCULATE GLOBAL FOCUS
        # =========================
        # If no face is detected, we might want to lower the focus score or keep it at 0
        focus = self._calculate_focus(scores) if frame_context.face_detected else 0.0
        
        # =========================
        # 📦 FINAL JSON OUTPUT
        # =========================
        return {
            "frame_id": frame_id,
            "timestamp": timestamp,
            "face_detected": frame_context.face_detected,
            "status": "active" if frame_context.face_detected else "no face detected",
            "focus": focus,
            "scores": scores
        }


# =========================
# SINGLETON INSTANCE
# =========================
aggregator = Aggregator()
