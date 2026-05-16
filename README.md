# Reactive Game Store Purchase System

Mini-project about a video game store system using reactive and legacy components, for educational purposes.


## Requirements
- Docker or Podman
- Java 17 (for manual deployment)
- Python 3.2 (for manual deployment)

## Optional
- [IntelliJ Mermaid Plugin](https://plugins.jetbrains.com/plugin/30432-mermaid-visualizer): To visualize mermaid diagrams in readme.md

# System architecture

```mermaid
---
title:  Shopping Cart System
---
graph TD
    subgraph storeSystem [Store System]
        storeService[Store Service]
        libraryDb[(Library DB)]
        shoppingCartDb[(Shopping Cart DB)]
        
        storeService -->|r2dbc SQLite| libraryDb
        storeService -->|mongoDB Driver| shoppingCartDb
    end
    
    storeService ==Web Client / HTTP==> catalogService
    
    subgraph catalogSystem [Catalog System]
        catalogService[Catalog Service]
        catalogDb[(CatalogDB)]
        
        catalogService -->|SQLite3 blocking| catalogDb
    end
```