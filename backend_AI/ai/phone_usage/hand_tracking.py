# backend/ai/phone_usage/hand_tracking.py

SCORE_NO_HAND = 0.0
SCORE_HAND_ONLY = 0.25
SCORE_HAND_NEAR_FACE = 0.70

def compute_hand_score(hands):
    """
    Calcule un score basé sur la détection des mains.
    
    Args:
        hands: dict avec clés:
            - detected (bool)
            - near_face (bool)
            - positions (list)
    
    Returns:
        tuple (score, details)
    """
    # Cas 1 : pas de main
    if hands is None:
        return SCORE_NO_HAND, {
            "detected": False,
            "near_face": False,
            "num_keypoints": 0,
            "confidence": 0.5,
        }
    
    detected = hands.get("detected", False)
    near_face = hands.get("near_face", False)
    
    # Cas 2 : main non détectée
    if not detected:
        return SCORE_NO_HAND, {
            "detected": False,
            "near_face": False,
            "num_keypoints": 0,
            "confidence": 0.88,
        }
    
    # Cas 3 : main détectée
    if near_face:
        score = SCORE_HAND_NEAR_FACE
    else:
        score = SCORE_HAND_ONLY
    
    return score, {
        "detected": True,
        "near_face": near_face,
        "num_keypoints": len(hands.get("positions", [])),
        "confidence": 0.8,
    }