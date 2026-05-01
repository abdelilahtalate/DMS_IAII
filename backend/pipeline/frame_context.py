from dataclasses import dataclass
from typing import Optional, List, Tuple
import numpy as np


Point = Tuple[float, float]

# hena li khsato xi haja isiftli bhal xi jointure jdid wela ila bgha image kamle kima hya isftlyaa
@dataclass
class FrameContext:
    """
    Pure RAW data container.
    NO AI logic inside.
    Only stores extracted landmarks + images.
    """

    # =========================
    # 🆔 Identity
    # =========================
    frame_id: str
    timestamp: float

    # =========================
    # 📥 FRAME DATA
    # =========================
    raw_frame: np.ndarray              # original camera frame (BGR)
    face_crop: Optional[np.ndarray]    # cropped face image
    nose_tip: Optional[Point]
    chin: Optional[Point]
    # =========================
    # 👁 EYES (RAW LANDMARKS ONLY)
    # =========================
    left_eye_points: Optional[List[Point]]
    right_eye_points: Optional[List[Point]]
    iris_points: Optional[List[Point]]

    # =========================
    # 👄 MOUTH (RAW LANDMARKS ONLY)
    # =========================
    mouth_points: Optional[List[Point]]

    # =========================
    # ⚡ METADATA
    # =========================
    face_detected: bool
    preprocessing_ms: float = 0.0

    # =========================
    # ✋ HANDS (EXTRA)
    # =========================
    hands: Optional[dict] = None