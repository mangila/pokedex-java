# Image-Converter : START
$dir = "./image-converter"
$dockerTag = "mangila/pokedex-image-converter:latest";
docker build -f ${dir}/Dockerfile -t ${dockerTag} ${dir}
minikube image load ${dockerTag}
#kubectl --apply -f ${dir}/k8s.yml
# Image-Converter : END

# Pokeapi-Bouncer : START
$dir = "./pokeapi-bouncer"
$dockerTag = "mangila/pokedex-pokeapi-bouncer:latest"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
minikube image load ${dockerTag}
#kubectl --apply -f ${dir}/k8s.yml
# Pokeapi-Bouncer : END

# Redis-Bouncer : START
$dir = "./redis-bouncer"
$dockerTag = "mangila/pokedex-redis-bouncer:latest"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
minikube image load ${dockerTag}
#kubectl --apply -f ${dir}/k8s.yml
# Redis-Bouncer : END

# Mongodb-Bouncer : START
$dir = "./mongodb-bouncer"
$dockerTag = "mangila/pokedex-mongodb-bouncer:latest"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
minikube image load ${dockerTag}
#kubectl --apply -f ${dir}/k8s.yml
# Mongodb-Bouncer : END

# Generation-Task : START
$dir = "./generation-task"
$dockerTag = "mangila/pokedex-generation-task:latest"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
minikube image load ${dockerTag}
#kubectl --apply -f ${dir}/k8s.yml
# Generation-Task : END

# Pokemon-Task : START
$dir = "./pokemon-task"
$dockerTag = "mangila/pokedex-pokemon-task:latest"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
minikube image load ${dockerTag}
#kubectl --apply -f ${dir}/k8s.yml
# Pokemon-Task : END

# Media-Task : START
$dir = "./media-task"
$dockerTag = "mangila/pokedex-media-task:latest"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
minikube image load ${dockerTag}
#kubectl --apply -f ${dir}/k8s.yml
# Media-Task : END
