# Stage 1: Build
FROM golang:1.24 AS builder
WORKDIR /usr/src/app
COPY . .
RUN go mod download && go mod verify
RUN go build -v -o image-converter

# Stage 2: Run
FROM alpine:latest
WORKDIR /app
COPY --from=builder /usr/src/app/image-converter .
CMD ["./image-converter"]