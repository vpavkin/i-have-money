#!/usr/bin/env bash
docker run -i --net=host \
-e ihavemoney_writeback_db_user=admin \
-e ihavemoney_writeback_db_password=changeit \
-e ihavemoney_writeback_db_host=$HOST_IP \
-e ihavemoney_writeback_db_port=5432 \
-e ihavemoney_writeback_db_name=ihavemoney-write \
-e ihavemoney_readback_db_user=admin \
-e ihavemoney_readback_db_password=changeit \
-e ihavemoney_readback_db_host=$HOST_IP \
-e ihavemoney_readback_db_port=5432 \
-e ihavemoney_readback_db_name=ihavemoney-read \
-e ihavemoney_readback_host=$HOST_IP \
-e ihavemoney_readback_port=9201 \
--name readback -a stdin ihavemoney/read-backend
