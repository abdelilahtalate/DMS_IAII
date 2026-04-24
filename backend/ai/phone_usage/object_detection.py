# backend/ai/phone_usage/object_detection.py

"""
Détecte la présence d'un téléphone dans l'image complète (full_image).

Utilise un modèle de détection d'objets passé depuis PhoneUsageDetector.
Si le modèle n'est pas encore chargé (None), retourne score=0.0 sans erreur —
la hand_tracking prend alors le relais.

Pour brancher ton modèle :
  - ONNX Runtime  : remplace _run_inference() avec session.run(...)
  - YOLOv8        : remplace _run_inference() avec model.predict(image)
  - PyTorch       : adapter selon ton pipeline
"""

import numpy as np

# ── Constantes ──────────────────────────────────────────────
# Classe COCO "cell phone" = 67
PHONE_CLASS_ID       = 67
DETECTION_THRESHOLD  = 0.50    # confiance minimale pour valider

# Taille d'entrée typique pour MobileNet/YOLO (adapter si besoin)
INPUT_SIZE = (320, 320)


def compute_object_score(
    image: np.ndarray | None,
    model=None,
) -> tuple[float, dict]:
    """
    Détecte un téléphone dans full_image via un modèle de détection d'objets.

    Args:
        image : full_image depuis FrameContext (H, W, 3) uint8, ou None.
        model : modèle chargé dans PhoneUsageDetector.load_model().
                Si None → retourne score neutre sans erreur.

    Returns:
        (score: float [0.0–1.0], details: dict)
    """
    # Pas d'image
    if image is None:
        return 0.0, {
            "detected":   False,
            "confidence": 0.0,
            "bbox":       None,
        }

    # Modèle non chargé → pas de détection possible
    if model is None:
        return 0.0, {
            "detected":   False,
            "confidence": 0.30,   # incertitude explicite
            "bbox":       None,
        }

    try:
        detections = _run_inference(model, image)
        best       = _get_best_phone_detection(detections)

        if best is None:
            return 0.0, {
                "detected":   False,
                "confidence": 0.90,
                "bbox":       None,
            }

        conf  = best["confidence"]
        score = _confidence_to_score(conf)

        return round(score, 4), {
            "detected":   True,
            "confidence": round(conf, 4),
            "bbox":       best["bbox"],   # [x1, y1, x2, y2] normalisé 0–1
        }

    except Exception as e:
        # Ne jamais faire crasher l'aggregator
        return 0.0, {
            "detected":   False,
            "confidence": 0.0,
            "bbox":       None,
            "error":      str(e),
        }


# ── Fonctions internes ───────────────────────────────────────
# À adapter selon le modèle choisi

def _run_inference(model, image: np.ndarray) -> list[dict]:
    """
    Lance l'inférence sur l'image et retourne les détections brutes.

    Format attendu en retour :
    [
        {
            "class_id":   int,
            "confidence": float,          # 0.0 → 1.0
            "bbox":       [x1, y1, x2, y2],  # coordonnées normalisées 0–1
        },
        ...
    ]

    ── Exemples d'adaptation ──────────────────────────────────

    YOLOv8 (ultralytics) :
        results = model.predict(image, conf=0.3, verbose=False)
        boxes   = results[0].boxes
        return [
            {
                "class_id":   int(b.cls),
                "confidence": float(b.conf),
                "bbox":       b.xyxyn[0].tolist(),
            }
            for b in boxes
        ]

    ONNX Runtime (MobileNet SSD) :
        blob  = preprocess(image, INPUT_SIZE)
        boxes, scores, classes, _ = model.run(None, {"inputs": blob})
        return [
            {
                "class_id":   int(classes[0][i]),
                "confidence": float(scores[0][i]),
                "bbox":       boxes[0][i].tolist(),
            }
            for i in range(len(scores[0]))
        ]
    ──────────────────────────────────────────────────────────
    """
    # Placeholder — aucune inférence réelle
    return []


def _get_best_phone_detection(detections: list[dict]) -> dict | None:
    """
    Filtre les détections pour ne garder que les téléphones
    au-dessus du seuil de confiance.

    Returns:
        La détection avec la plus haute confiance, ou None.
    """
    phone_dets = [
        d for d in detections
        if d.get("class_id")   == PHONE_CLASS_ID
        and d.get("confidence", 0.0) >= DETECTION_THRESHOLD
    ]

    if not phone_dets:
        return None

    return max(phone_dets, key=lambda d: d["confidence"])


def _confidence_to_score(confidence: float) -> float:
    """
    Convertit une confidence de détection [0.5–1.0] en score de risque [0.5–1.0].
    Interpolation linéaire : conf=0.5 → score=0.5, conf=1.0 → score=1.0
    """
    t = (confidence - DETECTION_THRESHOLD) / (1.0 - DETECTION_THRESHOLD)
    return 0.5 + 0.5 * t