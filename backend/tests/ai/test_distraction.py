# backend/tests/ai/test_distraction.py

import numpy as np
from pipeline.frame_context import FrameContext
from ai.distraction.detector import DistractionDetector


def make_frame(head_pose=None, gaze=None) -> FrameContext:
    return FrameContext(
        frame_id  ="test-dist",
        timestamp =0.0,
        raw_jpeg  =b"",
        full_image=np.zeros((480, 640, 3), dtype=np.uint8),
        landmarks ={
            "eyes": None, "mouth": None,
            "head_pose": head_pose,
            "gaze":      gaze,
            "hands":     None,
        },
    )


def make_frames(n: int, head_pose=None, gaze=None) -> list[FrameContext]:
    return [make_frame(head_pose=head_pose, gaze=gaze) for _ in range(n)]


# ── Tests ────────────────────────────────────────────────────

def test_no_face_when_no_landmarks():
    detector = DistractionDetector()
    result   = detector.predict(make_frames(15))
    assert result["label"]  == "no_face"
    assert result["score"]  == 0.0

def test_score_in_valid_range():
    head = {"yaw": 25.0, "pitch": -10.0, "roll": 3.0}
    gaze = {"direction": "right", "vector": [0.5, 0.0]}
    result = DistractionDetector().predict(make_frames(20, head_pose=head, gaze=gaze))
    assert 0.0 <= result["score"] <= 1.0
    assert all(k in result for k in ["score", "confidence", "label", "details"])

def test_extreme_distraction_high_score():
    head = {"yaw": 50.0, "pitch": 0.0, "roll": 0.0}
    gaze = {"direction": "left", "vector": [-0.9, 0.0]}
    result = DistractionDetector().predict(make_frames(20, head_pose=head, gaze=gaze))
    assert result["score"] >= 0.7
    assert result["label"] == "highly_distracted"

def test_centered_gaze_attentive():
    head = {"yaw": 2.0, "pitch": 1.0, "roll": 0.0}
    gaze = {"direction": "center", "vector": [0.0, 0.0]}
    result = DistractionDetector().predict(make_frames(20, head_pose=head, gaze=gaze))
    assert result["score"] < 0.3
    assert result["label"] == "attentive"

def test_predict_receives_list_not_single_frame():
    """Vérifie que predict() accepte bien une liste (BaseTemporalDetector)."""
    frames = make_frames(10)
    result = DistractionDetector().predict(frames)
    assert isinstance(result, dict)
    assert "frames_used" in result["details"]