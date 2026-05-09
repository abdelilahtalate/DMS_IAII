from sqlalchemy import Column, Integer, String, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.db.session import Base

class DrivingSession(Base):
    __tablename__ = "sessions"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    device_id = Column(Integer, ForeignKey("devices.id"))
    
    start_time = Column(DateTime(timezone=True), server_default=func.now())
    end_time = Column(DateTime(timezone=True), nullable=True)
    total_distance = Column(Integer, default=0) # in meters
    average_focus = Column(Integer, default=100)
    status = Column(String, default="active") # active, completed
    
    # Relationships
    user = relationship("User", back_populates="sessions")
    alerts = relationship("Alert", back_populates="session")
    location_points = relationship("LocationPoint", back_populates="session")
