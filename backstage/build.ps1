# Build with the Build Context from root
docker build -f ./generation-task/Dockerfile -t mangila/pokedex-generation-task:latest .
docker build -f ./pokemon-task/Dockerfile -t mangila/pokedex-pokemon-task:latest .