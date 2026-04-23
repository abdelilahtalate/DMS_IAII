# DMS — Driver Monitoring System
## Full Repository Guide & Team Contract
# Soubai Hiba
---

## 1. Repository Structure

```
DMS/
├── README.md
├── .gitignore
├── docker-compose.yml
├── Makefile
│
├── camera/                          ← ESP32-CAM firmware (C++)
│   ├── platformio.ini
│   ├── README.md
│   └── src/
│       └── main.cpp
│
├── backend/                         ← FastAPI Python backend
│   ├── main.py
│   ├── requirements.txt
│   ├── .env.example
│   │
│   ├── core/
│   │   ├── config.py
│   │   ├── logger.py
│   │   └── exceptions.py
│   │
│   ├── api/
│   │   └── routes/
│   │       ├── stream.py            ← WebSocket: receives ESP32 frames
│   │       └── health.py
│   │
│   ├── pipeline/
│   │   ├── frame_context.py         ← THE SHARED CONTRACT (read-only for models)
│   │   ├── preprocessor.py          ← fills FrameContext once per frame
│   │   └── aggregator.py            ← runs all models → returns one JSON
│   │
│   ├── ai/
│   │   ├── base.py                  ← BaseDetector: every model inherits this
│   │   ├── registry.py              ← auto-discovers all detectors
│   │   │
│   │   ├── fatigue/
│   │   │   ├── __init__.py
│   │   │   ├── detector.py          ← FatigueDetector
│   │   │   ├── eye_closure.py
│   │   │   ├── blink_rate.py
│   │   │   └── yawn_detection.py
│   │   │
│   │   ├── distraction/
│   │   │   ├── __init__.py
│   │   │   ├── detector.py          ← DistractionDetector
│   │   │   ├── head_pose.py
│   │   │   └── gaze_direction.py
│   │   │
│   │   ├── phone_usage/
│   │   │   ├── __init__.py
│   │   │   ├── detector.py          ← PhoneUsageDetector
│   │   │   ├── hand_tracking.py
│   │   │   └── object_detection.py
│   │   │
│   │   └── emotion/
│   │       ├── __init__.py
│   │       ├── detector.py          ← EmotionDetector
│   │       └── facial_classifier.py
│   │
│   ├── schemas/
│   │   ├── frame.py
│   │   └── prediction.py            ← final JSON schema sent to Android
│   │
│   └── tests/
│       ├── conftest.py
│       ├── test_aggregator.py
│       └── ai/
│           ├── test_fatigue.py
│           ├── test_distraction.py
│           ├── test_phone_usage.py
│           └── test_emotion.py
│
└── android/
    ├── README.md
    └── (Android project — separate team)
```

---

## 2. The Shared Contract — `FrameContext`

**File:** `backend/pipeline/frame_context.py`

This is the single object every model receives. The preprocessor fills it once
per frame. Models are READ-ONLY — they never modify it.

```python
# backend/pipeline/frame_context.py

from dataclasses import dataclass, field
from typing import Optional
import numpy as np


@dataclass
class FrameContext:
    """
    Created once per frame by the preprocessor.
    Passed as-is to every AI model.

    RULES FOR ALL TEAM MEMBERS:
      - Never modify any field inside a detector.
      - If you need a derived value (e.g. resized image),
        compute it locally inside your detector.py.
      - Only the preprocessor writes to this object.
    """

    # ── Identity ──────────────────────────────────────────────
    frame_id:   str          # unique ID for this frame (UUID)
    timestamp:  float        # unix timestamp when frame arrived

    # ── Images ────────────────────────────────────────────────
    raw_jpeg:   bytes        # original JPEG bytes from ESP32-CAM

    full_image: np.ndarray   # full decoded frame (H, W, 3) uint8
                             # → used by: PhoneUsageDetector, EmotionDetector

    face_crop:  Optional[np.ndarray] = None
                             # cropped and aligned face region (112x112) uint8
                             # → used by: EmotionDetector
                             # None if no face detected in frame

    # ── Landmarks ─────────────────────────────────────────────
    landmarks: dict = field(default_factory=lambda: {

        "eyes": None,
        # dict | None
        # {
        #   "left_ear":  float,   # eye aspect ratio 0.0–1.0
        #   "right_ear": float,
        #   "blink":     bool,
        #   "closed":    bool,    # True if EAR < threshold
        # }
        # → used by: FatigueDetector

        "mouth": None,
        # dict | None
        # {
        #   "mar": float,         # mouth aspect ratio 0.0–1.0
        #   "yawning": bool,
        # }
        # → used by: FatigueDetector

        "head_pose": None,
        # dict | None
        # {
        #   "yaw":   float,       # degrees, left(-) right(+)
        #   "pitch": float,       # degrees, down(-) up(+)
        #   "roll":  float,       # degrees
        # }
        # → used by: DistractionDetector

        "gaze": None,
        # dict | None
        # {
        #   "direction": str,     # "center"|"left"|"right"|"up"|"down"
        #   "vector":    list,    # [x, y] normalized gaze vector
        # }
        # → used by: DistractionDetector

        "hands": None,
        # dict | None
        # {
        #   "detected":    bool,
        #   "positions":   list,  # list of (x, y) hand keypoints
        #   "near_face":   bool,
        # }
        # → used by: PhoneUsageDetector
    })
```

