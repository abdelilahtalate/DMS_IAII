import requests


class IoTClient:

    BASE_URL = "http://127.0.0.1:8001"

    def verify_device(self, device_id: str, secret: str):
        response = requests.post(
            f"{self.BASE_URL}/pair",
            params={
                "device_id": device_id,
                "secret": secret
            }
        )
        return response.json()

    def get_preview(self):
        return f"192.168.137.1:8001/preview"

    def stop_preview(self):
        try:
            response = requests.get(f"{self.BASE_URL}/stop-preview")
            return response.json()
        except Exception as e:
            return {"error": f"Failed to contact device: {e}"}

    def start_analysis(self):
        try:
            response = requests.get(f"{self.BASE_URL}/start-analysis")
            return response.json()
        except Exception as e:
            return {"error": f"Failed to contact device: {e}"}

    def stop_analysis(self):
        try:
            response = requests.get(f"{self.BASE_URL}/stop-analysis")
            return response.json()
        except Exception as e:
            return {"error": f"Failed to contact device: {e}"}