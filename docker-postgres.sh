#!/usr/bin/env bash
docker run --net=host \
-e POSTGRES_USER=admin \
-e POSTGRES_PASSWORD=changeit \
--name ihavemoney-postgres -d postgres:9.4
