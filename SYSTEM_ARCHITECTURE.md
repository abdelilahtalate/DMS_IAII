# DriveMind DMS: Complete System Architecture & Orchestration Guide

This document presents the complete architectural layout, logical workflows, and data relationships for the **DriveMind Driver Monitoring System (DMS)**. It details how the **Android Mobile App**, **Android Backend**, **IoT Device Simulator**, and **AI Processing Backend** coordinate to process low-latency, real-time safety telemetry.

---

## 1. System-Wide Component Topology

The system comprises four decoupled, concurrent nodes that communicate over asynchronous network protocols (HTTP REST and WebSockets):

```mermaid
graph TB
    subgraph Android App [Android Mobile Application]
        UI[Jetpack Compose View] <--> VM[ViewModel]
        VM <--> UC[Use Case]
        UC <--> Repo[Repository]
        Repo <--> Net[Retrofit OkHttp API]
        Repo <--> WS_Client[WebSocket Client]
        Repo <--> GPS[Location Manager]
    end

    subgraph Port 8000 [Android Backend (FastAPI)]
        Router[API Routers] <--> Schema[Pydantic Schemas]
        Router <--> Serv[Services Layer]
        Serv <--> DB_Model[SQLAlchemy Models]
        DB_Model <--> SQLite[(SQLite Database)]
        Serv <--> Integr[IoT Integration Client]
        Router <--> Cache[In-Memory latest_scores Cache]
        Router <--> WS_Stream[WebSocket Stream Router]
    end

    subgraph Port 8001 [IoT Device Simulator (FastAPI)]
        Camera[Camera / Frames Simulator]
        WS_Sender[WebSocket Sender]
        Control[HTTP Control APIs]
    end

    subgraph Port 8002 [AI Backend (FastAPI)]
        AI_WS[WebSocket Stream Endpoint] <--> Prep[Preprocessor]
        Prep <--> Mesh[MediaPipe FaceMesh & Hands]
        Prep <--> Agg[Aggregator]
        Agg <--> Yol[YOLOv8 Object Detection]
        Agg <--> Temp[Temporal Stateful Models]
    end

    %% Network Connections
    Net <-->|REST API over HTTP| Router
    WS_Client <-->|Asynchronous Score Stream| WS_Stream
    Integr <-->|HTTP Device Control| Control
    Camera -->|Raw Frame Stream| WS_Sender
    WS_Sender <-->|Raw JPEG Frame Bytes| AI_WS
    AI_WS -->>|Live Fatigue & Distraction Scores| WS_Sender
    WS_Sender -->|POST AI Scores /ai/update| Router
    GPS -->|GPS Telemetry batch log| Router
```

---

## 2. End-to-End Orchestration Loop & Logical Sequence

The diagram below details the entire system lifecycle—from device pairing and session start to frame-by-frame deep-learning inference, audio alerts, and telemetry persistence:

