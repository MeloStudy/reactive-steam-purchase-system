from dataclasses import dataclass
from typing import List, Dict, Any

@dataclass
class Game:
    id: str
    name: str
    categories: List[str]
    price: float
    available: bool
    discount_percentage: float
    active_discount: bool

    def to_dict(self) -> Dict[str, Any]:
        """Convert domain model to a dictionary suitable for JSON serialization."""
        return {
            "id": self.id,
            "name": self.name,
            "categories": ";".join(self.categories),
            "price": self.price,
            "available": self.available,
            "discountPercentage": self.discount_percentage,
            "activeDiscount": self.active_discount
        }

    @classmethod
    def from_row(cls, row) -> 'Game':
        """Create a Game domain model from a database row."""
        categories_list = [c.strip() for c in row["categories"].split(";") if c.strip()]
        return cls(
            id=row["id"],
            name=row["name"],
            categories=categories_list,
            price=row["price"],
            available=bool(row["available"]),
            discount_percentage=row["discountPercentage"],
            active_discount=bool(row["activeDiscount"])
        )
