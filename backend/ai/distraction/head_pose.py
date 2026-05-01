import cv2
import numpy as np

def get_head_pose_distraction_score(ctx, yaw_thresh=30.0, pitch_thresh=25.0, roll_thresh=20.0) -> float:
    """
    High precision 3D head pose using solvePnP and stable RQ decomposition.
    """
    if not ctx.face_detected or not hasattr(ctx, 'nose_tip') or ctx.nose_tip is None:
        return 0.0
        
    try:
        # Extract from your preprocess arrays
        left_eye_outer = np.array(ctx.left_eye_points[3]) # idx 263
        right_eye_outer = np.array(ctx.right_eye_points[3]) # idx 33
        left_mouth = np.array(ctx.mouth_points[2]) # idx 78
        right_mouth = np.array(ctx.mouth_points[3]) # idx 308
        
        nose = np.array(ctx.nose_tip) # idx 1
        chin = np.array(ctx.chin)     # idx 152
        
        h, w = ctx.raw_frame.shape[:2]
        
        # 1. Map 2D image points (Order matches model_points exactly)
        image_points = np.array([
            nose, left_eye_outer, right_eye_outer, left_mouth, right_mouth, chin
        ], dtype=np.float64)
        
        # 2. Standard 3D human head model
        model_points = np.array([
            [0.0, 0.0, 0.0],        # Nose
            [-0.15, -0.05, 0.0],    # Left eye (image left)
            [0.15, -0.05, 0.0],     # Right eye (image right)
            [-0.1, 0.15, 0.0],      # Left mouth (image left)
            [0.1, 0.15, 0.0],       # Right mouth (image right)
            [0.0, 0.25, 0.0]        # Chin
        ], dtype=np.float64)
        
        # 3. Camera matrix
        focal_length = w
        camera_matrix = np.array([
            [focal_length, 0, w / 2],
            [0, focal_length, h / 2],
            [0, 0, 1]
        ], dtype=np.float64)
        dist_coeffs = np.zeros((4, 1))
        
        # 4. Solve PnP
        success, r_vec, t_vec = cv2.solvePnP(
            model_points, image_points, camera_matrix, dist_coeffs, 
            flags=cv2.SOLVEPNP_ITERATIVE
        )
        
        if not success: return 0.0
        
        # 5. Get Rotation Matrix
        r_mat, _ = cv2.Rodrigues(r_vec)
        
        # ==========================================
        # THE FIX: Use OpenCV's stable decomposition
        # ==========================================
        # cv2.RQDecomp3x3 returns: (retVal, mtxR, mtxQ, Qx, Qy, Qz)
        # Qx = Pitch (looking up/down)
        # Qy = Yaw   (looking left/right)
        # Qz = Roll  (tilting head)
        _, _, _, pitch, yaw, roll = cv2.RQDecomp3x3(r_mat)
        
        # 6. Normalize to 0.0 - 1.0
        yaw_score = min(abs(yaw) / yaw_thresh, 1.0)
        pitch_score = min(abs(pitch) / pitch_thresh, 1.0)
        roll_score = min(abs(roll) / roll_thresh, 1.0)
        print("yaw_score", yaw_score)
        print("pitch_score", pitch_score)
        print("roll_score", roll_score)
        # Debug prints (you can remove these once confirmed working)
        # print(f"Yaw: {yaw:.1f} ({yaw_score:.2f}) | Pitch: {pitch:.1f} ({pitch_score:.2f}) | Roll: {roll:.1f} ({roll_score:.2f})")
        
        return max(yaw_score, pitch_score, roll_score)
        
    except Exception:
        return 0.0 