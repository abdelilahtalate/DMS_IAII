import cv2
import numpy as np

class GazeSmoother:
    """Smooths the gaze magnitude over a tiny window to prevent single-frame flickers"""
    def __init__(self, size=3):
        self.buffer = []
        self.size = size
        
    def smooth(self, val):
        self.buffer.append(val)
        if len(self.buffer) > self.size: self.buffer.pop(0)
        return np.mean(self.buffer)

def get_gaze_distraction_score(ctx, smoother, gaze_thresh=0.35) -> float:
    """
    High precision gaze using Ellipse fitting and Angle Compensation.
    """
    if not ctx.face_detected or not ctx.left_eye_points or not ctx.iris_points:
        return 0.0
    
    def _calc_single_gaze(eye_pts, iris_pts):
        contour = np.array(eye_pts, dtype=np.float32)
        iris = np.array(iris_pts, dtype=np.float32)
        
        if len(contour) < 5 or len(iris) == 0: return 0.0
        
        try:
            ellipse = cv2.fitEllipse(contour)
            (ex, ey), (ewidth, eheight), angle = ellipse
            iris_center = np.mean(iris, axis=0)
            
            offset_x = iris_center[0] - ex
            offset_y = iris_center[1] - ey
            
            # ANGLE COMPENSATION (Cancels out head tilt perfectly)
            angle_rad = -np.radians(angle)
            cos_a, sin_a = np.cos(angle_rad), np.sin(angle_rad)
            comp_x = offset_x * cos_a - offset_y * sin_a
            comp_y = offset_x * sin_a + offset_y * cos_a
            
            norm_x = comp_x / (ewidth / 2.0) if ewidth > 0 else 0.0
            norm_y = comp_y / (eheight / 2.0) if eheight > 0 else 0.0
            
            h = np.clip(norm_x, -1, 1)
            v = np.clip(norm_y, -1, 1)
            
            return np.sqrt(h**2 + v**2)
        except cv2.error:
            return 0.0

    # Your preprocess splits iris into 4 left, 4 right
    l_mag = _calc_single_gaze(ctx.left_eye_points, ctx.iris_points[:4])
    r_mag = _calc_single_gaze(ctx.right_eye_points, ctx.iris_points[4:])
    
    avg_mag = (l_mag + r_mag) / 2
    
    # Smooth it before thresholding to stop jitter
    smoothed_mag = smoother.smooth(avg_mag)
    
    return min(smoothed_mag / gaze_thresh, 1.0)