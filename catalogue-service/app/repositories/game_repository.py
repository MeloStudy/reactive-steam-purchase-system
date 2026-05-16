from typing import List, Optional
from app.db.database import get_db_connection
from app.models.game import Game

class GameRepository:
    @staticmethod
    def get_all() -> List[Game]:
        """Fetch all games from the SQLite3 database and map them to domain models."""
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM CATALOG')
        rows = cursor.fetchall()
        conn.close()
        return [Game.from_row(row) for row in rows]

    @staticmethod
    def get_by_id(game_id: str) -> Optional[Game]:
        """Fetch a single game by ID, return None if not found."""
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM CATALOG WHERE id = ?', (game_id,))
        row = cursor.fetchone()
        conn.close()
        if row is None:
            return None
        return Game.from_row(row)

    @staticmethod
    def update(game_id: str, updates: dict) -> Optional[Game]:
        """Update fields of a game dynamically and return the updated Game model."""
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Prepare queries
        fields = []
        params = []
        
        if "available" in updates:
            fields.append("available = ?")
            params.append(1 if updates["available"] else 0)
            
        if "activeDiscount" in updates:
            fields.append("activeDiscount = ?")
            params.append(1 if updates["activeDiscount"] else 0)
            
        if "price" in updates:
            fields.append("price = ?")
            params.append(float(updates["price"]))
            
        if "discountPercentage" in updates:
            fields.append("discountPercentage = ?")
            params.append(float(updates["discountPercentage"]))
            
        if fields:
            params.append(game_id)
            query = f'UPDATE CATALOG SET {", ".join(fields)} WHERE id = ?'
            cursor.execute(query, params)
            conn.commit()
            
        # Retrieve the updated model
        cursor.execute('SELECT * FROM CATALOG WHERE id = ?', (game_id,))
        row = cursor.fetchone()
        conn.close()
        
        if row is None:
            return None
        return Game.from_row(row)
