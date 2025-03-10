package server

import (
	"fmt"
	"google.golang.org/grpc"
	"image-converter/service"
	"log"
	"net"
)

func Init(port string) {
	listener, err := net.Listen("tcp", fmt.Sprintf(":%s", port))
	if err != nil {
		log.Fatalf("failed to listen: %v", err)
	}
	grpcServer := grpc.NewServer()
	service.RegisterImageConverterServer(grpcServer, &service.ConverterService{})
	log.Printf("server listening at %v", listener.Addr())
	err = grpcServer.Serve(listener)
	if err != nil {
		log.Fatalf("failed to start grpc server: %v", err)
	}
}
