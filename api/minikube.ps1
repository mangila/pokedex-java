$fileApiDir = "./file-api"
$graphqlApiDir = "./graphql-api"

# Maven - create .jar files in api module
cmd.exe /c mvnw.cmd -f ./pom.xml clean package
# Docker - build images
docker build -f ${fileApiDir}/Dockerfile -t mangila/pokedex-file-api:latest ${fileApiDir}
docker build -f ${graphqlApiDir}/Dockerfile -t mangila/pokedex-graphql-api:latest ${graphqlApiDir}
# K8s - deploy specs
kubectl -- apply -f ${graphqlApiDir}/k8s.yml
kubectl -- apply -f ${fileApiDir}/k8s.yml