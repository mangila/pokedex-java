# Pokedex Database

Documentation about the /pokedex/database module

## Database

Redis Hash kind of database that persists to disk

"Write first, read later" database.

High throughput, low latency. With the cost of less durability and higher RAM usage.

### Write Flow

1. Client sends PUT
2. PUT is stored in-memory, waiting to be flushed to disk
3. Flush thread flushes to disk after a PUT limit threshold is met
    * 3.1. Rotate Thread rotates WAL files after a file size threshold is met
    * 3.2. Compression Thread compresses WAL file to gzip after a successful rotation
    * 3.3. File watcher receives event about a new gzip file has been created
    * 3.4. Write is stored to the main database file
    * 3.5. WAL writes is deleted
4. Client receives ACK (if needed, not really intended for this use case)

This flow gives very high throughput but less durability. Since the data is stored in-memory, until the flush thread
flushes to the disk, the data is lost if the JVM crashes.

To solve that problem, the flush thread can be configured to flush to disk after a certain amount of time or after a
certain amount of writes.

