#!/bin/bash

sbt clean && \
sbt compile && \
sbt assembly && \
cp target/scala-2.13/2022SpringSparkJob.jar docker/spark/apps && \
cd docker && docker-compose up -d
