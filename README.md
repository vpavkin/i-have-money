# I Have Money
Rich-featured example of event-sourced application with full CQRS support.

Built with akka, fun-cqrs, circe, shapeless, scalajs, scalajs-react and other libraries.

## Domain and Purpose
"I Have Money" is an application for tracking expenses, income and assets with multi-currency support.

Example version supports only Income and Expense commands on the write-side, and querying current balance from the read-side.

## Features
The intent of the example is to solve several problems, that arise for ES/CQRS applications, and get the solutions to work together. From this standpoint "I Have Money" has following features:

* Totally separated command and query services
* Domain logic encoded with the help of [fun-cqrs](https://github.com/strongtyped/fun-cqrs).
* AggregateId based [cluster sharding](http://doc.akka.io/docs/akka/2.4.3/scala/cluster-sharding.html) for write backend
* Efficient [Protobuf](https://developers.google.com/protocol-buffers/) serialization with the help of [sbt-scalapb](https://github.com/trueaccord/sbt-scalapb) and [protoc-jar](https://github.com/os72/protoc-jar) (command side only).
* Read and write backends are hidden behind respective HTTP API frontends (built with [akka-http](http://doc.akka.io/docs/akka/2.4.3/scala/http/)), that validate requests and forward them to the backends.
* PostgreSQL based journal and query-side databases.
  * Schemas defined with [Slick](http://slick.typesafe.com/).
  * Migrations are managed with [Flyway](https://flywaydb.org/).
  * Akka Persistence and Persistent Query implementations are supplied by [akka-persistence-jdbc](https://github.com/dnvriend/akka-persistence-jdbc). Some gotchas here:
    * Manual workaround is needed to handle [differences](https://github.com/strongtyped/fun-cqrs/issues/49) in Persistent Query offset interpretation between fun-cqrs and akka-persistence-jdbc.
    * Event stream for persistent query is _live_ only when respective persistent actors [are on the same machine](https://github.com/dnvriend/akka-persistence-jdbc/issues/39). A live stream has to be constructed by hand in case of distributed scenario, when one node is writing the journal and another is polling it for a query side projection.
* Each application can be assembled in a single .jar file and wrapped in a docker container for easier deployment (see [Launching with Docker](#running-the-example)).
* A web UI is served by the Read Frontend application. It's mobile friendly and written with [scalajs-react](https://github.com/japgolly/scalajs-react).
* Single HTTP protocol definition is [cross-compiled](https://www.scala-js.org/doc/project/cross-build.html) and used by both web UI and HTTP frontends. [Circe](http://circe.io) takes care of JSON serialization.

## Applications

Each of listed applications can be launched on separate node, with write backend being able to work on a cluster.

Single machine deployment is also supported.

### Write Backend
Receives domain commands from Write Frontend via cluster recipient, handles them and stores resulting events in a postgreSQL journal. Can be deployed in a sharded cluster.

### Read Backend
Polls the event stream from the journal, projects it to the current "state" (active balance in "I Have Money" domain) and stores it into a query-side postgreSQL database.

Also it handles query messages from Read Frontend and responds with current balance for requested aggregate.

### Write Frontend
HTTP API for sending commands to the system. After validating the HTTP request transforms it and sends to the Write Backend cluster.

Can be also used for circuit breaking (not implemented in this example)

### Read Frontend
HTTP API for sending queries. Transforms HTTP requests to Read Backend messages and forwards them there.

Also serves the web UI, that can be used to send commands and queries in a visual way.

## Other modules

### Domain
Domain entities and behaviour definitions. Some data classes are cross-compiled to be available for Frontend Protocol module.

### Serialization
Contains tools for converting between storage/network and domain message formats.

Defines protobuf protocols for write-side messages.

### Frontend Protocol
Cross-project, that contains message protocol definitions for read-frontend and write-frontend. Is used both by web UI and read/write backends.

Cross-compilation from single source guarantees protocol implementations consistency at compile time.

### JS App (Web UI)
HTML interface written in scala-js on top of scalajs-react framework. Allows for sending commands to Write Frontend and sending queries to Read Frontend.

## Running the example

Default configuration deploys all the apps on the localhost and uses same database server (but different DBs) for write and read sides.

All configuration required to set up a distributed deployment can be defined through run parameters or environment variables.

Next we'll see how to run the example on localhost with default configuration. There are two steps to be made, and each has two options. You can choose on your preference or availability.

###1. Setting up PostgreSQL database

#### Docker image
if you don't have Postgres installed, just run `./docker-postgres.sh`. This will run a docker container with PostgreSQL instance that is already configure with all "I Have Money" schemas.

The container will use port 5432 of your host machine or docker VM. Of course, docker has to be configured for this to run.

#### Locally installed PostgreSQL
Those, who **have postgreSQL installed locally**:

* Either add "admin" user with password "changeit", or change the credentials in build.sbt or docker launch scripts (depending on the way you are going to launch the app).
* Run `sbt readBackend/flywayMigrate` and `sbt writeBackend/flywayMigrate` to prepare the schema.
* Ensure that either your Postgres instance is available on localhost:5432 or change the host/port in build.sbt/launch scripts.

###2. Running the apps

#### Running with sbt
In most cases it's enough to do just this:

```bash
sbt writeBackend/run
sbt readBackend/run
sbt writeFrontend/run
sbt readFrontend/run
```
Everything will work with default settings if you have PostgreSQL on 127.0.0.0:5432

#### Running with docker
First build all the containers:

```bash
sbt docker
```

On Linux run `./docker-all-local.sh`.
On MacOS X run `./docker-all-vm.sh`

## Open the web UI

Go to 127.0.0.1:8201 with your favourite browser.

Change the IP to Docker VM IP in case running with docker on MacOS X.
