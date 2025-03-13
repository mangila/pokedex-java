# api

Public accessed apis.

## file-api

Fetch files from the GridFs database

* /api/v1/file/{fileName}?download=true|false

## graphql-api

Query from pokemon-species database via Graphql queries.

- `minikube service graphql-api-service -n pokedex --url` - to get minikube tunnel ip

* /api/v1/graphql
* /graphiql