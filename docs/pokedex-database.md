# Pokedex Database

Documentation about the /pokedex/database module

## Database

The pokedex database is a key-value store. (yet another one)

The engine uses an LRU cache and persists the data to disk—All operations are asynchronous.
A ScheduledExecutor creating Virtual Threads is polling from an internal queue for read or write operations.

### Reader Thread

Pooled ScheduledExecutor with a Virtual Thread factory is polling from an internal queue for read operations.

The executor is scheduled on a fixed rate and creates a new thread for each operation from its pool.

## Writer Thread

Pooled ScheduledExecutor with a Virtual Thread factory is polling from an internal queue for write operations.

The executor is scheduled on a fixed rate and creates a new thread for each operation from its pool.

When Write operations are performed, it will try to acquire a file lock on the header region of the file.
So that reading threads can still read the file.

The file header lock is the - “global append mutex.”—that is used to prevent concurrent writes to the same file.

## .yakvs format

`.yakvs` is a binary file-format for storing data. It uses two files, one for the index and one for the data.

The header is the same for both files.

### .yakvs header-format

The specs for the header are in order:

* Magic number-"yakvs"
    * The magic number is a simple identifier for the file format.
* Version—four bytes
    * The version is used to identify the format of the file.
* Record count—four bytes
    * Record count is the number of records in the file.
* Next offset—eight bytes
    * The next offset is the offset to be used when writing the next record.

### .yakvs index-format

### .yakvs data-format