from sqlalchemy import Column, Integer, String, Boolean, ForeignKey
from sqlalchemy.orm import relationship
from app.db.session import Base

class UserSettings(Base):
    __tablename__ = "user_settings"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), unique=True)
    
    fatigue_sensitivity = Column(String, default="HIGH")
    phone_sensitivity = Column(String, default="MEDIUM")
    distraction_sensitivity = Column(String, default="HIGH")
    
    voice_alerts = Column(Boolean, default=True)
    haptic_feedback = Column(Boolean, default=True)
    critical_only = Column(Boolean, default=False)
    
    preferred_voice = Column(String, default="DEFAULT") # e.g. "DEFAULT", "NOVA", "ECHO"

    user = relationship("User")
