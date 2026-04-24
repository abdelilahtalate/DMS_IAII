# backend/ai/phone_usage/hand_tracking.py

"""
Analyse les landmarks de la main pour évaluer le risque d'usage téléphone.

hands dict (depuis FrameContext.landmarks["hands"]):
{
    "detected":  bool,
    "positions": list,    # liste de (x, y) keypoints (max 21 — MediaPipe)
    "near_face": bool,
}
"""

# ── Constantes de risque ──────────────────────────────────────
SCORE_NO_HAND       = 0.0
SCORE_HAND_ONLY     = 0.25    # main détectée, pas près du visage
SCORE_HAND_NEAR_FACE = 0.70   # main détectée ET près du visage

# Nombre max de keypoints MediaPipe (référence pour la confidence)
MEDIAPIPE_MAX_KP = 21


def compute_hand_score(hands: dict | None) -> tuple[float, dict]:
    """
    Calcule un score [0.0–1.0] de risque basé sur les landmarks de la main.

    Args:
        hands: dict depuis FrameContext.landmarks["hands"], ou None.

    Returns:
        (score: float, details: dict)
    """
    # Cas 1 : aucun landmark de main
    if hands is None:
        return SCORE_NO_HAND, {
            "detected":      False,
            "near_face":     False,
            "num_keypoints": 0,
            "confidence":    0.50,    # incertitude : peut-être hors cadre
        }

    detected  = hands.get("detected",  False)
    near_face = hands.get("near_face", False)
    positions = hands.get("positions", [])

    # Cas 2 : pas de main détectée
    if not detected:
        return SCORE_NO_HAND, {
            "detected":      False,
            "near_face":     False,
            "num_keypoints": 0,
            "confidence":    0.88,
        }

    # Cas 3 : main détectée
    num_kp = len(positions)

    # Confidence proportionnelle aux keypoints visibles
    confidence = round(min(0.50 + (num_kp / MEDIAPIPE_MAX_KP) * 0.45, 0.95), 4)

    score = SCORE_HAND_NEAR_FACE if near_face else SCORE_HAND_ONLY

    # Légère modulation par la qualité de la détection
    # (moins de keypoints → moins sûr → score légèrement réduit)
    kp_ratio = num_kp / MEDIAPIPE_MAX_KP
    score    = round(score * (0.75 + 0.25 * kp_ratio), 4)

    return score, {
        "detected":      True,
        "near_face":     near_face,
        "num_keypoints": num_kp,
        "confidence":    confidence,
    }