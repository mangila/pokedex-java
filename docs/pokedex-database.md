# Pokedex Database

Documentation about the /pokedex/database module

## Database

### Write Flow
        
                        Client / App
                             │
                             ▼
                Append hashKey/value to WAL (disk)
                             │                                   
                             ▼                                        
            Insert hashKey/value into MemTable (in-memory)
                             │
                             ▼
                        Is MemTable full?
                             ├─ No → Done
                             └─ Yes → Flush MemTable to disk:
                             ├─ Create hash buckets (hash(hashKey) % N)
                             └─ Update Bloom filter for new keys
                             │
                             ▼
                Discard WAL entries for flushed MemTable