---

## 3. The Base Class — `BaseDetector`

**File:** `backend/ai/base.py`

Every model your team builds **must** inherit from this class.
It enforces the same interface across all detectors.

```python
# backend/ai/base.py

from abc import ABC, abstractmethod
from pipeline.frame_context import FrameContext


class BaseDetector(ABC):
    """
    Every AI model in this project inherits from BaseDetector.

    Your only job:
      1. Receive a FrameContext (read-only)
      2. Run your model logic
      3. Return a dict with the required fields (see predict docstring)
    """

    # Set this in your subclass — used by registry and aggregator
    name: str = ""

    def __init__(self):
        self.load_model()

    @abstractmethod
    def load_model(self) -> None:
        """
        Load your model weights, initialize resources.
        Called once at startup — not per frame.
        """
        pass

    @abstractmethod
    def predict(self, ctx: FrameContext) -> dict:
        """
        Run inference on one frame.

        Args:
            ctx: FrameContext — read-only, shared across all models.

        Returns:
            dict with these REQUIRED keys:
            {
                "score":      float,   # 0.0 (safe) → 1.0 (critical)
                "confidence": float,   # 0.0 → 1.0, how sure the model is
                "label":      str,     # human-readable state e.g. "drowsy"
                "details":    dict,    # any extra info specific to your model
            }

        Raise:
            DetectorError if something goes wrong — never return None.
        """
        pass
```

---

## 4. Team Guide — How to Build Your Model

### Step 1 — Go to your folder

Each team member works only inside their assigned folder:

| Team member | Folder |
|---|---|
| Fatigue team | `backend/ai/fatigue/` |
| Distraction team | `backend/ai/distraction/` |
| Phone usage team | `backend/ai/phone_usage/` |
| Emotion team | `backend/ai/emotion/` |

### Step 2 — Write your `detector.py`

This is the only file the aggregator calls. It must follow this exact pattern:

```python
# backend/ai/fatigue/detector.py  (example — fatigue team)

from ai.base import BaseDetector
from pipeline.frame_context import FrameContext
from .eye_closure import compute_eye_closure
from .blink_rate import compute_blink_rate
from .yawn_detection import compute_yawn


class FatigueDetector(BaseDetector):

    name = "fatigue"

    def load_model(self) -> None:
        # load your model weights here
        # example: self.model = load("weights/fatigue_model.onnx")
        pass

    def predict(self, ctx: FrameContext) -> dict:

        eyes  = ctx.landmarks["eyes"]
        mouth = ctx.landmarks["mouth"]

        # if landmarks missing (no face detected), return safe default
        if eyes is None or mouth is None:
            return {
                "score":      0.0,
                "confidence": 0.0,
                "label":      "no_face",
                "details":    {},
            }

        eye_score  = compute_eye_closure(eyes)
        blink_score = compute_blink_rate(eyes)
        yawn_score  = compute_yawn(mouth)

        final_score = (eye_score * 0.5) + (blink_score * 0.3) + (yawn_score * 0.2)

        return {
            "score":      round(final_score, 4),
            "confidence": 0.91,
            "label":      "drowsy" if final_score > 0.6 else "alert",
            "details": {
                "eye_closure": eye_score,
                "blink_rate":  blink_score,
                "yawn":        yawn_score,
            },
        }
```

