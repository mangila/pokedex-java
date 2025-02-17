# Maven - clean and package
cmd.exe /c mvnw.cmd clean package
# Docker - build
docker build -f ./scheduler/Dockerfile -t mangila/pokedex-scheduler:latest ./scheduler
docker build -f ./graphql-server/Dockerfile -t mangila/pokedex-graphql-server:latest ./graphql-server
docker build -f ./file-server/Dockerfile -t mangila/pokedex-file-server:latest ./file-server
# K8s apply
kubectl -- apply -f config-k8s.yml
kubectl -- apply -f secret-k8s.yml
kubectl -- apply -f service-k8s.yml
kubectl -- apply -f /graphql-server/k8s.yml
kubectl -- apply -f /file-server/k8s.yml
kubectl -- apply -f /scheduler/k8s.yml