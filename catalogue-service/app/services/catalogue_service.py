import time
import random
from typing import List, Optional
from app.repositories.game_repository import GameRepository
from app.models.game import Game

# Predefined list of slow legacy games (triggers artificial latency)
LATENCY_GAMES = ["GAME-002", "GAME-004", "GAME-005"]

class CatalogueService:
    @staticmethod
    def get_all_games() -> List[Game]:
        """Retrieve all games in the catalog."""
        return GameRepository.get_all()

    @staticmethod
    def get_game_by_id(game_id: str) -> Optional[Game]:
        """Retrieve a specific game by ID, introducing latency if it matches legacy items."""
        if game_id in LATENCY_GAMES:
            time.sleep(random.uniform(2.0, 3.0))
        return GameRepository.get_by_id(game_id)

    @staticmethod
    def update_game(game_id: str, updates: dict) -> Optional[Game]:
        """Verify business rules (if any) and update the game's details."""
        if "price" in updates and float(updates["price"]) < 0:
            raise ValueError("Price cannot be negative")
        return GameRepository.update(game_id, updates)
