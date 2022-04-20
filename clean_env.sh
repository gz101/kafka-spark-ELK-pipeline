#!/bin/bash

docker rm -f $(docker ps -aq) 2> /dev/null
sbt clean
rm -rf docker/spark/checkpoints
mkdir docker/spark/checkpoints