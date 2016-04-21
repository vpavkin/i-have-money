#!/usr/bin/env bash
docker rm -f writeback writefront readback readfront
nohup ./docker-writeback.sh &
nohup ./docker-readback.sh &
nohup ./docker-readfront.sh &
nohup ./docker-writefront.sh &
