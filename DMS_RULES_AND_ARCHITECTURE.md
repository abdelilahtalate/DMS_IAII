# DriveMind AI (DMS) - Architecture & Development Rules

This document serves as the master guide for the DMS project, covering service connectivity, Android patterns, and the "Big Rule" for feature expansion.

---

## 1. Multi-Service Architecture
The system is a distributed network of 4 primary services working in orchestration.

| Service | Technology | Role | Port |
| :--- | :--- | :--- | :--- |
| **Android App** | Kotlin, Compose, MVVM | Driver Interface | N/A |
| **Android Backend** | FastAPI | Central Orchestrator | 8000 |
| **AI Backend** | FastAPI, MediaPipe, YOLO | Intelligence Processor | 8002 |
| **IoT Simulation** | Python, FastAPI | Hardware Source | 8001 |

### Data Connectivity Flow
1. **Command Flow**: App triggers Backend -> Backend triggers IoT Sim -> IoT Sim starts Camera.
2. **Video Flow**: IoT Sim streams raw frames via **WebSocket** to AI Backend.
3. **Intelligence Flow**: AI Backend returns scores (JSON) to IoT Sim via WebSocket.
4. **Broadcast Flow**: IoT Sim updates Backend (REST) -> Backend broadcasts scores to App (WebSocket).

---

## 2. Android MVVM Standards
The Android application follows **Feature-Based Clean Architecture**.

- **Core Module**: Shared logic (`network`, `navigation`, `ui.theme`).
- **Feature Modules** (`auth`, `home`, `drive`, `device`):
    - **Data**: Repositories for API/WebSocket management.
    - **Domain**: UseCases for single-purpose business logic.
    - **Presentation**: UI (Compose Screens) + Logic (ViewModels with `UiState`).

---

## 3. Mandatory Development Rules

### Android Rules
1. **Immutability**: ViewModels must only expose `StateFlow<UiState>`.
2. **Visual Consistency**: All screens must use `DriveMindBackground` and colors from `LocalAppColors.current`.
3. **Error Handling**: Every network call or stream subscription must include a `.catch {}` block to prevent app crashes on connection loss.
4. **Memory Management**: When handling MJPEG streams, use bitmap pooling/reusable bitmaps to avoid performance lag.

### Backend Rules
1. **Orchestration**: The `android_backend` is the source of truth for the App. It should never process AI; it only routes data.
2. **AI Modularization**: Every AI model must inherit from `BaseTemporalDetector` to ensure sliding-window consistency.

---

## 4. The "BIG RULE" for Feature Expansion
To add a new feature (e.g., *Phone Usage Detection*), follow this exact sequence:

1. **AI Layer**: Create a detector in `backend_AI/ai/` and register it in `Aggregator.py`.
2. **Data Layer**: Update the `AiResult` and `AiResultWrapper` models in the Android app to match the new JSON keys.
3. **Domain Layer**: (Optional) Add a UseCase if the feature requires manual triggering.
4. **UI Layer**: Update the `HomeScreen` to include a new `StatusChip` or alert based on the new metric.

> [!IMPORTANT]
> **Intelligence flows from the AI Backend upwards.** Never implement AI logic or thresholds inside the Android app. The app only visualizes what the AI Backend decides.

---
*Created on 2026-05-05 by Antigravity AI Assistant.*
