creat enviroment:
    python -m venv .venv
    .venv\Scripts\activate
    pip install -r backend/requirements.txt
    uvicorn backend.main:app --reload --host [IP_ADDRESS] --port 8000

than run this in another terminal:
    python camera/camera_tempo_webcam.py
    it will open the camera of ur pc jsut for testing until esp32 come 