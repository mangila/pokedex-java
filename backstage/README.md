# backstage

Background jobs compiled a Native Image for FaaS(Function as a Service)

## To run

Will compile everything and install and generate native executables for your host system

* `mvn -U clean install` - compile and install all modules
* `mvn native:compile -Pnative -DskipTests` - generate native executables - this might take a while

## Requirements

For local development

* GraalVM for compiling the native Image

## Docker

Dockerfiles for the tasks has a graalvm image ready to compile

* use "host.docker.internal" as host if running locally
* see minikube.ps1 for build step

