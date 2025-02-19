# Maven - create .jar files in api module
cmd.exe /c mvnw.cmd -f ./pom.xml clean package

docker build -f ./file-api/Dockerfile -t mangila/pokedex-file-api:latest ./file-api
docker build -f ./graphql-api/Dockerfile -t mangila/pokedex-graphql-api:latest ./graphql-api