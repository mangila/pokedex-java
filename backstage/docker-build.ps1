Write-Output "build api module docker images and create tarballs"
# Image-Converter : START
$dir = "./image-converter"
$dockerTag = "pokedex-image-converter";
docker build -f $dir/Dockerfile -t $dockerTag $dir
docker save -o "$dockerTag.tar" $dockerTag
# Image-Converter : END

# Pokeapi-Bouncer : START
$dir = "./pokeapi-bouncer"
$dockerTag = "pokedex-pokeapi-bouncer"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
docker save -o "$dockerTag.tar" $dockerTag
# Pokeapi-Bouncer : END

# Redis-Bouncer : START
$dir = "./redis-bouncer"
$dockerTag = "pokedex-redis-bouncer"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
docker save -o "$dockerTag.tar" $dockerTag
# Redis-Bouncer : END

# Mongodb-Bouncer : START
$dir = "./mongodb-bouncer"
$dockerTag = "pokedex-mongodb-bouncer"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
docker save -o "$dockerTag.tar" $dockerTag
# Mongodb-Bouncer : END

# Generation-Task : START
$dir = "./generation-task"
$dockerTag = "pokedex-generation-task"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
docker save -o "$dockerTag.tar" $dockerTag
# Generation-Task : END

# Pokemon-Task : START
$dir = "./pokemon-task"
$dockerTag = "pokedex-pokemon-task"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
docker save -o "$dockerTag.tar" $dockerTag
# Pokemon-Task : END

# Media-Task : START
$dir = "./media-task"
$dockerTag = "pokedex-media-task"
docker build -f ${dir}/Dockerfile -t ${dockerTag} .
docker save -o "$dockerTag.tar" $dockerTag
# Media-Task : END