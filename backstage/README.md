# backstage

Background jobs compiled a Native Image for FaaS(Function as a Service)

* `mvn -U clean install`
* `mvn native:compile -Pnative -DskipTests`

## Requirements

* GraalVM for compiling the native Image
    * https://www.graalvm.org/latest/getting-started/windows/ - Windows install