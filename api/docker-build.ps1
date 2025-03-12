Write-Output "build api module docker images and create tarballs"
# Maven - create .jar files in api module
cmd.exe /c mvnw.cmd -f ./pom.xml clean package

# File-Api : START
$dir = "./file-api"
$dockerTag = "pokedex-file-api";
docker build -f $dir/Dockerfile -t $dockerTag $dir
docker save -o "$dockerTag.tar" $dockerTag
# File-Api : END

# Graphql-Api : START
$dir = "./graphql-api"
$dockerTag = "pokedex-graphql-api";
docker build -f $dir/Dockerfile -t $dockerTag $dir
docker save -o "$dockerTag.tar" $dockerTag
# Graphql-Api : END