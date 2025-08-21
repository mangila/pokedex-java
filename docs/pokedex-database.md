# Pokedex Database

Documentation about the /pokedex/database module

## Database

The pokedex database is a key-value store. (yet another one)

The engine uses an LRU cache and persists the data to disk—All operations are asynchronous.
A ScheduledExecutor creating Virtual Threads is polling from an internal queue for read or write operations.

One thread is responsible for reading and one thread is responsible for writing.

### Reader Thread

ScheduledExecutor with a Virtual Thread factory is polling from an internal queue for read operations.

The executor is scheduled on a fixed rate.

## Writer Thread

ScheduledExecutor with a Virtual Thread factory is polling from an internal queue for write operations.

The executor is scheduled on a fixed delay.

When Write operations are performed, it will try to acquire a file lock on the header region of the file.

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