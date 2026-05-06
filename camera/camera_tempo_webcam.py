import cv2
import asyncio
import websockets
import json

URI = "ws://localhost:8002/stream"

async def send_frames(websocket, cap):
    while True:
        ret, frame = cap.read()
        if not ret:
            continue

        # 🔹 Resize (optional → reduces latency)
        frame = cv2.resize(frame, (640, 480))

        # 🔹 SHOW PREVIEW
        cv2.imshow("📷 Local Camera", frame)

        # Press 'q' to quit
        if cv2.waitKey(1) & 0xFF == ord('q'):
            raise Exception("User exit")

        # 🔹 Compress frame
        _, buffer = cv2.imencode('.jpg', frame, [int(cv2.IMWRITE_JPEG_QUALITY), 70])
        await websocket.send(buffer.tobytes())

        await asyncio.sleep(0.03)  # ~30 FPS


async def receive_results(websocket):
    while True:
        try:
            result = await websocket.recv()
            data = json.loads(result)
            print("📊", data)
        except:
            print("⚠️ Receive error")
            break


async def stream_video():
    cap = cv2.VideoCapture(0)

    if not cap.isOpened():
        print("❌ Cannot open camera")
        return

    while True:  # 🔥 AUTO RECONNECT LOOP
        try:
            print("📶 Connecting...")
            async with websockets.connect(
                URI,
                ping_interval=10,
                ping_timeout=20,
                max_size=None
            ) as websocket:

                print("✅ Connected")

                sender = asyncio.create_task(send_frames(websocket, cap))
                receiver = asyncio.create_task(receive_results(websocket))

                done, pending = await asyncio.wait(
                    [sender, receiver],
                    return_when=asyncio.FIRST_EXCEPTION
                )

                for task in pending:
                    task.cancel()

        except Exception as e:
            print("❌ Connection lost:", e)
            print("🔁 Reconnecting in 2s...")
            await asyncio.sleep(2)

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    try:
        asyncio.run(stream_video())
    finally:
        cv2.destroyAllWindows()