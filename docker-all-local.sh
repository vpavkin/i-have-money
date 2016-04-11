#!/usr/bin/env bash
nohup ./docker-writeback.sh &
nohup ./docker-readback.sh &
nohup ./docker-readfront.sh &
nohup ./docker-writefront.sh &
