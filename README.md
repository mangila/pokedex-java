# pokedex-java

Pokedex built in Java

## Development Environment

- Java 21+
- Python3 installed on the system (for pre-commit hooks)
- cwebp installed on the system (when converting images to webp)

## Project Goal

- Implement and pinpoint software engineering principles and techniques
- Build software components from scratch
- For the sake of fun and love for programming, technologies and Pokémon

## Project Structure (WIP)

- Service Layer
- Maven multi module project
    - scheduler - Daemon Service
    - web - Web Service
    - shared — Common classes
- Java 21
- Terraform (AWS provider)
- Maven Wrapper (for consistent builds)

## System Architecture (WIP)

- Domain Primitives with Service Layer
- Event Driven

## Paradigms (WIP)

- Functional
- Imperative
- Object Oriented
- Declarative
- Concurrent

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
    - Serve JSON
- Key Value Database (Yet Another Key Value Store)
    - Persist to disk
    - Lock Free read and writes
    - Append only to file
    - ".yakvs" file format
        - Binary format
        - Index file
        - Data file

## Engineering Principles (WIP)

- EAFP — Easier to Ask Forgiveness than Permission
- LBYL - Look Before You Leap
- Defensive Programming
    - Ensure pattern / Fail fast
    - Try pattern / Fail safe

## Datastructures (WIP)

- Queue
    - Bounded Queue
    - Linked Queue
- Linked List
- Array List
- Hash Table

## Algorithms techniques (WIP)

- Two pointers

## Design Patterns (WIP)

#### Gang of Four

- Singleton
    - Bill Pugh with Configure method
    - Bill Pugh with Configure and Reset method
- Factory Method

#### Concurrency

- Fan Out and Fan In
- Dedicated Reader/Write Thread
- Trigger Thread with a Worker pool