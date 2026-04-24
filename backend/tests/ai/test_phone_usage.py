# backend/tests/ai/test_phone_usage.py

import numpy as np
from pipeline.frame_context import FrameContext
from ai.phone_usage.detector import PhoneUsageDetector


def make_ctx(hands=None, image=None) -> FrameContext:
    img = image if image is not None else np.zeros((480, 640, 3), dtype=np.uint8)
    return FrameContext(
        frame_id  ="test-phone",
        timestamp =0.0,
        raw_jpeg  =b"",
        full_image=img,
        landmarks ={
            "eyes": None, "mouth": None,
            "head_pose": None, "gaze": None,
            "hands": hands,
        },
    )


# ── Tests ────────────────────────────────────────────────────

def test_no_hand_returns_no_phone():
    result = PhoneUsageDetector().predict(make_ctx())
    assert result["label"] == "no_phone"
    assert result["score"] < 0.35

def test_hand_near_face_raises_score():
    hands = {"detected": True, "positions": [(0.5, 0.3)] * 18, "near_face": True}
    result = PhoneUsageDetector().predict(make_ctx(hands=hands))
    assert result["score"] >= 0.3
    assert result["details"]["hand_near_face"] is True

def test_hand_detected_not_near_face_low_score():
    hands = {"detected": True, "positions": [(0.1, 0.9)] * 10, "near_face": False}
    result = PhoneUsageDetector().predict(make_ctx(hands=hands))
    assert result["score"] < 0.5

def test_score_in_valid_range():
    hands = {"detected": True, "positions": [(0.5, 0.5)] * 21, "near_face": True}
    result = PhoneUsageDetector().predict(make_ctx(hands=hands))
    assert 0.0 <= result["score"] <= 1.0
    assert all(k in result for k in ["score", "confidence", "label", "details"])

def test_predict_receives_single_frame():
    """Vérifie que predict() accepte un seul FrameContext (BaseInstantDetector)."""
    ctx    = make_ctx()
    result = PhoneUsageDetector().predict(ctx)
    assert isinstance(result, dict)