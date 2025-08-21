# Pokedex Database

Documentation about the /pokedex/database module

## Database

The Pokédex database is an embedded key-value store. (yet another one)

The engine uses an LRU cache as a "second-level cache" and persists data to disk.

All its operations are asynchronous using CompletetableFutures.

The file is append-only and the header is locked during write operations. After (n) writes, a compact operation is
triggered.

The compact operation will remove the old keys and values from the file.
That means when a key is updated, it will append a new record to the file.
So when the compact operation is running, the oldest record will be removed.

### Reader Thread

Pooled ScheduledExecutor with a Virtual Thread factory is polling from an internal queue for read operations.

The executor is scheduled on a fixed rate and creates a new thread for each operation from its pool.

### Writer Thread

Pooled ScheduledExecutor with a Virtual Thread factory is polling from an internal queue for write operations.

The executor is scheduled on a fixed rate and creates a new thread for each operation from its pool.

When Write operations are performed, it will try to acquire a file lock on the header region of the file.
So that reading threads can still read the file.

The file header lock is the - “global append mutex.”—that is used to prevent concurrent writes to the same file.

Writer thread also polls write operations for delete and truncate to the file, but it does not acquire the file lock.
Eager approach for when truncating and deleting.

## .yakvs file-format

`.yakvs` is a binary file-format for storing data. It uses two files, one for the index and one for the data.

The header is the same for both files.

No size limit is imposed on the files.

### .yakvs header-format

The specs for the header are in order:

* Magic number-"yakvs"
    * The magic number is a simple identifier for the file format.
* Version—four bytes
    * The version is used to identify the version format of the file.
* Record count—four bytes
    * Record count is the number of records in the file.
* Next offset—eight bytes
    * The next offset is the offset to be used when writing the next record.

After a write operation, the next offset is updated and the record count is incremented.

### .yakvs index-format

The index file stores the key and offset/position of the data records.

For more efficient access, an index map(ConcurrentHashMap) is loaded into memory during startup,
it overwrites the duplicates and makes sure the newest key is loaded, during write operations the index, and its internal map is updated.

The specs for the index are in order:

* Key Length-4 bytes
    * The key length is the length of the key.
* Key Bytes-n bytes
    * The actual key. Can be any size.
* Data Offset-8 bytes
    * Points to record in data-file

### .yakvs data-format

The data file stores the actual data.

The specs for the data are in order:

* Record Length—4 bytes
    * The record length is the length of the record.
* Data - (n) bytes
    * The actual data. Can be any size.