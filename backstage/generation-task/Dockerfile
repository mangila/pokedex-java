FROM ghcr.io/graalvm/native-image-community:23 AS build

WORKDIR /usr/src/app

COPY . .
# Compile modules with Spring Native Profile
RUN ./mvnw.sh clean install -Pnative -DskipTests -pl shared -pl generation-task
# Generate Native Image
RUN ./mvnw.sh -o native:compile -Pnative -DskipTests -pl shared -pl generation-task

FROM debian:bookworm-slim

WORKDIR /app

# Copy the native binary from the build stage
COPY --from=build /usr/src/app/generation-task/target/generation-task /app/task

# Run the application
CMD ["/app/task"]