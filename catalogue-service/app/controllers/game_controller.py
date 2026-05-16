from flask import Blueprint, request, jsonify
from app.services.catalogue_service import CatalogueService

catalogue_bp = Blueprint('catalogue_bp', __name__)

@catalogue_bp.route('/games', methods=['GET'])
def get_games():
    """List all games in the catalog."""
    games = CatalogueService.get_all_games()
    return jsonify([game.to_dict() for game in games]), 200

@catalogue_bp.route('/games/<id>', methods=['GET'])
def get_game(id):
    """Retrieve a single game by ID, respecting latency configuration."""
    game = CatalogueService.get_game_by_id(id)
    if game is None:
        return jsonify({"error": "Game not found"}), 404
    return jsonify(game.to_dict()), 200

@catalogue_bp.route('/games/<id>', methods=['PUT'])
def update_game(id):
    """Update dynamic parameters of a game (price, active discount, availability)."""
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid input"}), 400
        
    try:
        updated_game = CatalogueService.update_game(id, data)
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
        
    if updated_game is None:
        return jsonify({"error": "Game not found"}), 404
        
    return jsonify(updated_game.to_dict()), 200
