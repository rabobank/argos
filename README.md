ARGOS Supply Chain Notary ![Drone (cloud)](https://img.shields.io/drone/build/rabobank/argos) [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=rabobank_argos&metric=coverage)](https://sonarcloud.io/dashboard/index/rabobank_argos)
============
 Argos provides a framework to protect the integrity of a
 [**software supply chain**](docs/terminology/terminology.md#ssc). It
 provides a [**REST API**](docs/terminology/terminology.md#restAPI)
 implemented as a [**SAAS**](docs/terminology/terminology.md#saas) and
 client libraries in order to protect the integrity of
 [**artifacts**](docs/terminology/terminology.md#artifact) produced by a
 [**software supply chain**](docs/terminology/terminology.md#ssc).
 
It does so by verifying that each
[**step**](docs/terminology/terminology.md#step) in the chain is carried
out as planned, by authorized systems or personnel, and that the product
is not tampered with in transit.

It allows a
[**project owner**](docs/terminology/terminology.md#productOwner) to
create a layout. A [**layout**](docs/terminology/terminology.md#layout)
lists the sequence of steps of the software supply chain, and the
[**functionaries**](docs/terminology/terminology.md#functionary)
authorized to perform these steps. bla

When a [**functionary**](docs/terminology/terminology.md#functionary)
performs a [**step**](docs/terminology/terminology.md#step) it gathers
information about the used commands and the related files and sends it
to the **ARGOS service** in a
[**link**](docs/terminology/terminology.md#link) metadata file. As a
consequence [**link**](docs/terminology/terminology.md#link) files
provide the required evidence to establish a continuous chain that can
be [**verified**](docs/terminology/terminology.md#verification) against
the steps defined in the
[**layout**](docs/terminology/terminology.md#layout) nog een bla

The [**layout**](docs/terminology/terminology.md#layout), signed by the
[**project owners**](docs/terminology/terminology.md#productOwner)
together with the links, signed by the designated
[**functionaries**](docs/terminology/terminology.md#functionary) for a
particular [**supply chain run**](docs/terminology/terminology.md#scr),
can be verified by the service.

## <a name="architecture"/> Argos service architecture
In order to allow other parties to easily add in their own storage and
api implementations into the service. The architecture is organized
around the so called hexagonal architecture pattern.

The hexagonal architecture is based on three principles and techniques:

- Explicitly separate Application, Domain, and Infrastructure
- Dependencies are going from Application and Infrastructure to the Domain
- We isolate the boundaries by using Ports and Adapters

See also these articles for more information about this architectural pattern:

* [ports and adapters architecture](https://www.thinktocode.com/2018/07/19/ports-and-adapters-architecture/)
* [hexagonal architecture](https://blog.octo.com/en/hexagonal-architecture-three-principles-and-an-implementation-example/)


## Modules
-   argos4j
-   argos-docker
-   argos-domain
-   argos-jenkins-base
-   argos-jenkins-plugin
-   argos-service
-   argos-service-adapter-in-rest
-   argos-service-adapter-out-mongodb
-   argos-service-api
-   argos-service-domain
-   argos-test
   
 
### argos4j
Java client library for creating,signing and sending link files to the
Argos service.

### argos-docker
Docker compose file and Docker files used for running the Argos service
locally and in the drone build pipeline.

### argos-domain
Core domain entities shared between the argos4j and the argos service
modules.

### argos-jenkins-base
Jenkins docker base image used in argos-docker

### argos-jenkins-plugin
Plugin for jenkins that uses argos4j library to post signed link files
with each build step to the argos service.

### argos-service
[Spring Boot](https://spring.io/projects/spring-boot) Java service to
expose the REST API

### argos-service-adapter-in-rest
Incoming adapter implementing the
[open api](https://swagger.io/specification/) REST specification. This
api is defined in the argos-service-api module. 
( See [architecture paragraph](#architecture) )

### argos-service-adapter-out-mongodb
Outgoing adapter using mongo db to implement the repository interfaces
defined in the argos-service-domain module. ( See [architecture
paragraph](#architecture) )
### argos-service-api
[Open api](https://swagger.io/specification/) specification for the
Argos Service endpoints.

### argos-service-domain
Domain entities and interfaces specifically for the argos service.

### argos-test

Integration test module to run integration tests locally or as step in a
drone pipeline.


## How to run
See [developer documentation](docs/DEVELOPER.md)


## Decisions

See [ADRs](docs/adr/index.md)

## Contributing 

See [contributing to Argos](CONTRIBUTING.md)

