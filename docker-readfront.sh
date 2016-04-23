#!/usr/bin/env bash
docker run -i --net=host \
-e ihavemoney_readback_host=$HOST_IP \
-e ihavemoney_readback_port=9201 \
-e ihavemoney_readfront_host=$HOST_IP \
-e ihavemoney_readfront_http_port=8201 \
-e ihavemoney_readfront_tcp_port=10201 \
-e ihavemoney_writefront_host=$HOST_IP \
-e ihavemoney_writefront_port=8101 \
--name readfront -a stdin ihavemoney/read-frontend
