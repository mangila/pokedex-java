# Pokedex Database

Documentation about the /pokedex/database module

# .yakvs format

`.yakvs` is a binary file-format for storing data. It uses two files, one for the index and one for the data.

The header is the same for both files.

## .yakvs header-format

The specs for the header are in order:

* Magic number-"yakvs"
    * The magic number is a simple identifier for the file format.
* Version—four bytes
    * The version is used to identify the format of the file.
* Record count—four bytes
    * Record count is the number of records in the file.
* Next offset—eight bytes
    * The next offset is the offset to be used when writing the next record.

## .yakvs index-format

## .yakvs data-format