# api

Public accessed apis.

## file-api

Fetch files from the GridFs database

- `minikube service file-api-service -n pokedex --url` - to get minikube tunnel ip and access from localhost

* /api/v1/file/{fileName}?download=true|false

## graphql-api

Query from pokemon-species database via Graphql queries.

- `minikube service graphql-api-service -n pokedex --url` - to get minikube tunnel ip and access from localhost

* /api/v1/graphql
* /graphiql