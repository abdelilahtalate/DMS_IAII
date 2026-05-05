import numpy as np
# import torch
import cv2  # Added to force correct image formatting
from ultralytics import YOLO
from ai.BaseTemporalDetector import BaseTemporalDetector

class PhoneDetector(BaseTemporalDetector):
    def __init__(self, buffer_size: int = 15, stride: int = 3):
        super().__init__(buffer_size=buffer_size, stride=stride)
        
        self.device = 'cpu'
        print(f"📱 PhoneDetector Device: {self.device.upper()}")
        
        self.model = YOLO('yolov8n.pt').to(self.device)
        
        # 🔥 DROPPED CONFIDENCE TO 0.15 TO CATCH EVERYTHING
        self.YOLO_CONF = 0.15 
        self.PHONE_CLASS_ID = 77
        self.TRIGGER_RATIO = 0.5 

    def compute(self) -> dict:
        if not self.buffer: 
            return {"is_using_phone": False, "score": 0.0}
        
        phone_detected_frames = 0
        total_valid_frames = 0
        frames_to_check = list(self.buffer)[-5:] # Check last 5 frames
        
        for i, ctx in enumerate(frames_to_check):
            if ctx.raw_frame is None or not ctx.raw_frame.any():
                print("⚠️ PHONE DET: Raw frame is empty!")
                continue
                
            total_valid_frames += 1
            
            # 🔥 FORCE BGR 3-CHANNEL FORMAT (YOLO requirement)
            img = ctx.raw_frame
            if len(img.shape) == 2:
                img = cv2.cvtColor(img, cv2.COLOR_GRAY2BGR)
            elif img.shape[2] == 4:
                img = cv2.cvtColor(img, cv2.COLOR_BGRA2BGR)
                
            # Run YOLO
            results = self.model(img, verbose=False, conf=self.YOLO_CONF, device=self.device)[0]
            
            boxes = results.boxes
            frame_has_phone = False
            
            # 🔥 DEBUG: What did YOLO actually see in this frame?
            if boxes is not None and len(boxes) > 0:
                for box in boxes:
                    class_id = int(box.cls[0].cpu().numpy())
                    conf = float(box.conf[0].cpu().numpy())
                    
                    # Print ANY object YOLO sees with high confidence
                    if conf > 0.3:
                        print(f"🔍 YOLO Frame {i}: Saw Class {class_id} with Confidence {conf:.2f}")
                    
                    # Check if it's a phone (Class 77)
                    if class_id == self.PHONE_CLASS_ID:
                        print(f"📱🚨 YOLO FOUND PHONE! Confidence: {conf:.2f}")
                        frame_has_phone = True
                        break 
            else:
                # 🔥 DEBUG: YOLO saw absolutely nothing
                print(f"🔍 YOLO Frame {i}: Saw NOTHING")
                        
            if frame_has_phone:
                phone_detected_frames += 1
                
        if total_valid_frames == 0: 
            return {"is_using_phone": False, "score": 0.0}
        
        ratio = phone_detected_frames / total_valid_frames
        print(f"📊 PHONE RATIO: {ratio} ({phone_detected_frames}/{total_valid_frames})")
        
        return {
            "is_using_phone": ratio >= self.TRIGGER_RATIO,
            "score": round(ratio, 4)
        }