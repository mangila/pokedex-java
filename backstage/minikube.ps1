Write-Output "upload backstage module tarballs to minikube and apply k8s specs"

# Image-Converter : START
$dir = "./image-converter"
$dockerTag = "pokedex-image-converter";
minikube image load "$dockerTag.tar"
minikube kubectl -- delete -f $dir/k8s.yml
minikube kubectl -- apply -f $dir/k8s.yml
# Image-Converter : END

# Pokeapi-Bouncer : START
$dir = "./pokeapi-bouncer"
$dockerTag = "pokedex-pokeapi-bouncer"
minikube image load "$dockerTag.tar"
minikube kubectl -- delete -f $dir/k8s.yml
minikube kubectl -- apply -f $dir/k8s.yml
# Pokeapi-Bouncer : END

# Redis-Bouncer : START
$dir = "./redis-bouncer"
$dockerTag = "pokedex-redis-bouncer"
minikube image load "$dockerTag.tar"
minikube kubectl -- delete -f $dir/k8s.yml
minikube kubectl -- apply -f $dir/k8s.yml
# Redis-Bouncer : END

# Mongodb-Bouncer : START
$dir = "./mongodb-bouncer"
$dockerTag = "pokedex-mongodb-bouncer"
minikube image load "$dockerTag.tar"
minikube kubectl -- delete -f $dir/k8s.yml
minikube kubectl -- apply -f $dir/k8s.yml
# Mongodb-Bouncer : END

# Generation-Task : START
$dir = "./generation-task"
$dockerTag = "pokedex-generation-task"
minikube image load "$dockerTag.tar"
minikube kubectl -- delete -f $dir/k8s.yml
minikube kubectl -- apply -f $dir/k8s.yml
# Generation-Task : END

# Pokemon-Task : START
$dir = "./pokemon-task"
$dockerTag = "pokedex-pokemon-task"
minikube image load "$dockerTag.tar"
minikube kubectl -- delete -f $dir/k8s.yml
minikube kubectl -- apply -f $dir/k8s.yml
# Pokemon-Task : END

# Media-Task : START
$dir = "./media-task"
$dockerTag = "pokedex-media-task"
minikube image load "$dockerTag.tar"
minikube kubectl -- delete -f $dir/k8s.yml
minikube kubectl -- apply -f $dir/k8s.yml
# Media-Task : END