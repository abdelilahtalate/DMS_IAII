from app.core.security import hash_password, verify_password, create_access_token

# TEMP "DATABASE"
fake_users_db = {}


class AuthService:

    def signup(self, full_name: str, email: str, password: str, gender: str, age: int = None):
        if email in fake_users_db:
            return {"error": "User already exists"}

        hashed = hash_password(password)

        fake_users_db[email] = {
            "full_name": full_name,
            "email": email,
            "password": hashed,
            "gender": gender,
            "age": age,
            "role": "driver"
        }

        token = create_access_token({"sub": email})

        return {
            "message": "User created",
            "access_token": token
        }

    def login(self, email: str, password: str):
        user = fake_users_db.get(email)

        if not user:
            return {"error": "User not found"}

        if not verify_password(password, user["password"]):
            return {"error": "Invalid credentials"}

        token = create_access_token({"sub": email})

        return {
            "message": "Login successful",
            "access_token": token
        }