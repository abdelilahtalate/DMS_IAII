# backend/ai/distraction/head_pose.py

"""
Analyse une liste de head_pose dicts sur une fenêtre temporelle
et retourne un score de distraction agrégé.

Chaque head_pose dict (depuis FrameContext.landmarks["head_pose"]):
{
    "yaw":   float,   # degrés, gauche(-) droite(+)
    "pitch": float,   # degrés, bas(-) haut(+)
    "roll":  float,   # degrés
}
"""

# ── Seuils en degrés ──────────────────────────────────────────
YAW_WARN   = 20.0    # début distraction
YAW_CRIT   = 40.0    # distraction critique
PITCH_WARN = 15.0
PITCH_CRIT = 30.0

# Seuil de fraction : si X% des frames sont distraites → score élevé
DISTRACTED_FRAME_RATIO_WARN = 0.30   # 30% des frames
DISTRACTED_FRAME_RATIO_CRIT = 0.60   # 60% des frames


def compute_head_pose_score(head_pose_list: list[dict]) -> tuple[float, dict]:
    """
    Analyse une liste de head_pose sur la fenêtre temporelle.

    Args:
        head_pose_list: liste de dicts head_pose (non-None uniquement)

    Returns:
        (score: float [0.0–1.0], details: dict)
    """
    if not head_pose_list:
        return 0.0, {"avg_yaw": None, "avg_pitch": None, "confidence": 0.0}

    yaws   = [h.get("yaw",   0.0) for h in head_pose_list]
    pitchs = [h.get("pitch", 0.0) for h in head_pose_list]

    avg_yaw   = sum(yaws)   / len(yaws)
    avg_pitch = sum(pitchs) / len(pitchs)

    # Score instantané par frame
    per_frame_scores = [
        _frame_head_score(y, p)
        for y, p in zip(yaws, pitchs)
    ]

    # Fraction de frames avec distraction significative (score > 0.4)
    distracted_frames = sum(1 for s in per_frame_scores if s > 0.4)
    distracted_ratio  = distracted_frames / len(per_frame_scores)

    # Score moyen sur la fenêtre
    avg_score = sum(per_frame_scores) / len(per_frame_scores)

    # Score final = combinaison du score moyen + pénalité sur la durée
    if distracted_ratio >= DISTRACTED_FRAME_RATIO_CRIT:
        duration_penalty = 1.0
    elif distracted_ratio >= DISTRACTED_FRAME_RATIO_WARN:
        t = (distracted_ratio - DISTRACTED_FRAME_RATIO_WARN) / \
            (DISTRACTED_FRAME_RATIO_CRIT - DISTRACTED_FRAME_RATIO_WARN)
        duration_penalty = 0.5 + 0.5 * t
    else:
        duration_penalty = distracted_ratio / DISTRACTED_FRAME_RATIO_WARN * 0.5

    # Fusion : 60% score moyen instantané + 40% pénalité durée
    final = (avg_score * 0.6) + (duration_penalty * 0.4)

    return round(min(final, 1.0), 4), {
        "avg_yaw":          round(avg_yaw,   2),
        "avg_pitch":        round(avg_pitch, 2),
        "avg_score":        round(avg_score, 4),
        "distracted_ratio": round(distracted_ratio, 3),
        "confidence":       0.90,
    }


def _frame_head_score(yaw: float, pitch: float) -> float:
    """
    Score [0.0–1.0] pour une seule frame basé sur yaw + pitch.
    """
    yaw_score   = _angle_to_score(abs(yaw),   YAW_WARN,   YAW_CRIT)
    pitch_score = _angle_to_score(abs(pitch), PITCH_WARN, PITCH_CRIT)
    return (yaw_score * 0.65) + (pitch_score * 0.35)


def _angle_to_score(angle: float, warn: float, crit: float) -> float:
    """
    Interpolation linéaire par morceaux :
      [0, warn]      → [0.0, 0.3]
      [warn, crit]   → [0.3, 1.0]
      > crit         → 1.0
    """
    if angle <= 0:
        return 0.0
    if angle >= crit:
        return 1.0
    if angle <= warn:
        return 0.3 * (angle / warn)
    t = (angle - warn) / (crit - warn)
    return 0.3 + 0.7 * t