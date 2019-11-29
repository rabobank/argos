ARGOS Supply Chain Notary
============
Provides a rest API implemented as **SAAS** and client libraries to
protect the integrity of **artifacts** produced by a **software supply
chain**.
 
It does so by verifying that each **step** in the chain is carried out
as planned, by authorized systems or personnel, and that the product is
not tampered with in transit.

It allows a
[**project owner**](docs/terminology/terminology.md#productOwner) to
create a layout. A **layout** lists the sequence of steps of the
software supply chain, and the **functionaries** authorized to perform
these steps.

When a **functionary** performs a **step** it gathers information about
the used commands and the related files and sends it to the **ARGOS
servic**e in a link metadata file. As a consequence link files provide
the required evidence to establish a continuous chain that can be
validated against the steps defined in the **layout**.

The layout, signed by the **project owners**, together with the links,
signed by the designated functionaries for a particular **supply chain
run**, can be verified by the service.

## Modules

 
### argos4j
client library for java to allow 



## How to run

### Ports


## Setup local environment


## Local configuration


## Docker


**Notice:** Make sure to have all required dependencies installed
- Java JDK8 <br/>
`Please look up instructions for your OS`
- Docker <br/>
`Make sure permissions are correctly set up`
- Docker-Compose <br/>
`Make sure permissions are correctly set up`
- Maven <br/>
`sudo apt-get install maven`
- Node & NPM <br/>
`sudo apt-get install npm`
- Chrome <br/>
`For Ubuntu, find instructions here`<br/>
[Please follow these instructions](https://askubuntu.com/a/510186)

## Decisions

See [ADRs](docs/adr/index.md)