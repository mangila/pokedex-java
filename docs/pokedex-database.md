# Pokedex Database

Documentation about the /pokedex/database module

## Database

### Write Flow
        
                        Client / App
                             │
                             ▼
                Append key/value to WAL (disk)
                             │                                   
                             ▼                                        
            Insert key/value into MemTable (in-memory)
                             │
                             ▼
                        Is MemTable full?
                             ├─ No → Done
                             └─ Yes → Flush MemTable to disk:
                             ├─ Create hash buckets (hash(key) % N)
                             └─ Update Bloom filter for new keys
                             │
                             ▼
                Discard WAL entries for flushed MemTable

