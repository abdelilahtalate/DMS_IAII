import cv2

def open_camera(camera_id=0):
    """
    Opens the webcam and returns the capture object.
    """
    cap = cv2.VideoCapture(camera_id)
    
    if not cap.isOpened():
        raise RuntimeError("Cannot open camera")
    
    return cap


def get_frame(cap):
    """
    Reads one frame from the camera.
    Returns: (ret, frame)
    """
    ret, frame = cap.read()
    return ret, frame