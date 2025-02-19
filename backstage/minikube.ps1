$generationTaskDir = "./generation-task"
$pokemonTaskDir = "./pokemon-task"

# Docker - build images - start context from root
docker build -f ${generationTaskDir}/Dockerfile -t mangila/pokedex-generation-task:latest .
docker build -f ${pokemonTaskDir}/Dockerfile -t mangila/pokedex-pokemon-task:latest .
# K8s - deploy specs
kubectl -- apply -f ${generationTaskDir}/k8s.yml
kubectl -- apply -f ${pokemonTaskDir}/k8s.yml