import sqlite3
import os

# Create an instance directory if it doesn't exist to store the DB
BASE_DIR = os.getcwd()
INSTANCE_DIR = os.path.join(BASE_DIR, 'instance')

if not os.path.exists(INSTANCE_DIR):
    os.makedirs(INSTANCE_DIR)

# Allow overriding via environment variable for Docker volumes
DB_PATH = os.environ.get('DATABASE_PATH', os.path.join(INSTANCE_DIR, 'catalogue.db'))

def get_db_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # Clean up existing data to ensure a fresh start on every boot
    cursor.execute('DROP TABLE IF EXISTS CATALOG')
    
    # Create the CATALOG table
    cursor.execute('''
        CREATE TABLE CATALOG (
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            categories TEXT NOT NULL,
            price REAL NOT NULL,
            available BOOLEAN NOT NULL CHECK (available IN (0, 1)),
            discountPercentage REAL NOT NULL,
            activeDiscount BOOLEAN NOT NULL CHECK (activeDiscount IN (0, 1))
        )
    ''')
    
    # Always seed the database with the initial state
    seed_data = [
        ("GAME-001", "The Witcher 4", "Action;RPG", 60.00, 1, 10.00, 1),
        ("GAME-002", "Cyberpunk 2078", "Action;Sci-Fi", 50.00, 1, 0.00, 0),
        ("GAME-003", "Stardew Valley 2", "Simulation;RPG", 20.00, 1, 25.00, 1),
        ("GAME-004", "Hollow Knight: Silksong", "Action;Platformer", 30.00, 1, 0.00, 0),
        ("GAME-005", "Half-Life 3", "Action;Sci-Fi", 70.00, 0, 0.00, 0)
    ]
    
    cursor.executemany('''
        INSERT INTO CATALOG (id, name, categories, price, available, discountPercentage, activeDiscount)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    ''', seed_data)
        
    conn.commit()
    conn.close()
