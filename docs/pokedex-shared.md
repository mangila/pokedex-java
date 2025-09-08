# Pokedex Shared

The Shared module provides low-level building blocks used by all other modules.

Includes:
- HTTP primitives (HTTP/1.1 request/response parsing, headers, status, JSON-specific client)
- TLS connection pooling (client sockets, connection handles, pooling via a bounded queue)
- JSON parser and tokenizer (streaming, zero-dependency)
- Caches (TTL and LRU)
- Concurrent queues abstraction (QueueService + named blocking queues)
- Utilities (Ensure, VirtualThreadFactory, Buffer utils)

Important packages:
- com.github.mangila.pokedex.shared.https.* (http, json, media, tls)
- com.github.mangila.pokedex.shared.json.* (lexer, parser, model)
- com.github.mangila.pokedex.shared.cache.* (lru, ttl)
- com.github.mangila.pokedex.shared.queue.*
- com.github.mangila.pokedex.shared.util.*

Threading:
- Makes heavy use of Java virtual threads. VirtualThreadFactory centralizes executors and graceful shutdown.