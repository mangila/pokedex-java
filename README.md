# pokedex-java

Low-level Java implementation that consumes data from PokeApi and serves it over HTTP.

For more information, please see /docs

## Project Goal

- Implement and pinpoint software engineering principles and techniques
- Build software components from scratch
- For the sake of fun and love for programming, technologies and Pokémon

## Development Environment

- Java 21+
- Python3 installed on the system (for pre-commit hooks)
- `cwebp` installed on the system (when converting images to webp)

## Software Components (WIP)

- Http Client
    - HTTP/1.1
    - Connection Pool
    - SSL/TLS
- Json Parser
- Time To Live(TTL) Cache
- Last Recently Used(LRU) Cache
- Scheduler
- HTTP Server
    - Serve RESTful JSON
    - HTTP/1.1
- Key Value Database (Yet Another Key Value Store)
    - Persist to disk
    - Lock Free read and writes
    - Append only to file
    - ".yakvs" file format
        - Binary format
        - Index file
        - Data file