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
        print("Adding user_id column to devices...")
        cursor.execute("ALTER TABLE devices ADD COLUMN user_id INTEGER REFERENCES users(id)")
        print("Column added successfully!")
    except Exception as e:
        print(f"Error: {e}")
        
    conn.commit()
    conn.close()

if __name__ == "__main__":
    migrate()
