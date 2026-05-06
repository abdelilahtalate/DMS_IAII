# Android Backend LLM Coding Rules

Use this file as the mandatory instruction set for any LLM or developer editing `android_backend`. The goal is to keep the FastAPI backend scalable, flexible, easy to modify, and consistent with the existing service-oriented structure.

## Project Identity

- Service role: Android-facing backend and orchestrator for DriveMind AI
- Framework: FastAPI
- Database: SQLAlchemy with SQLite by default
- API version: `/api/v1`
- Entry point: `app/main.py`
- Router aggregator: `app/api/v1/api.py`
- Runtime port: `8000`

This backend should coordinate the Android app, database, IoT simulation, and AI result stream. It should not contain computer vision, model inference, or heavy AI processing.

## Golden Rule

Never put all logic inside a route function. Routes receive requests, validate schemas, call services or integrations, and return responses. Services own business and database logic. Integrations own calls to external services such as `iot_simulation`.

Expected flow:

```text
Route -> Schema validation -> Service -> Model/DB or Integration -> Response
```

For external service calls:

```text
Route -> Service -> Integration client -> IoT/AI service
```

## Folder Contract

Keep this structure:

```text
android_backend/
  app/
    api/
      v1/
        api.py
        routes/
    core/
    db/
    integrations/
    models/
    schemas/
    services/
    utils/
    main.py
    requirement.txt
```

Responsibilities:

- `api/v1/routes`: FastAPI route handlers only
- `api/v1/api.py`: includes and tags all v1 routers
- `schemas`: Pydantic request/response models
- `models`: SQLAlchemy database models
- `services`: business logic and database operations
- `integrations`: HTTP/WebSocket clients for other services
- `db`: SQLAlchemy engine, session, base imports
- `core`: settings, config, security, shared infrastructure
- `utils`: small shared helpers

Do not place project code inside virtual environment folders such as `venv` or `android_env`.

## Route Rules

Files belong in:

```text
app/api/v1/routes/
```

Required rules:

- Use `APIRouter`.
- Keep route functions thin.
- Validate input using Pydantic schemas from `app/schemas`.
- Use `Depends(get_db)` for database sessions when a route needs the database.
- Call a service class for business/database logic.
- Call an integration client only for very small pass-through endpoints. If orchestration grows, move it into a service.
- Raise `HTTPException` for API errors instead of returning raw `{"error": ...}` when the route controls the response.
- Keep route paths stable because the Android app depends on them.
- Register every new router in `app/api/v1/api.py`.

Example pattern:

```python
@router.post("/start")
def start_session(data: SessionStart, db: Session = Depends(get_db)):
    result = drive_service.start_session(db, data)
    if isinstance(result, dict) and "error" in result:
        raise HTTPException(status_code=404, detail=result["error"])
    return result
```

## Service Rules

Files belong in:

```text
app/services/
```

Required rules:

- Service files end with `_service.py` unless they are a simple store/helper already following another local pattern.
- Service classes end with `Service`.
- Services own business rules.
- Services perform database queries and commits.
- Services receive a SQLAlchemy `Session` from the route when database work is needed.
- Prefer dependency injection through method arguments over creating global database sessions inside services.
- If existing service code manually opens `SessionLocal`, keep behavior working, but new database code should prefer `Depends(get_db)` in the route and pass `db` into the service.
- Commit only after a complete valid change.
- Roll back or avoid partial writes when adding multi-step operations.
- Do not import FastAPI route decorators in services.
- Do not perform AI model inference in services.

Good ownership examples:

- `AuthService`: signup, login, password verification, token creation
- `DeviceService`: device verification, pairing status, ownership
- `DriveService`: driving session, location logging, alert logging

## Schema Rules

Files belong in:

```text
app/schemas/
```

Required rules:

- Use Pydantic `BaseModel`.
- Request/response DTOs belong here, not in route files.
- Name request schemas by action: `SignupRequest`, `LoginRequest`, `SessionStart`, `LocationCreate`, `AlertCreate`.
- Use precise types: `EmailStr`, `Optional[int]`, `float`, `datetime`, etc.
- Do not put SQLAlchemy models in schemas.
- Do not put database sessions, service calls, or external HTTP calls in schemas.
- Add response schemas for stable public endpoints when the response is important to Android.

## Model Rules

Files belong in:

```text
app/models/
```

Required rules:

- SQLAlchemy models inherit from `Base`.
- Every model must define `__tablename__`.
- Keep database column names stable unless you also handle migration/data compatibility.
- Relationships should use `back_populates` consistently.
- Import every new model in `app/db/base.py` so table creation sees it.
- Do not put API validation rules or route logic inside SQLAlchemy models.

Existing model domains:

- `User`
- `Device`
- `DrivingSession`
- `Alert`
- `LocationPoint`

## Database Rules

- Use `app/db/session.py` for engine, `SessionLocal`, `Base`, and `get_db`.
- Use `Depends(get_db)` in routes that need database access.
- Pass `db: Session` into services.
- Do not create multiple engines in feature files.
- Do not hardcode database URLs outside `core/settings.py`.
- Keep SQLite thread settings in `db/session.py`.
- Do not commit generated database files such as `drivemind.db` unless the team intentionally wants seed data tracked.
- There is no migration system in the current project. Be careful with model changes because `Base.metadata.create_all()` does not safely migrate existing tables.

## Integration Rules

Files belong in:

```text
app/integrations/
```

Required rules:

- Put external service clients here, such as `IoTClient`.
- Keep external base URLs configurable through `core/settings.py` or `core/config.py`.
- Do not hardcode IP addresses or ports in route files.
- Add timeouts to HTTP calls when possible.
- Catch connection errors and return/raise clear errors.
- Do not let integration clients know about FastAPI route objects.
- Do not put Android response formatting inside integration clients; format final API responses in services or routes.

