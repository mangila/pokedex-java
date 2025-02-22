FROM ghcr.io/graalvm/native-image-community:21 AS build

WORKDIR /usr/src/app

COPY . .
# Compile modules with Spring Native Profile
RUN ./mvnw.sh clean install -Pnative -DskipTests -pl integration -pl model -pl pokemon-task
# Generate Native Image
RUN ./mvnw.sh -o native:compile -Pnative -DskipTests -pl integration -pl model -pl pokemon-task

FROM debian:bookworm-slim

WORKDIR /app

# Copy the native binary from the build stage
COPY --from=build /usr/src/app/pokemon-task/target/pokemon-task /app/task

# Run the application
CMD ["/app/task"]