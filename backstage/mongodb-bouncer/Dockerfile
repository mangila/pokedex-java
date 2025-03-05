# Stage 1: Build
FROM ghcr.io/graalvm/native-image-community:21 AS buil
WORKDIR /usr/src/app
COPY . .
# Compile and test all modules - with Spring Native Profile
RUN ./mvnw.sh clean install -Pnative
# Generate Native Image only for the target modules and skip tests
RUN ./mvnw.sh -o native:compile -DskipTests -Pnative -pl shared -pl mongodb-bouncer

# Stage 2: Run
FROM debian:bookworm-slim
WORKDIR /app
COPY --from=build /usr/src/app/mongodb-bouncer/target/mongodb-bouncer .
CMD ["./mongodb-bouncer"]