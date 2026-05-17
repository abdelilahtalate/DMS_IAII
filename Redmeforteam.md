Setuping 
there is 3 server side (android_backend / backend_ai / iot_sumilator)

each server side had his own venv puthon
so creat 3 venv for each directory 

    /iot_simulator  --> python -m venv esp32  ---> esp32/Scripts/activate -->   python -m uvicorn main:app --host 0.0.0.0 --port 8001
    
    /backend_ai --> python -m venv AIenv --> AIenv/Scripts/activate---> python -m uvicorn main:app --host 0.0.0.0 --port 8002
    
    /android_backend ---> python -m venv Andrenv -->Andrenv/Scripts/activate---> python -m uvicorn main:app --host 0.0.0.0 --port 8000

🚨🚨🚨be carefull 🚨🚨🚨
each port had his own port dont mixed btween them each microservice with his right port 


### 2. 📱 Android App Setup
The Android app is built with **Jetpack Compose** and **Material 3**.

1.  **Open the project:**
    Open Android Studio and select the `DMS/android` folder.
2.  **Configure the Server IP:**
    *   Find your computer's local IP address (e.g., `192.168.1.15`).
    *   Open `com.example.dms_app.core.config.AppConfig.kt`.
    *   Update the `HOST` variable to your computer's IP.
3.  **Sync & Build:**
    Click **Sync Project with Gradle Files** and wait for completion.
4.  **Run:**
    Deploy to a physical Android device or an emulator.
