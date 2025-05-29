# Maven Wrapper

The Maven Wrapper is a way to ensure a user of your Maven build has everything necessary to run your Maven build.

## Why

The Maven Wrapper was created to solve the following problems:

- Dependency on having Maven installed on the machine
- Dependency on having the right version of Maven installed on the machine

## How it works

When you run the wrapper script (`mvnw` or `mvnw.cmd`), it will download the correct version of Maven (as specified in `.mvn/wrapper/maven-wrapper.properties`) if it's not already present, and then use that version to run Maven.

## Files

- `maven-wrapper.properties`: Specifies the Maven version to use
- `maven-wrapper.jar`: The wrapper JAR file (will be downloaded automatically on first run)
- `MavenWrapperDownloader.java`: A Java class used as a fallback to download the wrapper JAR
- `mvnw`: Shell script for Unix-like systems
- `mvnw.cmd`: Batch script for Windows

## Usage

Instead of running Maven commands with `mvn`, use the wrapper scripts:

```bash
# Unix-like systems
./mvnw clean install

# Windows
mvnw.cmd clean install
```

This ensures that everyone uses the same version of Maven, regardless of what's installed on their machine.