# Pokedex Database

Documentation about the /pokedex/database module

## Database

"Write first, read later" database.

High throughput, low latency. With the cost of less durability and higher RAM usage.

### Write Flow

1. Client sends PUT
2. PUT is stored in-memory, waiting to be flushed to disk
3. Flush thread flushes to disk after a PUT limit threshold is met
   3.1. Rotate Thread rotates WAL files after a file size threshold is met
   3.2. Compression Thread compresses WAL file to gzip after a successful rotation
4. Client receives ACK

This flow gives very high throughput but less durability. Since the data is stored in-memory, until the flush thread flushes to the disk, the data is lost if the JVM crashes.

