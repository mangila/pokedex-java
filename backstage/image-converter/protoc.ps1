# Compile the Proto with Golang - grpc and go
protoc --proto_path=..\shared\src\main\proto --go_out=. --go-grpc_out=. ..\shared\src\main\proto\image_converter.proto