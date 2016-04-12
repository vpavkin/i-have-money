#!/usr/bin/env bash
cp ./read-backend/src/main/resources/db/migrations/V1_0__ReadTables.sql ./docker-postgres/V1_0__ReadTables.sql
cp ./write-backend/src/main/resources/db/migrations/V1_0__Journals.sql ./docker-postgres/V1_0__Journals.sql
docker build -t ihavemoney/postgres ./docker-postgres && \
docker run --net=host \
-e POSTGRES_USER=admin \
-e POSTGRES_PASSWORD=changeit \
--name ihavemoney-postgres -d ihavemoney/postgres