Current service boundary:

- `android_backend` talks to `iot_simulation` on port `8001`.
- `backend_AI` owns AI/model logic on port `8002`.
- Android app talks to `android_backend` on port `8000`.

## AI And Streaming Rules

This backend is the orchestrator. It may start/stop analysis and forward results, but it must not implement detection algorithms.

Allowed:

- Start analysis by calling IoT/integration endpoints.
- Stop analysis by calling IoT/integration endpoints.
- Receive latest AI scores from another service.
- Stream latest AI scores to Android over WebSocket.
- Store session, alert, and location history.

Forbidden:

- Do not add OpenCV, MediaPipe, YOLO, or model inference here.
- Do not calculate fatigue/distraction from raw frames here.
- Do not put camera frame processing in route handlers.
- Do not make Android call `backend_AI` directly if `android_backend` is meant to orchestrate that flow.

## API Contract Rules

The Android app depends on these existing endpoint shapes:

```text
POST /api/v1/auth/signup
POST /api/v1/auth/login
POST /api/v1/pairing/pair
POST /api/v1/ai/start
POST /api/v1/ai/stop
WebSocket /api/v1/ai/stream_analytics
```

Rules:

- Do not rename or remove these routes without updating Android.
- Keep response keys stable, especially `access_token`, `message`, `device`, `device_id`, and `preview_stream`.
- Add new fields in a backward-compatible way when possible.
- Use clear HTTP status codes for new APIs.
- Keep API versioning under `/api/v1`.

## Configuration Rules

Configuration belongs in:

```text
app/core/settings.py
app/core/config.py
```

Rules:

- Read environment variables in `settings.py`.
- Expose app constants through `Config` only when needed.
- Do not hardcode secrets in production code.
- Move `SECRET_KEY`, token expiry, IoT base URL, and other deployment values to settings when touching security/integration code.
- Keep ports consistent with the project:
  - Android backend: `8000`
  - IoT simulation: `8001`
  - AI backend: `8002`

## Security Rules

Security helpers belong in:

```text
app/core/security.py
```

Required rules:

- Use password hashing for stored passwords.
- Never store plain text passwords.
- Create tokens through a shared security helper.
- Do not log passwords, tokens, secrets, or authorization headers.
- Use `EmailStr` for email validation.
- New protected routes should verify JWT/auth identity before trusting user input.
- Avoid hardcoded development secrets when improving or refactoring security.

## Error Handling Rules

- Routes should raise `HTTPException` for client-visible API errors.
- Services may return structured errors to routes, but routes should convert them to correct HTTP status codes.
- Integration failures should include a clear message and not crash the server.
- Do not swallow exceptions silently.
- For WebSockets, handle disconnects and stop loops cleanly.
- Avoid returning stack traces to clients.

## WebSocket Rules

- Accept the socket before sending data.
- Handle `WebSocketDisconnect`.
- Avoid busy loops; include `asyncio.sleep`.
- Do not store per-user sensitive state in a global dict unless the data is intentionally shared.
- If multiple users/devices are supported, key streams by session or device instead of one global `latest_scores`.
- Keep hardware control separate from data streaming.

## Naming Rules

Use these naming patterns:

```text
routes/auth.py
schemas/auth_schema.py
models/user.py
services/auth_service.py
integrations/iot_client.py
```

Classes:

```text
AuthService
DeviceService
DriveService
SignupRequest
LoginRequest
DrivingSession
IoTClient
```

Functions:

```text
signup
login
start_session
stop_session
log_location
log_alert
```

## Scalability Rules

When adding a new backend feature:

1. Create or update a schema in `app/schemas`.
2. Create or update a model in `app/models` if persistence is needed.
3. Import new models in `app/db/base.py`.
4. Add service logic in `app/services`.
5. Add external calls in `app/integrations` if another service is involved.
6. Add thin route handlers in `app/api/v1/routes`.
7. Register the router in `app/api/v1/api.py`.
8. Update Android only if the API contract changes.
9. Run a syntax/build check before finishing.

Do not modify unrelated routes or services while adding one feature.

## LLM Pre-Code Checklist

Before editing backend code, the LLM must answer these internally:

- Which layer owns this change: route, schema, model, service, integration, db, or core?
- Does Android already depend on this endpoint response?
- Is this database-backed or only an external orchestration action?
- Does this need `Depends(get_db)`?
- Does this need a new Pydantic schema?
- Does this need a new SQLAlchemy model or relationship?
- Does `app/db/base.py` need an import?
- Does `app/api/v1/api.py` need router registration?
- Could this break the IoT or AI service port contract?
- What verification command should be run after editing?

## LLM Forbidden Moves

- Do not put business logic directly in routes.
- Do not put database queries in schemas.
- Do not put FastAPI route decorators in services.
- Do not put AI model inference in `android_backend`.
- Do not hardcode service IPs or secrets when adding new code.
- Do not create a second database engine/session system.
- Do not rename public API routes casually.
- Do not change response keys used by Android without updating Android.
- Do not write code inside `venv`, `android_env`, `__pycache__`, or generated database files.
- Do not commit credentials, tokens, or local machine-only IPs as permanent config.

## Done Definition

A backend change is complete only when:

- Code is in the correct folder/layer.
- Routes are thin and registered.
- Schemas validate request/response data.
- Services own business/database logic.
- Integrations own external calls.
- Database sessions are closed by dependency management or `finally`.
- New models are imported in `db/base.py`.
- API response keys remain compatible with Android.
- Errors use clear HTTP status codes or clear integration failure messages.
- WebSocket loops handle disconnects.
- A syntax check or server import check passes, or the reason it could not be run is documented.

