#!/bin/bash

# clean the environment first
./clean.sh

# setup jar file and copy over to spark shared volume
sbt clean compile assembly && \
cp target/scala-2.13/2022SpringSparkJob.jar docker/spark/apps && \

# start docker
cd docker && docker-compose up -d
