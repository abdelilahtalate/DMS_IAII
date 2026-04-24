# backend/ai/phone_usage/detector.py

from ai.base import BaseInstantDetector
from pipeline.frame_context import FrameContext
from .hand_tracking import compute_hand_score
from .object_detection import compute_object_score


class PhoneUsageDetector(BaseInstantDetector):

    name = "phone_usage"

    def load_model(self) -> None:
        # Charge ici le modèle de détection d'objets (YOLO, MobileNet, etc.)
        # Exemple : self.obj_model = ort.InferenceSession("weights/phone.onnx")
        self.obj_model = None   # placeholder — brancher le vrai modèle ici

    def predict(self, ctx: FrameContext) -> dict:
        """
        Appelé sur chaque frame (instant track).
        Combine la détection de main (landmarks) et la détection d'objet (image).
        """

        hands      = ctx.landmarks["hands"]
        full_image = ctx.full_image

        # Aucune donnée disponible
        if hands is None and full_image is None:
            return {
                "score":      0.0,
                "confidence": 0.0,
                "label":      "no_data",
                "details":    {},
            }

        # ── Score 1 : analyse des landmarks de la main ──
        hand_score, hand_details = compute_hand_score(hands)

        # ── Score 2 : détection objet sur l'image complète ──
        obj_score, obj_details = compute_object_score(full_image, self.obj_model)

        # ── Fusion : objet 55% + main 45% ──
        # Si pas de modèle chargé, obj_score = 0 → hand_score prédomine
        final_score = (obj_score * 0.55) + (hand_score * 0.45)
        final_score = round(min(max(final_score, 0.0), 1.0), 4)

        # Confidence moyenne des deux sources
        conf_hand = hand_details.get("confidence", 0.0)
        conf_obj  = obj_details.get("confidence",  0.0)
        confidence = round((conf_hand + conf_obj) / 2, 4)

        # Label
        if final_score >= 0.65:
            label = "phone_detected"
        elif final_score >= 0.35:
            label = "possible_phone"
        else:
            label = "no_phone"

        return {
            "score":      final_score,
            "confidence": confidence,
            "label":      label,
            "details": {
                "hand_near_face":  hand_details.get("near_face",  False),
                "hand_score":      round(hand_score, 4),
                "object_detected": obj_details.get("detected",   False),
                "object_score":    round(obj_score,  4),
                "object_bbox":     obj_details.get("bbox"),
            },
        }