```mermaid
sequenceDiagram
    autonumber
    actor Driver as Driver (Physical Actions)
    participant Android as Android App (Kotlin)
    participant Backend as Android Backend (FastAPI:8000)
    participant IoT as IoT Device Simulator (Port 8001)
    participant AI as AI Backend (Port 8002)

    Note over Driver, AI: PHASE 1: SYSTEM BOOTSTRAP & PAIRING
    Android->>Backend: POST /pairing/pair (user_email, device_id, secret)
    Backend->>IoT: POST /pair (credentials verification)
    IoT-->>Backend: Verification status (Success/Fail)
    alt Device is valid
        Backend->>Backend: Exclusively map device to user in SQLite
        Backend->>Backend: Disconnect other active devices for this user
        Backend-->>Android: Status 200 (Success) & Video Preview URL
    else Device credentials invalid
        Backend-->>Android: HTTP 400 (Failed)
    end

    Note over Driver, AI: PHASE 2: SESSION STARTUP
    Android->>Backend: POST /sessions (start driving session)
    Backend->>Backend: Write session record in DB (status = 'active')
    Backend-->>Android: Returns session_id
    Android->>Backend: POST /ai/start
    Backend->>IoT: GET /start-analysis
    IoT-->>Backend: Status OK (Camera sensor powered on)
    Backend-->>Android: Safety monitoring started

    Note over Driver, AI: PHASE 3: COMPUTER VISION & DEEP LEARNING (30 FPS LOOP)
    IoT->>IoT: Ingest raw camera frames (30fps)
    IoT->>AI: WebSocket binary stream (Raw JPEG Bytes)
    AI->>AI: Preprocess, decode, & resize (640x480)
    AI->>AI: Run MediaPipe FaceMesh & Hands tracking
    alt Multi-Face detected (Passengers)
        AI-->>IoT: Empty scores (face_detected = false)
    else Exactly 1 Face
        AI->>AI: Calculate EAR, MAR, blink-rate, and 3D Head Pose
        AI->>AI: Run YOLOv8 on CPU/GPU to identify phone usage (Class 77)
        AI->>AI: Run weighted aggregator focus scoring formula
        AI-->>IoT: Safety metrics (fatigue, distraction, phone, focus)
    end
    IoT->>Backend: POST /api/v1/ai/update (forward scores)
    Backend->>Backend: Update thread-safe latest_scores cache
    Android->>Backend: WS Connection (/stream_analytics)
    loop Asynchronous real-time score updates
        Backend->>Backend: Check if latest_scores changed (diff-check)
        Backend-->>Android: Stream live JSON scores
        alt Focus Score Dropped / Distraction Detected
            Android->>Android: Trigger SoundManager Auditory Alerts & Auditory Haptics
            Android->>Backend: POST /sessions/alerts (log violation with GPS)
            Backend->>Backend: Insert Alert record in DB
        end
    end

    Note over Driver, AI: PHASE 4: GPS TELEMETRY BATCH LOGGING
    Android->>Android: Track GPS locations on a background thread
    Android->>Backend: POST /sessions/locations (Batch GPS telemetry upload)
    Backend->>Backend: Execute bulk_save_objects() in database

    Note over Driver, AI: PHASE 5: DRIVING TEARDOWN
    Android->>Backend: POST /ai/stop
    Backend->>IoT: GET /stop-analysis (turn off camera sensor)
    Android->>Backend: POST /sessions/stop
    Backend->>Backend: Mark DrivingSession as 'completed'
    Backend->>Backend: Calculate final trip focus averages
```

---

## 3. Database Entity-Relationship Diagram (ERD)

The database schema (`drivemind.db` SQLite) maps the relational telemetry that links users, physical paired devices, driving logs, GPS tracking points, and safety alerts:

```mermaid
erDiagram
    users {
        int id PK
        string full_name
        string email UK
        string hashed_password
        string gender
        int age
    }

    devices {
        int id PK
        string device_id UK
        int user_id FK
        string secret
        boolean paired
    }

    sessions {
        int id PK
        int user_id FK
        int device_id FK
        datetime start_time
        datetime end_time
        int total_distance
        int average_focus
        string status
    }

    alerts {
        int id PK
        int session_id FK
        string alert_type
        float latitude
        float longitude
        datetime timestamp
    }

    location_points {
        int id PK
        int session_id FK
        float latitude
        float longitude
        float speed
        datetime timestamp
    }

    user_settings {
        int id PK
        int user_id FK
        boolean soundEnabled
        boolean vibrationEnabled
        float sensitivity
        string theme
    }

    users ||--o{ sessions : "drives"
    users ||--o{ devices : "owns"
    users ||--|| user_settings : "configures"
    devices ||--o{ sessions : "monitors"
    sessions ||--o{ alerts : "triggers"
    sessions ||--o{ location_points : "records"
```

---

## 4. Logical Interaction Breakdown

The system represents a decoupled orchestrator architecture where nodes have explicit roles and zero logical leakage:

1. **Active Telemetry Ingestion (Fast Ingress)**:
   The Android Mobile App runs a background GPS service utilizing [LocationRepository.kt](file:///c:/Users/crasy/OneDrive/Bureau/s2%20ia2i/mobile/dms_ba/DMS/android/app/src/main/java/com/example/dms_app/core/location/LocationRepository.kt). Telemetry data is saved locally and periodically uploaded in batches (`POST /api/v1/sessions/locations`) to prevent network congestion.
   
2. **Video Processing & Deep Learning Pipeline**:
   The physical IoT device acts as the edge node. It captures camera video frames and streams raw JPEG bytes to `backend_AI` (Port `8002`). The AI engine extracts landmarks (MediaPipe) and evaluates object classes (YOLOv8) asynchronously.
   
3. **Reactive WebSocket Scores Broker**:
   Once safety evaluations are made, the IoT edges push results to `android_backend` (Port `8000`) at `/ai/update`. The backend acts as a low-latency broker by immediately distributing the scores to the active Android app over a WebSocket stream (`ws://.../stream_analytics`), ensuring real-time driver alert feedback.
