$generationTaskDir = "./generation-task"
$pokemonTaskDir = "./pokemon-task"
$mediaTaskDir = "./media-task"
$imageConverterDir = "./image-converter"
$redisBouncerkDir = "./redis-bouncer"
$mongoDbBouncerkDir = "./mongodb-bouncer"

# Image-Converter
docker build -f ${imageConverterDir}/Dockerfile -t mangila/pokedex-image-converter:latest ${imageConverterDir}
# Build images - start build context from root, since we need the shared modules compiled
# Tasks
docker build -f ${generationTaskDir}/Dockerfile -t mangila/pokedex-generation-task:latest .
docker build -f ${pokemonTaskDir}/Dockerfile -t mangila/pokedex-pokemon-task:latest .
docker build -f ${mediaTaskDir}/Dockerfile -t mangila/pokedex-media-task:latest .
# Bouncer
docker build -f ${redisBouncerkDir}/Dockerfile -t mangila/pokedex-redis-bouncer:latest .
docker build -f ${mongoDbBouncerkDir}/Dockerfile -t mangila/pokedex-mongodb-bouncer:latest .
# K8s - deploy specs
kubectl -- apply -f ${imageConverterDir}/k8s.yml
kubectl -- apply -f ${generationTaskDir}/k8s.yml
kubectl -- apply -f ${pokemonTaskDir}/k8s.yml
kubectl -- apply -f ${mediaTaskDir}/k8s.yml
kubectl -- apply -f ${redisBouncerkDir}/k8s.yml
kubectl -- apply -f ${mongoDbBouncerkDir}/k8s.yml