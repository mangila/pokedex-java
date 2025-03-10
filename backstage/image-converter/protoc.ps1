# Compile the Proto with Golang - grpc and go
protoc --proto_path=..\shared\src\main\proto --go_out=.\model --go_opt=paths=source_relative ..\shared\src\main\proto\model.proto
protoc --proto_path=..\shared\src\main\proto --go_out=.\service --go-grpc_out=.\service --go_opt=paths=source_relative --go-grpc_opt=paths=source_relative ..\shared\src\main\proto\service.proto