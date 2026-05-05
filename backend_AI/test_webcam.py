# backend/test_webcam.py
import cv2
import numpy as np
import uuid
import time
import os

import mediapipe as mp
from mediapipe.tasks import python
from mediapipe.tasks.python import vision

from pipeline.frame_context import FrameContext
from ai.distraction.detector import DistractionDetector
from ai.phone_usage.detector import PhoneUsageDetector

print("🚀 Démarrage du système DMS avec MediaPipe...")

# Initialisation des détecteurs
distraction_det = DistractionDetector()
phone_det = PhoneUsageDetector()

# Vérifier si les modèles existent
if not os.path.exists("models/face_landmarker.task"):
    print("❌ Modèle face_landmarker.task manquant!")
    print("   Télécharge-le depuis: https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task")
    exit(1)

if not os.path.exists("models/hand_landmarker.task"):
    print("❌ Modèle hand_landmarker.task manquant!")
    print("   Télécharge-le depuis: https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task")
    exit(1)

# Initialisation MediaPipe Face Landmarker
base_options = python.BaseOptions(model_asset_path="models/face_landmarker.task")
face_options = vision.FaceLandmarkerOptions(
    base_options=base_options,
    output_face_blendshapes=False,
    output_facial_transformation_matrixes=False,
    num_faces=1,
    min_face_detection_confidence=0.5,
    min_face_presence_confidence=0.5,
    min_tracking_confidence=0.5,
)
face_landmarker = vision.FaceLandmarker.create_from_options(face_options)

# Initialisation MediaPipe Hand Landmarker avec seuils plus bas pour meilleure détection
hand_base_options = python.BaseOptions(model_asset_path="models/hand_landmarker.task")
hand_options = vision.HandLandmarkerOptions(
    base_options=hand_base_options,
    num_hands=2,  # Permet de détecter 2 mains
    min_hand_detection_confidence=0.3,  # Seuil plus bas = plus sensible
    min_hand_presence_confidence=0.3,
    min_tracking_confidence=0.3,
)
hand_landmarker = vision.HandLandmarker.create_from_options(hand_options)

print("✅ MediaPipe initialisé avec succès")

def extract_landmarks(frame_rgb):
    """Extrait les landmarks avec MediaPipe."""
    landmarks = {
        "head_pose": None,
        "gaze": None,
        "hands": None
    }
    
    mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=frame_rgb)
    
    # Détection du visage
    face_result = face_landmarker.detect(mp_image)
    if face_result.face_landmarks:
        fl = face_result.face_landmarks[0]
        nose = fl[1]
        chin = fl[152]
        l_temp = fl[234]
        r_temp = fl[454]
        
        yaw = (r_temp.x - l_temp.x) * 90
        pitch = (nose.y - chin.y) * -90
        
        landmarks["head_pose"] = {
            "yaw": round(yaw, 2),
            "pitch": round(pitch, 2),
            "roll": 0.0
        }
        
        # Gaze simplifié
        try:
            l_iris = fl[468]
            r_iris = fl[473]
            gaze_x = ((l_iris.x + r_iris.x) / 2 - 0.5) * 2
            gaze_y = ((l_iris.y + r_iris.y) / 2 - 0.5) * 2
            
            if gaze_x < -0.3:
                direction = "left"
            elif gaze_x > 0.3:
                direction = "right"
            elif gaze_y < -0.3:
                direction = "up"
            elif gaze_y > 0.3:
                direction = "down"
            else:
                direction = "center"
            
            landmarks["gaze"] = {
                "direction": direction,
                "vector": [round(gaze_x, 3), round(gaze_y, 3)]
            }
        except:
            pass
    
    # Détection des mains
    hand_result = hand_landmarker.detect(mp_image)
    if hand_result.hand_landmarks and len(hand_result.hand_landmarks) > 0:
        # Prendre la première main détectée
        hl = hand_result.hand_landmarks[0]
        wrist = hl[0]
        # Calculer si la main est près du visage (y < 0.5 = partie haute de l'image)
        near_face = wrist.y < 0.5
        landmarks["hands"] = {
            "detected": True,
            "near_face": near_face,
            "positions": [(lm.x, lm.y) for lm in hl]
        }
    else:
        # Aucune main détectée
        landmarks["hands"] = {
            "detected": False,
            "near_face": False,
            "positions": []
        }
    
    return landmarks

