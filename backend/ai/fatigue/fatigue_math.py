import numpy as np

def get_eye_aspect_ratio(eye_points) -> float:
    """
    Calculates Eye Aspect Ratio (EAR).
    Your preprocess provides 6 points per eye: 
    [outer, top1, top2, inner, bottom1, bottom2]
    """
    if not eye_points or len(eye_points) < 6:
        return 0.3  # Safe default (open eye)
    
    p0 = np.array(eye_points[0]) # Outer
    p1 = np.array(eye_points[1]) # Top 1
    p2 = np.array(eye_points[2]) # Top 2
    p3 = np.array(eye_points[3]) # Inner
    p4 = np.array(eye_points[4]) # Bottom 1
    p5 = np.array(eye_points[5]) # Bottom 2
    
    # Vertical distances
    vert1 = np.linalg.norm(p2 - p4) 
    vert2 = np.linalg.norm(p1 - p5) 
    
    # Horizontal distance
    horiz = np.linalg.norm(p0 - p3)
    
    if horiz == 0:
        return 0.3
        
    return (vert1 + vert2) / (2.0 * horiz)


def get_mouth_aspect_ratio(mouth_points) -> float:
    """
    Calculates Mouth Aspect Ratio (MAR).
    Your preprocess provides 4 points: 
    [top_lip, bottom_lip, left_corner, right_corner]
    """
    if not mouth_points or len(mouth_points) < 4:
        return 0.0
        
    top = np.array(mouth_points[0])
    bottom = np.array(mouth_points[1])
    left = np.array(mouth_points[2])
    right = np.array(mouth_points[3])
    
    vert = np.linalg.norm(top - bottom)
    horiz = np.linalg.norm(left - right)
    
    if horiz == 0:
        return 0.0
        
    return vert / horiz