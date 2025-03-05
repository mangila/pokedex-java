package main

import (
	_ "github.com/joho/godotenv/autoload"
	"image-converter/server"
	"os"
)

func main() {
	server.Init(os.Getenv("PORT"))
}
