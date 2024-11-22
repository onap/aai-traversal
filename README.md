# OpenECOMP AAI Traversal

## Introduction
OpenECOMP AAI Traversal is delivered with multiple docker containers with hbase, and gremlin docker container preinstalled and also have a aai-haproxy container installed for routing requests properly between resources and graph query docker containers

For demo app use case you can install all three of the containers in one machine. Configuration and deployment of hbase for any other use cases should be evaluated and updated accordingly.

## Development
### Compile

AAI can be compiled with
``` bash
mvn clean install -DskipTests
```
### Test
``` bash
mvn test
```
Run individual tests
```
mvn test -Dtest=EchoResponseTest#testEchoResultWhenValidHeaders
```

### Run
Run the project with
``` bash
mvn -N -P runAjsc
```
Make sure to be in `aai-traversal` before executing it!

## Accessing AAI APIs
Most of the AAI features within OpenECOMP are triggered by using **RESTful interfaces**. AAI  is configured on this release with HTTPS only using Basic Authentication. Two way SSL using client certificates should be considered and used for non demo use case deployments.

The MSO APIs are configured to accept requests having a **basic auth. header** set with various **username and password** depending on which client is triggering the request. The realm.properties contains the credentials for the OpenECOMP components and these should be changed as appropriate.

All API endpoints are exposed on port **8443**.

##### Example API endpoints in the first open source release
http://aai.api.simpledemo.openecomp.org:8443/aai/v10/query

The easy way to trigger these endpoints is to use a RESTful client or automation framework. HTTP GET/PUT/DELETE are supported for most resource endpoints. More information on the REST interface can be found in the AAI Service REST API specification.

## Configuring AAI
The Docker containers use a Chef based configuration file (JSON) in order to provision AAI basic configuration for the demo app use case set up.

## Testing AAI Functionalities
Any RESTful client such as SoapUI may be configured and setup to use for testing AAI requests.

## Integration Tests
Integration tests are located in `it` directory, and disabled by default in the pom file:
`<skipITs>true</skipITs>`

As a naming convention, All integration test classes should end with `IT`, and will be executed by changing the `skipITs` value in pom file, or through the command line `-DskipITs=false`
