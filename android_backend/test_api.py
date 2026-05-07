import requests
import json

def test_devices():
    base_url = "http://localhost:8000/api/v1"
    
    print("--- Testing Devices ---")
    res = requests.get(f"{base_url}/profile/1/devices")
    print(f"Status: {res.status_code}")
    print(f"RAW Body: {res.text}")

if __name__ == "__main__":
    test_devices()
