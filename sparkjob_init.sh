#!/bin/bash

# clean the environment first
./clean_env.sh

# setup jar file and copy over to spark shared volume
sbt clean && \
sbt compile && \
sbt assembly && \
cp target/scala-2.13/2022SpringSparkJob.jar docker/spark/apps && \

# start docker
cd docker && docker-compose up -d
