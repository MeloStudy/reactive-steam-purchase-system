from flask import Flask
from app.db.database import init_db
from app.controllers.game_controller import catalogue_bp

def create_app():
    app = Flask(__name__)
    
    # Initialize SQLite database and tables
    init_db()
    
    # Register blueprints/routes
    app.register_blueprint(catalogue_bp)
    
    return app
