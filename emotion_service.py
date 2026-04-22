# backend/ai/emotion/facial_classifier.py

import cv2
from huggingface_hub import InferenceClient


def classify_emotion(client: InferenceClient, face_image):
    # Resize
    face = cv2.resize(face_image, (48, 48))

    # Convertir en bytes
    _, buffer = cv2.imencode(".jpg", face)

    # Appel modèle HuggingFace
    output = client.image_classification(
        buffer.tobytes(),
        model="mo-thecreator/vit-Facial-Expression-Recognition"
    )

    best = output[0]
    label = best["label"]
    confidence = float(best["score"])

    return label, confidence