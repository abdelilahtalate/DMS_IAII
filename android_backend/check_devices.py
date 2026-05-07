import sqlite3
import os

def check_devices():
    db_path = "android_backend/drivemind.db"
    if not os.path.exists(db_path):
        print("DB not found")
        return
        
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    print("--- Devices Table ---")
    cursor.execute("SELECT * FROM devices")
    rows = cursor.fetchall()
    for row in rows:
        print(row)
    
    conn.close()

if __name__ == "__main__":
    check_devices()
