import cv2
import threading
import time

latest_frame = None
cap = None
_running = False
_thread = None

def capture_loop():
    global latest_frame, cap, _running
    
    while _running:
        if cap is None or not cap.isOpened():
            print("[Camera] Attempting to open camera...")
            cap = cv2.VideoCapture(0)
            if not cap.isOpened():
                print("[Camera] Failed to open camera. Retrying in 5 seconds...")
                time.sleep(5)
                continue
            print("[Camera] Camera opened successfully.")

        ret, frame = cap.read()
        if ret:
            latest_frame = frame
        else:
            print("[Camera] Failed to capture frame. Camera might have been disconnected.")
            if cap:
                cap.release()
            cap = None
            time.sleep(1)
        
        time.sleep(0.01)
    
    # Cleanup when stopping
    if cap:
        cap.release()
        cap = None
    latest_frame = None
    print("[Camera] Camera stopped and released.")


def start_camera():
    global _running, _thread
    if _running:
        print("[Camera] Camera already running.")
        return
    
    _running = True
    _thread = threading.Thread(target=capture_loop, daemon=True)
    _thread.start()
    print("[Camera] Capture thread started.")

def stop_camera():
    global _running
    _running = False
    print("[Camera] Stopping camera...")