---

```python
# backend/ai/emotion/detector.py  (example — emotion team)

import cv2
from ai.base import BaseDetector
from pipeline.frame_context import FrameContext
from .facial_classifier import classify_emotion


class EmotionDetector(BaseDetector):

    name = "emotion"

    def load_model(self) -> None:
        # load CNN weights
        # self.model = load("weights/emotion_cnn.onnx")
        pass

    def predict(self, ctx: FrameContext) -> dict:

        # emotion uses face_crop — full image is available too
        if ctx.face_crop is None:
            return {
                "score":      0.0,
                "confidence": 0.0,
                "label":      "no_face",
                "details":    {},
            }

        # if you need a different size, resize LOCALLY — never modify ctx
        face = cv2.resize(ctx.face_crop, (48, 48))

        emotion_label, confidence = classify_emotion(self.model, face)

        # score encodes risk: angry/fearful = higher risk
        risk_map = {
            "neutral": 0.1, "happy": 0.05, "surprised": 0.3,
            "sad": 0.5,     "angry": 0.8,  "fearful": 0.7,
            "disgusted": 0.4,
        }
        score = risk_map.get(emotion_label, 0.2)

        return {
            "score":      round(score, 4),
            "confidence": round(confidence, 4),
            "label":      emotion_label,
            "details": {
                "all_emotions": {},  # optional: scores for each class
            },
        }
```

### Step 3 — Put helper logic in separate files

Keep `detector.py` clean. Heavy logic goes in sibling files:

```
fatigue/
├── detector.py          ← thin: calls helpers, returns score
├── eye_closure.py       ← compute_eye_closure(eyes: dict) -> float
├── blink_rate.py        ← compute_blink_rate(eyes: dict) -> float
└── yawn_detection.py    ← compute_yawn(mouth: dict) -> float
```

### Step 4 — Register your detector

Open `backend/ai/registry.py` and add one line:

```python
# backend/ai/registry.py

from ai.fatigue.detector    import FatigueDetector
from ai.distraction.detector import DistractionDetector
from ai.phone_usage.detector import PhoneUsageDetector
from ai.emotion.detector    import EmotionDetector

ALL_DETECTORS = [
    FatigueDetector(),
    DistractionDetector(),
    PhoneUsageDetector(),
    EmotionDetector(),
    # new model? add one line here — nothing else changes
]
```

### Step 5 — Write your test

```python
# backend/tests/ai/test_fatigue.py

import numpy as np
from pipeline.frame_context import FrameContext
from ai.fatigue.detector import FatigueDetector

def make_ctx(eyes=None, mouth=None):
    return FrameContext(
        frame_id="test-001",
        timestamp=0.0,
        raw_jpeg=b"",
        full_image=np.zeros((480, 640, 3), dtype=np.uint8),
        face_crop=np.zeros((112, 112, 3), dtype=np.uint8),
        landmarks={"eyes": eyes, "mouth": mouth,
                   "head_pose": None, "gaze": None, "hands": None},
    )

def test_returns_no_face_when_landmarks_missing():
    detector = FatigueDetector()
    result = detector.predict(make_ctx())
    assert result["label"] == "no_face"
    assert result["score"] == 0.0

def test_score_in_valid_range():
    eyes  = {"left_ear": 0.2, "right_ear": 0.2, "blink": False, "closed": True}
    mouth = {"mar": 0.1, "yawning": False}
    result = FatigueDetector().predict(make_ctx(eyes=eyes, mouth=mouth))
    assert 0.0 <= result["score"] <= 1.0
    assert "confidence" in result
    assert "label" in result
    assert "details" in result
```

---

## 5. The Aggregator — Runs All Models, Returns One JSON

**File:** `backend/pipeline/aggregator.py`

This is the only file that calls all detectors. It is managed by the pipeline
team — individual model developers do not touch it.

