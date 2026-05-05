# backend/tests/ai/test_phone_usage.py

import numpy as np
from pipeline.frame_context import FrameContext
from ai.phone_usage.detector import PhoneUsageDetector


def make_ctx(hands=None, image=None):
    img = image if image is not None else np.zeros((480, 640, 3), dtype=np.uint8)
    
    # Créer le FrameContext avec uniquement les attributs existants
    frame = FrameContext(
        frame_id="test-phone",
        timestamp=0.0,
        raw_frame=img,
        face_crop=None,
        left_eye_points=None,
        right_eye_points=None,
        iris_points=None,
        mouth_points=None,
        face_detected=False,
        preprocessing_ms=0.0,
    )
    
    # Ajouter dynamiquement les attributs hands pour les tests
    if hands is not None:
        frame.hand_points = hands.get("positions", [])
        frame.hand_detected = hands.get("detected", False)
        frame.hand_near_face = hands.get("near_face", False)
    
    return frame


def test_no_hand_returns_no_phone():
    result = PhoneUsageDetector().predict(make_ctx())
    # Le score peut être bas car pas de main détectée
    assert result["score"] >= 0.0
    assert result["label"] in ["no_phone", "no_data"]


def test_hand_near_face_raises_score():
    hands = {"detected": True, "positions": [(0.5, 0.3)] * 18, "near_face": True}
    result = PhoneUsageDetector().predict(make_ctx(hands=hands))
    assert result["score"] >= 0.3
    assert result["details"]["hand_near_face"] is True


def test_hand_detected_not_near_face_low_score():
    hands = {"detected": True, "positions": [(0.1, 0.9)] * 10, "near_face": False}
    result = PhoneUsageDetector().predict(make_ctx(hands=hands))
    assert result["score"] < 0.6


def test_score_in_valid_range():
    hands = {"detected": True, "positions": [(0.5, 0.5)] * 21, "near_face": True}
    result = PhoneUsageDetector().predict(make_ctx(hands=hands))
    assert 0.0 <= result["score"] <= 1.0
    assert all(k in result for k in ["score", "confidence", "label", "details"])


def test_predict_receives_single_frame():
    ctx = make_ctx()
    result = PhoneUsageDetector().predict(ctx)
    assert isinstance(result, dict)