FROM ghcr.io/graalvm/native-image-community:21 AS build

WORKDIR /usr/src/app

COPY . .
# Compile modules with Spring Native Profile
RUN ./mvnw.sh clean install -Pnative -DskipTests -pl model -pl redis-bouncer
# Generate Native Image
RUN ./mvnw.sh -o native:compile -Pnative -DskipTests -pl model -pl redis-bouncer

FROM debian:bookworm-slim

WORKDIR /app

# Copy the native binary from the build stage
COPY --from=build /usr/src/app/redis-bouncer/target/redis-bouncer /app/bouncer

# Run the application
CMD ["/app/bouncer"]