```python
# backend/pipeline/aggregator.py

import time
from ai.registry import ALL_DETECTORS
from pipeline.frame_context import FrameContext
from core.logger import logger


def run_all(ctx: FrameContext) -> dict:
    """
    Runs every registered detector on the same FrameContext.
    Returns one unified JSON dict — sent directly to Android via WebSocket.

    Args:
        ctx: FrameContext — populated by preprocessor, read-only.

    Returns:
        {
            "frame_id":  str,
            "timestamp": float,
            "scores": {
                "fatigue":     { "score": float, "confidence": float,
                                 "label": str,   "details": dict },
                "distraction": { ... },
                "phone_usage": { ... },
                "emotion":     { ... },
            }
        }
    """

    scores = {}

    for detector in ALL_DETECTORS:
        try:
            scores[detector.name] = detector.predict(ctx)
        except Exception as e:
            logger.error(f"[{detector.name}] failed on frame {ctx.frame_id}: {e}")
            scores[detector.name] = {
                "score":      0.0,
                "confidence": 0.0,
                "label":      "error",
                "details":    {"error": str(e)},
            }

    return {
        "frame_id":  ctx.frame_id,
        "timestamp": ctx.timestamp,
        "scores":    scores,
    }
```

---

## 6. Final JSON Sent to Android

Every frame produces exactly this structure over WebSocket:

```json
{
  "frame_id":  "a3f2c1d4-...",
  "timestamp": 1718000000.123,
  "scores": {
    "fatigue": {
      "score":      0.82,
      "confidence": 0.91,
      "label":      "drowsy",
      "details": {
        "eye_closure": 0.75,
        "blink_rate":  0.80,
        "yawn":        0.90
      }
    },
    "distraction": {
      "score":      0.45,
      "confidence": 0.87,
      "label":      "slightly_distracted",
      "details": {
        "head_yaw":   22.5,
        "head_pitch": -8.1,
        "gaze":       "left"
      }
    },
    "phone_usage": {
      "score":      0.10,
      "confidence": 0.95,
      "label":      "no_phone",
      "details": {
        "hand_near_face": false,
        "object_detected": false
      }
    },
    "emotion": {
      "score":      0.05,
      "confidence": 0.88,
      "label":      "neutral",
      "details": {
        "all_emotions": {}
      }
    }
  }
}
```

The Android team receives this JSON on every frame and handles all
decision-making, thresholds, and alerts on their side.

---

## 7. Full Pipeline Flow (Code Level)

```
ESP32-CAM
   │  binary JPEG over WebSocket
   ▼
api/routes/stream.py          → receives raw bytes, creates frame_id
   │
   ▼
pipeline/preprocessor.py      → decodes JPEG, fills FrameContext
   │                             (full_image, face_crop, all landmarks)
   ▼
pipeline/aggregator.run_all() → loops over ALL_DETECTORS
   │                             each detector.predict(ctx) → score dict
   │                             wraps all scores into final JSON
   ▼
api/routes/stream.py          → sends JSON over WebSocket to Android
```

---

## 8. Rules Every Team Member Must Follow

| Rule | Why |
|---|---|
| Never modify `FrameContext` inside a detector | It is shared — mutating it breaks other models |
| Always return all 4 keys: `score`, `confidence`, `label`, `details` | Aggregator and Android expect a fixed schema |
| `score` must be `float` between `0.0` and `1.0` | Android uses it for thresholds directly |
| Handle the `None` landmark case | Not every frame has a detected face |
| Local resizes stay local | Use `ctx.full_image` or `ctx.face_crop` as a base, resize inside your function |
| One detector per category folder | Never import from another model's folder |
| Write at least 2 tests | One for missing landmarks, one for valid output range |

---

## 9. Adding a New Model (Future)

Say the team adds a seatbelt detection model:

1. Create `backend/ai/seatbelt/`
2. Add `detector.py` inheriting `BaseDetector`, set `name = "seatbelt"`
3. Add helper files as needed
4. Register in `registry.py`: `SeatbeltDetector()`
5. The aggregator picks it up automatically
6. Android receives a new `"seatbelt"` key in the JSON

**Zero changes** to preprocessor, aggregator logic, or any other model.

---

## 10. Quick Reference — Who Uses What from FrameContext

| Model | `full_image` | `face_crop` | `landmarks["eyes"]` | `landmarks["mouth"]` | `landmarks["head_pose"]` | `landmarks["gaze"]` | `landmarks["hands"]` |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| FatigueDetector | | | ✓ | ✓ | | | |
| DistractionDetector | | | | | ✓ | ✓ | |
| PhoneUsageDetector | ✓ | | | | | | ✓ |
| EmotionDetector | | ✓ | | | | | |
