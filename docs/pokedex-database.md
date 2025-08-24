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
            Insert key/value into WalTable (in-memory)
                             │
                             ▼
                        Is WalTable full?
                             ├─ No → Done
                             └─ Yes → Flush WalTable to disk:
                             ├─ Create hash buckets (hash(key) % N)
                             └─ Update Bloom filter for new keys
                             │
                             ▼
                Discard WAL entries for flushed WalTable

