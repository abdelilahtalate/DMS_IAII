# test_phone_only.py
import sys
sys.path.insert(0, '.')

from pipeline.frame_context import FrameContext
from ai.phone_usage.detector import PhoneUsageDetector
from ai.phone_usage.hand_tracking import compute_hand_score

print("=" * 50)
print("TEST 1: Test direct de compute_hand_score")
print("=" * 50)

# Test direct de la fonction hand_tracking
hands_data = {
    "detected": True,
    "near_face": True,
    "positions": [(0.5, 0.3) for _ in range(21)]
}

score, details = compute_hand_score(hands_data)
print(f"compute_hand_score direct:")
print(f"  score: {score}")
print(f"  details: {details}")
print()

print("=" * 50)
print("TEST 2: Test via PhoneUsageDetector")
print("=" * 50)

# Créer le détecteur
phone_det = PhoneUsageDetector()

# Créer FrameContext avec landmarks
from unittest.mock import MagicMock
import numpy as np
import time
import uuid

frame = np.zeros((480, 640, 3), dtype=np.uint8)

# Créer les landmarks dans la structure attendue
lm = {
    "hands": hands_data,
    "head_pose": {"yaw": 0, "pitch": 0, "roll": 0},
    "gaze": {"direction": "center", "vector": [0, 0]}
}

ctx = FrameContext(
    frame_id=str(uuid.uuid4()),
    timestamp=time.time(),
    raw_frame=frame,
    face_crop=None,
    left_eye_points=None,
    right_eye_points=None,
    iris_points=None,
    mouth_points=None,
    face_detected=True,
    preprocessing_ms=0.0,
)
ctx.landmarks = lm

print(f"Landmarks créés: {ctx.landmarks is not None}")
print(f"  hands: {ctx.landmarks.get('hands')}")
print()

print("Appel de phone_det.predict()...")
result = phone_det.predict(ctx)
print(f"Résultat: {result}")
print()

print("=" * 50)
print("TEST 3: Vérification dans le détecteur")
print("=" * 50)

# Simuler ce que fait le détecteur
hands = ctx.landmarks.get("hands")
print(f"hands récupéré: {hands}")
print(f"  detected: {hands.get('detected') if hands else None}")
print(f"  near_face: {hands.get('near_face') if hands else None}")

# Appeler directement compute_hand_score
score2, details2 = compute_hand_score(hands)
print(f"compute_hand_score dans le contexte: {score2}")