#!/usr/bin/env bash
docker run -i --net=host \
-e ihavemoney_readback_host=127.0.0.1 \
-e ihavemoney_readback_port=9201 \
-e ihavemoney_readfront_host=127.0.0.1 \
-e ihavemoney_readfront_http_port=8201 \
-e ihavemoney_readfront_tcp_port=10201 \
--name readfront -a stdin ihavemoney/read-frontend
