FROM ghcr.io/graalvm/native-image-community:23 AS build

WORKDIR /usr/src/app

COPY . .
# Compile modules with Spring Native Profile
RUN ./mvnw.sh clean install -Pnative -DskipTests -pl cache -pl integration -pl model -pl media-task
# Generate Native Image
RUN ./mvnw.sh -o native:compile -Pnative -DskipTests -pl cache -pl integration -pl model -pl media-task

FROM debian:bookworm-slim

WORKDIR /app

# Copy the native binary from the build stage
COPY --from=build /usr/src/app/media-task/target/media-task /app/task

# Run the application
CMD ["/app/task"]