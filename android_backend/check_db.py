import sqlite3
import os

def check_db():
    db_path = "android_backend/drivemind.db"
    if not os.path.exists(db_path):
        print("DB not found")
        return
        
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    print("--- Sessions for User 1 ---")
    cursor.execute("SELECT id, average_focus, start_time FROM sessions WHERE user_id = 1")
    rows = cursor.fetchall()
    for row in rows:
        print(f"Session ID: {row[0]}, Focus: {row[1]}, Start: {row[2]}")
    
    conn.close()

if __name__ == "__main__":
    check_db()
