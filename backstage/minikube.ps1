$generationTaskDir = "./generation-task"
$pokemonTaskDir = "./pokemon-task"
$mediaTaskDir = "./media-task"

# Docker - build images - start build context from root, since we need shared module compiled
docker build -f ${generationTaskDir}/Dockerfile -t mangila/pokedex-generation-task:latest .
docker build -f ${pokemonTaskDir}/Dockerfile -t mangila/pokedex-pokemon-task:latest .
docker build -f ${mediaTaskDir}/Dockerfile -t mangila/pokedex-media-task:latest .
# K8s - deploy specs
kubectl -- apply -f ${generationTaskDir}/k8s.yml
kubectl -- apply -f ${pokemonTaskDir}/k8s.yml
kubectl -- apply -f ${mediaTaskDir}/k8s.yml