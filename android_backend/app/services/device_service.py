from app.services.device_store import devices_db


class DeviceService:

    def verify_device(self, device_id: str, secret: str):
        device = devices_db.get(device_id)

        if not device:
            return {"error": "Device not found"}

        if device["secret"] != secret:
            return {"error": "Invalid device secret"}

        return {"message": "Device verified", "device": device}

    def register_owner(self, device_id: str, user_email: str):
        device = devices_db.get(device_id)

        if not device:
            return {"error": "Device not found"}

        device["owner"] = user_email

        return device

    def update_status(self, device_id: str, ip: str, stream_url: str):
        device = devices_db.get(device_id)

        if not device:
            return {"error": "Device not found"}

        device["status"] = "online"
        device["ip"] = ip
        device["stream_url"] = stream_url

        return device