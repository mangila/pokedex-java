Write-Output "upload api module tarballs to minikube and apply k8s specs"

# File-Api : START
$dir = "./file-api"
$dockerTag = "pokedex-file-api";
minikube image load "$dockerTag.tar"
minikube kubectl -- delete -f $dir/k8s.yml
minikube kubectl -- apply -f $dir/k8s.yml
# File-Api : END

# Graphql-Api : START
$dir = "./graphql-api"
$dockerTag = "pokedex-graphql-api";
minikube image load "$dockerTag.tar"
minikube kubectl -- delete -f $dir/k8s.yml
minikube kubectl -- apply -f $dir/k8s.yml
# Graphql-Api : END