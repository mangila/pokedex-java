FROM ghcr.io/graalvm/native-image-community:21 AS build

WORKDIR /usr/src/app

COPY . .
# Compile and test all modules - with Spring Native Profile
RUN ./mvnw.sh clean install -Pnative
# Generate Native Image only for the target modules and skip tests
RUN ./mvnw.sh -o native:compile -DskipTests -Pnative -pl shared -pl mongodb-bouncer

FROM debian:bookworm-slim

WORKDIR /app

# Copy the native binary from the build stage
COPY --from=build /usr/src/app/mongodb-bouncer/target/mongodb-bouncer /app/bouncer

# Run the application
CMD ["/app/bouncer"]