#!/bin/bash

docker rm -f $(docker ps -aq) 2> /dev/null
sudo rm -rf docker/spark/checkpoints
mkdir docker/spark/checkpoints
