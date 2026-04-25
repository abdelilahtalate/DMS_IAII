import time
import uuid
import cv2
import numpy as np
import mediapipe as mp

from pipeline.frame_context import FrameContext


# =========================
# CONFIG
# =========================
FRAME_W = 640
FRAME_H = 480
FACE_CROP_SIZE = 112


# =========================
# MEDIAPIPE INIT
# =========================
mp_face = mp.solutions.face_mesh
mp_hands = mp.solutions.hands

face_mesh = mp_face.FaceMesh(
    static_image_mode=False,
    max_num_faces=2,  # we detect up to 2 only to reject multi-face
    refine_landmarks=True,
    min_detection_confidence=0.5,
    min_tracking_confidence=0.5
)

hands_detector = mp_hands.Hands(
    static_image_mode=False,
    max_num_hands=2,
    min_detection_confidence=0.5,
    min_tracking_confidence=0.5
)


# =========================
# LANDMARK INDEXES
# =========================
LEFT_EYE_IDX = [362, 385, 387, 263, 373, 380]
RIGHT_EYE_IDX = [33, 160, 158, 133, 153, 144]

LEFT_IRIS_IDX = [474, 475, 476, 477]
RIGHT_IRIS_IDX = [469, 470, 471, 472]

MOUTH_IDX = [13, 14, 78, 308]


# =========================
# HELPERS
# =========================
def _to_points(lm, w, h, idx_list):
    return [(lm[i].x * w, lm[i].y * h) for i in idx_list]


def _face_crop(image, landmarks, w, h):
    xs = [lm.x * w for lm in landmarks]
    ys = [lm.y * h for lm in landmarks]

    x1, x2 = int(min(xs)), int(max(xs))
    y1, y2 = int(min(ys)), int(max(ys))

    pad = 10
    x1 = max(x1 - pad, 0)
    y1 = max(y1 - pad, 0)
    x2 = min(x2 + pad, w)
    y2 = min(y2 + pad, h)

    crop = image[y1:y2, x1:x2]

    if crop.size == 0:
        return np.zeros((FACE_CROP_SIZE, FACE_CROP_SIZE, 3), dtype=np.uint8)

    return cv2.resize(crop, (FACE_CROP_SIZE, FACE_CROP_SIZE))


# =========================
# MAIN PREPROCESSOR
# =========================
def preprocess(raw_jpeg: bytes) -> FrameContext:

    start = time.perf_counter()

    frame_id = str(uuid.uuid4())
    timestamp = time.time()

    # -------------------------
    # Decode image
    # -------------------------
    arr = np.frombuffer(raw_jpeg, np.uint8)
    image = cv2.imdecode(arr, cv2.IMREAD_COLOR)

    if image is None:
        print("image is None")
        return FrameContext(
            frame_id=frame_id,
            timestamp=timestamp,
            raw_frame=np.zeros((FRAME_H, FRAME_W, 3), np.uint8),
            face_crop=None,
            left_eye_points=None,
            right_eye_points=None,
            iris_points=None,
            mouth_points=None,
            hands=None,
            face_detected=False,
            preprocessing_ms=0.0,
        )

    # -------------------------
    # Resize
    # -------------------------
    image = cv2.resize(image, (FRAME_W, FRAME_H))
    h, w = image.shape[:2]

    rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

    # -------------------------
    # Face detection
    # -------------------------
    result = face_mesh.process(rgb)

    left_eye = None
    right_eye = None
    iris = None
    mouth = None
    face_crop = None
    face_detected = False

    # =========================
    # ❗ MULTI-FACE RULE
    # =========================
    if result.multi_face_landmarks:

        # 🚨 MORE THAN 1 FACE → REJECT FRAME
        if len(result.multi_face_landmarks) != 1:
            return FrameContext(
                frame_id=frame_id,
                timestamp=timestamp,
                raw_frame=image,
                face_crop=None,
                left_eye_points=None,
                right_eye_points=None,
                iris_points=None,
                mouth_points=None,
                hands=None,
                face_detected=False,
                preprocessing_ms=(time.perf_counter() - start) * 1000,
            )

        # =========================
        # SINGLE FACE → PROCESS
        # =========================
        face_detected = True
        lm = result.multi_face_landmarks[0].landmark

        left_eye = _to_points(lm, w, h, LEFT_EYE_IDX)
        right_eye = _to_points(lm, w, h, RIGHT_EYE_IDX)

        iris = _to_points(lm, w, h, LEFT_IRIS_IDX + RIGHT_IRIS_IDX)

        mouth = _to_points(lm, w, h, MOUTH_IDX)

        face_crop = _face_crop(image, lm, w, h)

    # -------------------------
    # Hand detection
    # -------------------------
    hand_result = hands_detector.process(rgb)
    hands_dict = None
    
    if hand_result.multi_hand_landmarks:
        positions = []
        for hand_landmarks in hand_result.multi_hand_landmarks:
            for lm in hand_landmarks.landmark:
                positions.append((lm.x * w, lm.y * h))
        
        near_face = False
        if face_detected and result.multi_face_landmarks:
            face_lm = result.multi_face_landmarks[0].landmark
            face_xs = [lm.x * w for lm in face_lm]
            face_ys = [lm.y * h for lm in face_lm]
            fx1, fx2 = min(face_xs), max(face_xs)
            fy1, fy2 = min(face_ys), max(face_ys)
            
            expand_x = (fx2 - fx1) * 0.5
            expand_y = (fy2 - fy1) * 0.5
            
            for px, py in positions:
                if fx1 - expand_x <= px <= fx2 + expand_x and fy1 - expand_y <= py <= fy2 + expand_y:
                    near_face = True
                    break
                    
        hands_dict = {
            "detected": True,
            "near_face": near_face,
            "positions": positions
        }

    # -------------------------
    # Build FrameContext
    # -------------------------
    end = time.perf_counter()

    return FrameContext(
        frame_id=frame_id,
        timestamp=timestamp,
        raw_frame=image,
        face_crop=face_crop,

        left_eye_points=left_eye,
        right_eye_points=right_eye,
        iris_points=iris,
        mouth_points=mouth,
        
        hands=hands_dict,

        face_detected=face_detected,
        preprocessing_ms=(end - start) * 1000
    )