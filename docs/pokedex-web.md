# Pokedex Web

The Web module contains an HTTP application entry point and web-layer utilities when running the service as a standalone web app.

Current status:
- WebApplication main class exists primarily for development/testing. The production entry point lives in the app module.

Responsibilities:
- Wire a future HTTP server and REST endpoints (WIP).
- Serve static/media assets (WIP).

Key class:
- com.github.mangila.pokedex.web.WebApplication