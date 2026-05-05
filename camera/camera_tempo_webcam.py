import cv2
import asyncio
import websockets
import json

async def stream_video():
    uri = "ws://localhost:8002/stream"
    
    # Open local webcam (0 is usually the default laptop webcam)
    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("Error: Could not open the webcam.")
        return

    print(f"📶 Connecting to {uri}...")
    try:
        async with websockets.connect(uri) as websocket:
            print("✅ Connected! Sending frames...")
            while True:
                ret, frame = cap.read()
                if not ret:
                    print("Failed to grab frame.")
                    break
                
                # 1. Encode frame as JPEG
                success, buffer = cv2.imencode('.jpg', frame)
                if not success:
                    continue
                
                jpeg_bytes = buffer.tobytes()
                
                # 2. Send bytes over websocket
                await websocket.send(jpeg_bytes)
                
                # 3. Receive the analysis JSON from the backend
                result_str = await websocket.recv()
                result = json.loads(result_str)
                
                print(f"📊 Server Response: {result['scores']}")
                
                # Show the webcam locally
                cv2.imshow("Local Webcam", frame)
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    break
                    
                # Small pause to hit roughly ~30 FPS
                await asyncio.sleep(0.03)

    except ConnectionRefusedError:
        print("❌ Error: Connection refused. Make sure you run the FastAPI backend first!")
    except Exception as e:
        print(f"❌ Error: {e}")
    finally:
        cap.release()
        cv2.destroyAllWindows()

if __name__ == "__main__":
    asyncio.run(stream_video())
