# backstage

Background jobs compiled a Native Image for FaaS(Function as a Service) <br>
when running as a Function in K8s

* `mvn -U clean install`
* `mvn native:compile -Pnative -DskipTests`

## Requirements

* GraalVM for compiling for the native Image
    * https://www.graalvm.org/latest/getting-started/windows/ - Windows install
* MiniKube to run apply into the cluster
    * https://winget.run/pkg/Kubernetes/minikube - Windows install