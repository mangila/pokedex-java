# Pokedex Database

The Database module implements a lightweight, embedded MultiMap key -> field -> value store with high throughput and low latency, inspired by RocksDB and Redis Hash.

Characteristics:
- Write-Ahead Log (WAL) per table with memory-mapped files for fast IO.
- Append-only WAL writes, with background compression of big objects.
- In-memory indexes and caches backed by off-heap buffers.
- Event-driven write pipeline with publisher/subscriber threads.

Key packages:
- com.github.mangila.pokedex.database (Database, DefaultDatabase)
- com.github.mangila.pokedex.database.engine (Engine, ReadOps, WriteOps, Cache)
- com.github.mangila.pokedex.database.model (Key, Field, Value, Entry, Metadata, etc.)
- com.github.mangila.pokedex.database.wal (WalManager, WalTableReader/Writer, MappedBuffer, WalFileHandle, etc.)
- com.github.mangila.pokedex.database.config (DatabaseConfig, WalConfig)

Lifecycle:
- Database.open() initializes the engine and WAL tables.
- Database.close() flushes outstanding writes and closes mapped files.

Typical flows (high-level):
- PUT: enqueue WriteOperation -> WAL append -> in-memory index update.
- GET: locate Key/Field in index -> read from cache or WAL region via MappedBuffer.
- DELETE: mark tombstone in WAL -> index eviction handled by engine.
- KEYS/FIELDS: iterate index structures and stream results.

Caveats:
- Optimized for read/write throughput; durability depends on WAL flush policies and OS page cache.
- Larger objects are compressed through a dedicated queue to keep append path fast.