def draw_overlay(frame, dist_result, phone_result):
    h, w = frame.shape[:2]
    overlay = frame.copy()
    cv2.rectangle(overlay, (0, 0), (550, 200), (0, 0, 0), -1)
    cv2.addWeighted(overlay, 0.55, frame, 0.45, 0, frame)
    
    ds = dist_result.get("score", 0)
    ps = phone_result.get("score", 0)
    
    # Couleurs selon le score
    color_d = (0, 0, 255) if ds > 0.3 else (0, 165, 255) if ds > 0.15 else (0, 200, 0)
    color_p = (0, 0, 255) if ps > 0.25 else (0, 165, 255) if ps > 0.1 else (0, 200, 0)
    
    # Distraction
    cv2.putText(frame, f"DISTRACTION: {dist_result.get('label', '?')} - {ds:.2f}", 
                (10, 35), cv2.FONT_HERSHEY_SIMPLEX, 0.6, color_d, 2)
    cv2.rectangle(frame, (10, 45), (10 + int(min(ds, 1.0) * 300), 55), color_d, -1)
    
    # Phone Usage
    cv2.putText(frame, f"PHONE USAGE: {phone_result.get('label', '?')} - {ps:.2f}", 
                (10, 85), cv2.FONT_HERSHEY_SIMPLEX, 0.6, color_p, 2)
    cv2.rectangle(frame, (10, 95), (10 + int(min(ps, 1.0) * 300), 105), color_p, -1)
    
    # Détails Phone
    hand_near = phone_result.get("details", {}).get("hand_near_face", False)
    hand_score = phone_result.get("details", {}).get("hand_score", 0)
    obj_score = phone_result.get("details", {}).get("object_score", 0)
    
    cv2.putText(frame, f"Hand near face: {hand_near} (score: {hand_score:.2f})", 
                (10, 140), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (200, 200, 200), 1)
    cv2.putText(frame, f"Object detection: {obj_score:.2f}", 
                (10, 160), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (200, 200, 200), 1)
    
    # Instructions
    cv2.putText(frame, "Press Q to quit", (w - 130, h - 10), 
                cv2.FONT_HERSHEY_SIMPLEX, 0.5, (150, 150, 150), 1)
    
    return frame

def main():
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("❌ Webcam introuvable")
        return
    
    # Augmenter la résolution pour meilleure détection
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)
    
    print("✅ Webcam ouverte — appuie sur Q pour quitter")
    print("🎯 Détection en temps réel avec MediaPipe")
    print("📱 Montre ta main près du visage pour tester phone_usage")
    
    dist_result = {"score": 0, "label": "starting...", "details": {}}
    phone_result = {"score": 0, "label": "starting...", "details": {}}
    
    frame_count = 0
    
    while True:
        ret, frame = cap.read()
        if not ret:
            break
        
        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        lm = extract_landmarks(frame_rgb)
        
        ctx = FrameContext(
            frame_id=str(uuid.uuid4()),
            timestamp=time.time(),
            raw_frame=frame,
            face_crop=None,
            left_eye_points=None, right_eye_points=None, iris_points=None,
            mouth_points=None,
            face_detected=lm["head_pose"] is not None,
            preprocessing_ms=0.0
        )
        ctx.landmarks = lm
        
        # Phone detection
        phone_result = phone_det.predict(ctx)
        
        # Distraction detection
        result = distraction_det.push_frame(ctx)
        if result:
            dist_result = result
        
        # Affichage
        frame = draw_overlay(frame, dist_result, phone_result)
        cv2.imshow("DMS System with MediaPipe", frame)
        
        # Debug console toutes les 30 frames
        frame_count += 1
        if frame_count % 30 == 0:
            hands_detected = lm.get("hands", {}).get("detected", False)
            print(f"[{frame_count}] Hands: {hands_detected}, Phone: {phone_result.get('score', 0):.2f} - {phone_result.get('label', '?')}")
        
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
    
    cap.release()
    cv2.destroyAllWindows()
    print("👋 Test terminé")

if __name__ == "__main__":
    main()