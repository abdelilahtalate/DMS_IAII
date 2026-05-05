from ai.BaseTemporalDetector import BaseTemporalDetector
from .head_pose import get_head_pose_distraction_score
from .gaze_direction import get_gaze_distraction_score, GazeSmoother

class DistractDetector(BaseTemporalDetector):
    def __init__(self, buffer_size: int = 30, stride: int = 5,
             final_trigger_ratio: float = 0.4,
             frame_thresh: float = 0.2):   # was hardcoded 0.8 inside compute()
        super().__init__(buffer_size=buffer_size, stride=stride)
        self.final_trigger_ratio = final_trigger_ratio
        self.frame_thresh = frame_thresh
        self.gaze_smoother = GazeSmoother(size=3)  # kept for size reference only

    def compute(self) -> dict:
        valid_frames = 0
        distracted_frames = 0
        local_gaze_smoother = GazeSmoother(size=self.gaze_smoother.size)
        for ctx in self.buffer:
            if not ctx.face_detected:
                continue
                
            valid_frames += 1
            
            # 1. Get precision scores
            hp_score = get_head_pose_distraction_score(ctx)
            # print("hp_score", hp_score)
            gaze_score = get_gaze_distraction_score(ctx, local_gaze_smoother)
            # print("gaze_score", gaze_score)
            # 2. Combine scores (If EITHER is highly suspicious, flag the frame)
            # Using max() means looking far sideways OR turning head triggers it.
            # Using average requires BOTH to agree.
            frame_score = 0.6 * hp_score + 0.4 * gaze_score
            
            # 3. Threshold this specific frame
            if frame_score >= self.frame_thresh:  # configurable, not hardcoded 0.8
                distracted_frames += 1  # Must be 80% distracted on this frame to count it
                
        if valid_frames == 0:
            return {"is_distracted": False, "score": 0.0}
            
        ratio = distracted_frames / valid_frames
        
        return {
            "is_distracted": ratio >= self.final_trigger_ratio,
            "score": round(ratio, 4)
        }
        