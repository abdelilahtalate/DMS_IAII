import time
from typing import Dict, Any

from ai.fatigue.detector import FatigueDetector
# from ai.distraction.detector import DistractionDetector
# from ai.gaze.detector import GazeDetector
# from ai.emotion.detector import EmotionDetector
# from ai.phone.detector import PhoneDetector


class Aggregator:

    def __init__(self):

        # =========================
        # 🧠 ALL MODELS (TEMPORAL)
        # =========================
        self.models = {
            "fatigue": FatigueDetector(buffer_size=30, stride=5),
            # "distraction": DistractionDetector(buffer_size=20, stride=3),
            # "gaze": GazeDetector(buffer_size=10, stride=1),
            # "emotion": EmotionDetector(buffer_size=15, stride=2),
            # "phone": PhoneDetector(buffer_size=10, stride=1),
        }

        # =========================
        # 💾 LAST KNOWN RESULTS
        # =========================
        self.last_scores: Dict[str, Any] = {}

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
        # 📦 FINAL JSON OUTPUT
        # =========================
        return {
            "frame_id": frame_id,
            "timestamp": timestamp,
            "scores": scores
        }


# =========================
# SINGLETON INSTANCE
# =========================
aggregator = Aggregator()
# {
#   "frame_id": "b3f1c2a8-91d4-4a2e-9c1b-6f8d9e2a1234",
#   "timestamp": 1713948123.52,
#   "scores": {
#     "fatigue": 0.78,
#     "distraction": 0.34,
#     "gaze": {
#       "direction": "left",
#       "confidence": 0.82
#     },
#     "emotion": {
#       "class": "neutral",
#       "confidence": 0.91
#     },
#     "phone": 0.12
#   }
# }