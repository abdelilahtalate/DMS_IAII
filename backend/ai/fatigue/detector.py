from ai.BaseTemporalDetector import BaseTemporalDetector
from ai.fatigue.eye_closure import compute_eye_closure

class FatigueDetector(BaseTemporalDetector):
    """
    Fatigue detector that utilizes Eye Aspect Ratio (EAR).
    Uses the buffer to analyze the latest frame's eye state.
    """
    def __init__(self, buffer_size: int = 30, stride: int = 5):
        super().__init__(buffer_size=buffer_size, stride=stride)

    def compute(self):
        # For simplicity, we just look at the most recent frame in our buffer.
        # (A more advanced model would count the number of closed frames over the buffer window!)
        latest_frame = self.buffer[-1]
        
        # We pass the landmarks provided correctly from the preprocessor
        is_closed, avg_ear = compute_eye_closure(
            latest_frame.left_eye_points, 
            latest_frame.right_eye_points,
            threshold=0.25
        )
        
        status = "CLOSED" if is_closed else "OPEN"
        return f"Eyes {status} (EAR: {avg_ear:.2f})"
