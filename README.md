# Pokédex Spring Boot Application

A modern, distributed Pokémon data aggregation system built with Spring Boot, providing a GraphQL API for querying
Pokémon data and managing media assets. The application consists of two main modules: a scheduler for data collection
and a GraphQL API server.

## Overview

This application serves as a comprehensive Pokémon data platform that automatically fetches and aggregates data from
PokéAPI, stores it efficiently, and provides it through a modern GraphQL interface.

## Architecture

The application is built using a microservices architecture with two main components:

### 1. Scheduler Service

- Responsible for data collection and processing
- Manages distributed task queuing
- Handles media file downloads and storage
- Components:
    - Task Scheduler
    - Pokemon Data Processor
    - Media Handler
    - Redis Queue Manager

### 2. GraphQL API Service

- Provides the query interface for Pokémon data
- Serves media files with proper caching
- Handles GraphQL operations
- Components:
    - GraphQL Controllers
    - Data Resolvers
    - Media File Server

## Technology Stack

### Core Technologies

- Java 21
- Spring Boot 3.4.4
- MongoDB (with GridFS)
- Redis
- GraphQL
- Project Reactor Netty

### Key Dependencies

- Spring Data MongoDB
- Spring Data Redis
- Spring GraphQL
- Lettuce (Redis client)
- Project Reactor Netty
- Lombok
- Spring Validation

### Testing

- Spring Boot Test
- Testcontainers
- JUnit Jupiter

## Features

### Data Collection

- Automated Pokemon data harvesting from PokéAPI
- Distributed task processing using Redis queues
- Efficient media file handling (images and sounds)
- Fault-tolerant processing with automatic retries

### Storage

- MongoDB for structured Pokémon data
- GridFS for media file storage
- Redis for task queue management

### API

- GraphQL endpoint for flexible data queries
- REST endpoints for media file serving
- Proper cache control headers
- Support for various media types:
    - Images (PNG, JPG, GIF, SVG)
    - Audio (OGG)

## Prerequisites

- Java 21 or higher
- MongoDB 5.0+
- Redis 6.0+
- Maven 3.8+
- Minikube 1.26+
- Python 3.8+
- Docker

## Module Structure

### pokedex-scheduler

- Handles data collection and processing
- Manages distributed task queues
- Processes media downloads

### pokedex-graphql

- Provides GraphQL API
- Serves media files
- Handles data queries

## Configuration

The application uses Spring Boot's configuration system. Key configuration files:

- `application.yml` - Core application settings
- `graphql/schema.graphqls` - GraphQL schema definition

## Running the Application

1. Install Minikube and create a Kubernetes cluster.
    1. Create a namespace for `pokedex`
2. Run the `minikube.py` script
3. Run `minikube dashboard` to view the application status and logs
    1. Port forward to access from localhost - `kubectl port-forward service/pokedex-graphql-service 7080:80 -n pokedex`