![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![Terraform](https://img.shields.io/badge/terraform-%235835CC.svg?style=for-the-badge&logo=terraform&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54)

# pokedex-java

Low-level Java implementation that consumes data from [https://pokeapi.co/](https://pokeapi.co/), saves it to disk and serves it via HTTP.

For more information, please see /docs (wip)

## Project Goal

- Implement and pinpoint software engineering principles and techniques
- Build software components from scratch
- For the sake of fun and love for programming, technologies and Pokémon

## Development Environment

- Java 21+
- Python3 installed on the system (for pre-commit hooks)
- `cwebp` installed on the system (when converting images to webp)
- `terraform` installed on the system (for infrastructure as code)

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
    - Serve media files
    - HTTP/1.1
- Key Value Database (Yet Another Key Value Store)
    - Persist to disk
    - Lock Free read and writes
    - Append only to file
    - ".yakvs" file format
        - Binary format
        - Index file
        - Data file

## Contributing

This project is for educational purposes. Feel free to fork and extend it.

## License

This project is open-source and available under the [MIT License](LICENSE).

This project is independent work and has no affiliation with [https://pokeapi.co/]([https://pokeapi.co/]) or The Pokémon Company.