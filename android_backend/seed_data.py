import sys
import os
from datetime import datetime, timedelta
import random

# Add current directory to path so we can import app
sys.path.append(os.getcwd())

from app.db.session import SessionLocal, engine
from app.models.user import User
from app.models.device import Device
from app.models.session import DrivingSession
from app.models.alert import Alert
from app.models.location import LocationPoint
from app.db.session import Base

def seed():
    # Create tables if they don't exist
    Base.metadata.create_all(bind=engine)
    
    db = SessionLocal()
    try:
        # 1. Create/Get User 1
        user = db.query(User).filter(User.id == 1).first()
        if not user:
            print("Creating User 1...")
            user = User(
                id=1,
                full_name="Abdelilah Talate",
                email="abdelilah@example.com",
                hashed_password="mock_password",
                is_active=True
            )
            db.add(user)
            db.commit()
            db.refresh(user)
        
        # 2. Create/Get Device
        device = db.query(Device).filter(Device.device_id == "ESP_001").first()
        if not device:
            print("Creating Device ESP_001...")
            device = Device(
                device_id="ESP_001",
                secret="abc123",
                paired=True
            )
            db.add(device)
            db.commit()
            db.refresh(device)

        # 3. Clear old sessions for a clean test? 
        # No, let's just add to them or keep them.
        
        print("Generating mock sessions for the last 14 days...")
        now = datetime.now()
        
        # Generate sessions for the last 14 days
        for i in range(14):
            day = now - timedelta(days=i)
            # 1-2 sessions per day
            num_sessions = random.randint(1, 2)
            
            for s in range(num_sessions):
                # Random start time between 8am and 10pm
                start_hour = random.randint(8, 20)
                start_time = day.replace(hour=start_hour, minute=random.randint(0, 59), second=0, microsecond=0)
                duration_min = random.randint(15, 120)
                end_time = start_time + timedelta(minutes=duration_min)
                
                avg_focus = random.randint(70, 98)
                distance = random.randint(5000, 45000) # 5-45km
                
                session = DrivingSession(
                    user_id=1,
                    device_id=device.id,
                    start_time=start_time,
                    end_time=end_time,
                    total_distance=distance,
                    average_focus=avg_focus,
                    status="completed"
                )
                db.add(session)
                db.commit()
                db.refresh(session)
                
                # Add 0-5 alerts per session
                num_alerts = random.randint(0, 5)
                for a in range(num_alerts):
                    alert_time = start_time + timedelta(minutes=random.randint(1, duration_min-1))
                    alert_type = random.choice(["fatigue", "distraction", "phone_usage"])
                    
                    alert = Alert(
                        session_id=session.id,
                        alert_type=alert_type,
                        latitude=33.5731 + random.uniform(-0.1, 0.1),
                        longitude=-7.5898 + random.uniform(-0.1, 0.1),
                        timestamp=alert_time
                    )
                    db.add(alert)
                
        db.commit()
        print("Successfully seeded database with 14 days of drive data!")
        
    except Exception as e:
        print(f"Error seeding data: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    seed()
