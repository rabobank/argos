ARGOS Supply Chain Notary
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
authorized to perform these steps.

When a [**functionary**](docs/terminology/terminology.md#functionary)
performs a [**step**](docs/terminology/terminology.md#step) it gathers
information about the used commands and the related files and sends it
to the **ARGOS service** in a
[**link**](docs/terminology/terminology.md#link) metadata file. As a
consequence [**link**](docs/terminology/terminology.md#link) files
provide the required evidence to establish a continuous chain that can
be [**verified**](docs/terminology/terminology.md#verification) against
the steps defined in the
[**layout**](docs/terminology/terminology.md#layout)

The [**layout**](docs/terminology/terminology.md#layout), signed by the
[**project owners**](docs/terminology/terminology.md#productOwner)
together with the links, signed by the designated
[**functionaries**](docs/terminology/terminology.md#functionary) for a
particular [**supply chain run**](docs/terminology/terminology.md#scr),
can be verified by the service.

## Modules

 
### argos4j
client library for java to allow 



## How to run
See [developer documentation](docs/DEVELOPER.md)


## Decisions

See [ADRs](docs/adr/index.md)

## Contributing 

See [contributing to Argos](CONTRIBUTING.md)

