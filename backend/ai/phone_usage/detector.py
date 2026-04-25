# backend/ai/phone_usage/detector.py

from ai.BaseInstantDetector import BaseInstantDetector
from pipeline.frame_context import FrameContext
from .hand_tracking import compute_hand_score
from .object_detection import compute_object_score
from ai.BaseTemporalDetector import BaseTemporalDetector


class PhoneUsageDetector(BaseTemporalDetector):

    name = "phone_usage"

    def __init__(self, buffer_size: int = 1, stride: int = 1):
        super().__init__(buffer_size=buffer_size, stride=stride)
        self.obj_model = None
        self.load_model()

    def load_model(self) -> None:
        self.obj_model = None

    def compute(self) -> dict:
        """
        Appelé sur chaque frame (instant track).
        Combine la détection de main (landmarks) et la détection d'objet (image).
        """
        # print(aa)
        ctx = self.buffer[-1]
        
        # DEBUG: Afficher ce qui est reçu
        print("=" * 50)
        print("📊 predict() appelé")
        
        hands = ctx.hands
        print(f"👋 hands: {hands is not None}")
        if hands:
            print(f"   detected: {hands.get('detected')}")
            print(f"   near_face: {hands.get('near_face')}")
        
        # Utiliser raw_frame
        full_image = ctx.raw_frame
        print(f"🖼️ full_image: {full_image is not None}")

        # Score 1 : analyse des landmarks de la main
        hand_score, hand_details = compute_hand_score(hands)
        print(f"✋ hand_score: {hand_score}")
        print(f"   hand_details: {hand_details}")

        # Score 2 : détection objet sur l'image complète
        obj_score, obj_details = compute_object_score(full_image, self.obj_model)
        print(f"📱 obj_score: {obj_score}")

        # Fusion : objet 55% + main 45%
        final_score = (obj_score * 0.55) + (hand_score * 0.45)
        final_score = round(min(max(final_score, 0.0), 1.0), 4)
        print(f"🎯 final_score: {final_score}")

        # Confidence moyenne
        conf_hand = hand_details.get("confidence", 0.0)
        conf_obj = obj_details.get("confidence", 0.0)
        confidence = round((conf_hand + conf_obj) / 2, 4)

        # Label
        if final_score >= 0.65:
            label = "phone_detected"
        elif final_score >= 0.35:
            label = "possible_phone"
        else:
            label = "no_phone"

        result = {
            "score": final_score,
            "confidence": confidence,
            "label": label,
            "details": {
                "hand_near_face": hand_details.get("near_face", False),
                "hand_score": round(hand_score, 4),
                "object_detected": obj_details.get("detected", False),
                "obj_status": obj_details.get("status", ""),
                "object_score": round(obj_score, 4),
                "object_bbox": obj_details.get("bbox"),
            },
        }
        
        print(f"📤 Résultat: {result}")
        print("=" * 50)
        return result