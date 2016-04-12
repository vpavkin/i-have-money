#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
CREATE DATABASE "ihavemoney-read"
  WITH OWNER = admin
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.utf8'
       LC_CTYPE = 'en_US.utf8'
       CONNECTION LIMIT = -1;
CREATE DATABASE "ihavemoney-write"
 WITH OWNER = admin
	  ENCODING = 'UTF8'
	  TABLESPACE = pg_default
	  LC_COLLATE = 'en_US.utf8'
	  LC_CTYPE = 'en_US.utf8'
		  CONNECTION LIMIT = -1;
EOSQL

psql -v ON_ERROR_STOP=1 -d ihavemoney-read --username "$POSTGRES_USER" -f V1_0__ReadTables.sql
psql -v ON_ERROR_STOP=1 -d ihavemoney-write --username "$POSTGRES_USER" -f V1_0__Journals.sql
