import math

def euclidean_distance(p1, p2):
    return math.sqrt((p2[0] - p1[0])**2 + (p2[1] - p1[1])**2)

def compute_ear(eye_points):
    """
    Computes Eye Aspect Ratio (EAR) given 6 points of the eye contour.
    The EAR formula dynamically checks the vertical vs horizontal size of the eye.
    """
    if not eye_points or len(eye_points) != 6:
        return 0.0
        
    # Vertical distances (top eyelids to bottom eyelids)
    v1 = euclidean_distance(eye_points[1], eye_points[5])
    v2 = euclidean_distance(eye_points[2], eye_points[4])
    
    # Horizontal distance (corner to corner)
    h = euclidean_distance(eye_points[0], eye_points[3])
    
    if h == 0:
        return 0.0
        
    ear = (v1 + v2) / (2.0 * h)
    return ear
    
def compute_eye_closure(left_eye_points, right_eye_points, threshold=0.25):
    """
    Returns True if eyes are considered closed based on EAR threshold.
    Also returns the average EAR.
    """
    if not left_eye_points or not right_eye_points:
        return False, 0.0
        
    left_ear = compute_ear(left_eye_points)
    right_ear = compute_ear(right_eye_points)
    
    avg_ear = (left_ear + right_ear) / 2.0
    
    is_closed = avg_ear < threshold
    return is_closed, avg_ear
