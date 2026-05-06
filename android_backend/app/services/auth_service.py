from app.core.security import hash_password, verify_password, create_access_token
from app.models.user import User
from app.db.session import SessionLocal

class AuthService:

    def signup(self, full_name: str, email: str, password: str, gender: str, age: int = None):
        db = SessionLocal()
        try:
            # Check if user exists
            db_user = db.query(User).filter(User.email == email).first()
            if db_user:
                return {"error": "User already exists"}

            hashed = hash_password(password)
            new_user = User(
                full_name=full_name,
                email=email,
                hashed_password=hashed,
                gender=gender,
                age=age
            )
            db.add(new_user)
            db.commit()
            db.refresh(new_user)

            token = create_access_token({"sub": email})
            return {
                "message": "User created",
                "access_token": token
            }
        finally:
            db.close()

    def login(self, email: str, password: str):
        db = SessionLocal()
        try:
            db_user = db.query(User).filter(User.email == email).first()
            if not db_user:
                return {"error": "User not found"}

            if not verify_password(password, db_user.hashed_password):
                return {"error": "Invalid credentials"}

            token = create_access_token({"sub": email})
            return {
                "message": "Login successful",
                "access_token": token
            }
        finally:
            db.close()