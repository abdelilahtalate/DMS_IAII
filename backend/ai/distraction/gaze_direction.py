# backend/ai/distraction/gaze_direction.py

"""
Analyse une liste de gaze dicts sur une fenêtre temporelle
et retourne un score de distraction agrégé.

Chaque gaze dict (depuis FrameContext.landmarks["gaze"]):
{
    "direction": str,     # "center"|"left"|"right"|"up"|"down"
    "vector":    list,    # [x, y] vecteur normalisé [-1, 1]
}
"""

from collections import Counter


# Score de risque par direction (instantané)
GAZE_RISK_MAP: dict[str, float] = {
    "center": 0.0,
    "up":     0.25,
    "down":   0.35,
    "left":   0.80,
    "right":  0.80,
}

# Seuils vecteur
VECTOR_WARN = 0.3
VECTOR_CRIT = 0.65

# Fraction de frames off-center pour déclencher la pénalité durée
OFF_CENTER_RATIO_WARN = 0.35
OFF_CENTER_RATIO_CRIT = 0.65


def compute_gaze_score(gaze_list: list[dict]) -> tuple[float, dict]:
    """
    Analyse une liste de gaze sur la fenêtre temporelle.

    Args:
        gaze_list: liste de dicts gaze (non-None uniquement)

    Returns:
        (score: float [0.0–1.0], details: dict)
    """
    if not gaze_list:
        return 0.0, {"dominant_direction": None, "confidence": 0.0}

    directions = [g.get("direction", "center") for g in gaze_list]
    vectors    = [g.get("vector", [0.0, 0.0])  for g in gaze_list]

    # Direction dominante sur la fenêtre
    direction_counts    = Counter(directions)
    dominant_direction  = direction_counts.most_common(1)[0][0]

    # Score instantané par frame (combinaison direction + vecteur)
    per_frame_scores = [
        _frame_gaze_score(d, v)
        for d, v in zip(directions, vectors)
    ]

    # Fraction de frames off-center
    off_center_frames = sum(1 for d in directions if d != "center")
    off_center_ratio  = off_center_frames / len(directions)

    # Score moyen instantané
    avg_score = sum(per_frame_scores) / len(per_frame_scores)

    # Pénalité durée
    if off_center_ratio >= OFF_CENTER_RATIO_CRIT:
        duration_penalty = 1.0
    elif off_center_ratio >= OFF_CENTER_RATIO_WARN:
        t = (off_center_ratio - OFF_CENTER_RATIO_WARN) / \
            (OFF_CENTER_RATIO_CRIT - OFF_CENTER_RATIO_WARN)
        duration_penalty = 0.5 + 0.5 * t
    else:
        duration_penalty = off_center_ratio / OFF_CENTER_RATIO_WARN * 0.5

    # Fusion : 55% score moyen + 45% pénalité durée
    final = (avg_score * 0.55) + (duration_penalty * 0.45)

    return round(min(final, 1.0), 4), {
        "dominant_direction": dominant_direction,
        "direction_counts":   dict(direction_counts),
        "off_center_ratio":   round(off_center_ratio, 3),
        "avg_score":          round(avg_score, 4),
        "confidence":         0.85,
    }


def _frame_gaze_score(direction: str, vector: list) -> float:
    """
    Score [0.0–1.0] pour une seule frame basé sur direction + vecteur.
    """
    base_score   = GAZE_RISK_MAP.get(direction, 0.2)
    vector_score = _vector_to_score(vector)
    # Prend le max pour ne pas sous-estimer
    return max(base_score, vector_score * 0.85)


def _vector_to_score(vector: list) -> float:
    """
    Norme L2 du vecteur → score [0.0–1.0].
    """
    if not vector or len(vector) < 2:
        return 0.0
    magnitude = (float(vector[0]) ** 2 + float(vector[1]) ** 2) ** 0.5
    if magnitude <= VECTOR_WARN:
        return (magnitude / VECTOR_WARN) * 0.3
    elif magnitude <= VECTOR_CRIT:
        t = (magnitude - VECTOR_WARN) / (VECTOR_CRIT - VECTOR_WARN)
        return 0.3 + 0.7 * t
    return 1.0