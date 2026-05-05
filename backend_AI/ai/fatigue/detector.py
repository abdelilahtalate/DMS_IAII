import time
from ai.BaseTemporalDetector import BaseTemporalDetector
from .fatigue_math import get_eye_aspect_ratio, get_mouth_aspect_ratio

class FatigueDetector(BaseTemporalDetector):
    """
    Temporal Fatigue Detector.
    Inherits sliding window from BaseTemporalDetector.
    """
    def __init__(self, buffer_size: int = 90, stride: int = 1):
        """
        Args:
            buffer_size: 90 frames = ~3 seconds at 30fps. Needed to detect 2-3s closed eyes.
            stride: MUST be 1 for fatigue (can't skip frames or you'll miss blinks).
        """
        super().__init__(buffer_size=buffer_size, stride=stride)
        
        # Thresholds
        self.EAR_THRESH = 0.22        # Standard drowsiness threshold
        self.MAR_THRESH = 0.6         # Yawn threshold (might need slight tuning)
        self.CLOSED_EYE_TIME = 2.0    # Seconds eyes must be closed
        
        # ==========================================
        # STATEFUL VARIABLES FOR BLINK RATE
        # ==========================================
        self.was_eyes_closed = False
        self.blink_timestamps = []     # Stores exact times of blinks
        self.BLINK_WINDOW_SEC = 5.0    # Evaluate blinks over last 5 seconds
        self.MIN_BLINKS_IN_WINDOW = 1 # < 1 blink in 5s = fatigue

    def compute(self) -> dict:
        if not self.buffer: return {"is_fatigued": False, "score": 0.0}
        
        current_time = time.time()
        
        # Variables for this specific compute cycle
        closed_duration_score = 0.0
        yawn_score = 0.0
        blink_score = 0.0
        
        closed_frames_count = 0
        yawn_frames_count = 0
        
        # 1. Iterate through sliding window
        for ctx in self.buffer:
            if not ctx.face_detected or not ctx.left_eye_points:
                continue
                
            # --- A. Eye Aspect Ratio ---
            left_ear = get_eye_aspect_ratio(ctx.left_eye_points)
            right_ear = get_eye_aspect_ratio(ctx.right_eye_points)
            avg_ear = (left_ear + right_ear) / 2.0
            
            is_closed = avg_ear < self.EAR_THRESH
            
            # Count CONTINUOUS closed frames (must be recent frames)
            if is_closed:
                closed_frames_count += 1
            else:
                break # If we hit an open eye in the past, stop counting duration
                
            # --- B. Blink Detection (State Machine) ---
            # A blink is a transition from CLOSED -> OPEN
            if is_closed:
                self.was_eyes_closed = True
            elif self.was_eyes_closed and not is_closed:
                # The eyes JUST opened after being closed! That's 1 blink.
                self.blink_timestamps.append(ctx.timestamp)
                self.was_eyes_closed = False
                
            # --- C. Yawn Detection ---
            mar = get_mouth_aspect_ratio(ctx.mouth_points)
            if mar > self.MAR_THRESH:
                yawn_frames_count += 1
                
        # ==========================================
        # 2. CALCULATE FINAL SCORES
        # ==========================================
        
        # Score A: Closed Eyes Duration (Time-based)
        # Estimate FPS from buffer to convert frames to seconds
        if len(self.buffer) > 1:
            time_span = self.buffer[-1].timestamp - self.buffer[0].timestamp
            estimated_fps = len(self.buffer) / max(time_span, 0.1)
        else:
            estimated_fps = 30
            
        closed_duration_sec = closed_frames_count / estimated_fps
        
        if closed_duration_sec >= self.CLOSED_EYE_TIME:
            closed_duration_score = 1.0  # Max fatigue (Micro-sleep)
        else:
            closed_duration_score = closed_duration_sec / self.CLOSED_EYE_TIME
            
        # Score B: Yawning (Ratio-based)
        valid_frames_in_buffer = len([1 for c in self.buffer if c.face_detected])
        if valid_frames_in_buffer > 0:
            yawn_ratio = yawn_frames_count / valid_frames_in_buffer
            if yawn_ratio > 0.3: # Yawned for 30% of the window
                yawn_score = 1.0
            else:
                yawn_score = yawn_ratio / 0.3
                
        # Score C: Blink Rate (Time-based)
        # Clean out old blinks outside our 5-second window
        self.blink_timestamps = [t for t in self.blink_timestamps if (current_time - t) < self.BLINK_WINDOW_SEC]
        recent_blinks = len(self.blink_timestamps)
        
        if recent_blinks <= self.MIN_BLINKS_IN_WINDOW:
            blink_score = 1.0  # Eyes are drooping, not blinking
        elif recent_blinks < 3: # Slightly low
            blink_score = 0.5
        else:
            blink_score = 0.0  # Normal blinking
            
        # ==========================================
        # 3. MERGE AND RETURN
        # ==========================================
        # Weighted merge: Closed eyes is the most dangerous fatigue indicator
        final_score = (closed_duration_score * 0.5) + (yawn_score * 0.3) + (blink_score * 0.2)
        
        return {
            "is_fatigued": final_score >= 0.6, # Trigger if 60% fatigued
            "score": round(final_score, 4)
        }