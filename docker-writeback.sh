#!/usr/bin/env bash
docker run -i --net=host \
-e ihavemoney_writeback_db_user=admin \
-e ihavemoney_writeback_db_password=changeit \
-e ihavemoney_writeback_db_host=$HOST_IP \
-e ihavemoney_writeback_db_port=5432 \
-e ihavemoney_writeback_db_name=ihavemoney-write \
-e ihavemoney_writeback_host=$HOST_IP \
-e ihavemoney_writeback_port=9101 \
--name writeback -a stdin ihavemoney/write-backend
