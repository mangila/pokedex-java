# Pokedex Database

Documentation about the /pokedex/database module

## Database

"Write first, read later" kind of database. For Bursty Write spikes.

### Write Flow

1. Client sends PUT
2. PUT is sent to write via Subscriber queue
3. Flush thread flushes to disk after a certain threshold is reached (batch writes, less IO syscalls)
   3.1. Rotate Thread rotates WAL files if a certain threshold is met and compresses to GZIP
4. Client receives ACK

