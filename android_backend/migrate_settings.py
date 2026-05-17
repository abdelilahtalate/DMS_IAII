import sqlite3
import os

def migrate():
    db_path = "android_backend/drivemind.db"
    if not os.path.exists(db_path):
        print("DB not found")
        return
        
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    try:
        print("Creating user_settings table...")
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS user_settings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE REFERENCES users(id),
                fatigue_sensitivity TEXT DEFAULT 'HIGH',
                phone_sensitivity TEXT DEFAULT 'MEDIUM',
                distraction_sensitivity TEXT DEFAULT 'HIGH',
                voice_alerts BOOLEAN DEFAULT 1,
                haptic_feedback BOOLEAN DEFAULT 1,
                critical_only BOOLEAN DEFAULT 0,
                preferred_voice TEXT DEFAULT 'DEFAULT'
            )
        """)
        print("Table created successfully!")
    except Exception as e:
        print(f"Error: {e}")
        
    conn.commit()
    conn.close()

if __name__ == "__main__":
    migrate()
