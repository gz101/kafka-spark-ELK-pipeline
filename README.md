# Kappa Architecture with Kafka, Spark Structured Streaming
My take on implementing a pipeline based on the Kappa Architecture by Jay Kreps to solve a real world problem.

## Current Status
This project is still at a proof of concept stage. The progress so far has been to implement `Docker` containers for the required services, and ensure connectivity between the main stages of the pipeline, namely from `Kafka` to `Spark` with some transformation, and then back to `Kafka` again, writing to different topics. I have not yet explored adding `Cassandra` or a data visualization stage yet. The data transformation performed in this proof of concept is near non-existent at this stage, if only to help ensure connectivity between `Kafka` and `Spark` within the `Docker` container.

## Basic Architecture
![alt text](assets/KappaArchitecture.png)

A `Kafka` (with `Zookeeper`) message broker  ingests data from a fictional API in `JSON` format. `Structured Streaming` by `Spark` is used to transform and aggregate the ingested data over several topics and returned into selected `Kafka` topics. These topics are then used to either serve real-time views for data analysis/visualization or stored in a `Cassandra` database for batch analysis.

`Kafka`, `Zookeeper`, `Spark`, and `Cassandra` are currently containerized using `Docker` as this is still at a proof of concept stage. It is envisioned that this project will investigate scaling once the business logic has been implemented in full, either through deployment to a cloud service like `AWS` or `Azure`, or through an increased number of local instances.

## Usage
First, run the script `sparkjob_init.sh` to compile and assemble the `Spark` job `jar` file that will be later submitted to `Spark`. This script also starts up the required `Docker` containers, and copies the assembled `jar` file into the shared volume folder for the `Spark` container.

Next, create some `Kafka` topics, one for incoming (`messages`), and one for outgoing (`responses`) using `Kafka`'s `Docker` terminal. Then leave this `Kafka` terminal in polling mode, waiting for a message to be proceed and returned into the `responses` topic. Any successful piece of data which travels through the pipeline will be displayed on the console.
1. `docker exec -it kafka /bin/sh`
2. `kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic messages`
3. `kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic responses`
4. `kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic responses --from-beginning`

In a seperate terminal, use the `Spark`'s `Docker` terminal to submit the `Spark` job's `jar` file assembled in the initial step.
1. `docker exec -it spark-master /bin/bash`
2. `./opt/spark/bin/spark-submit --class StreamHandler --master local[*] --packages org.apache.spark:spark-sql-kafka-0-10_2.13:3.2.1 /opt/spark-apps/2022SpringSparkJob.jar`

Now open another new terminal window to start manually feeding data to `Kafka`. Any string can be input into the prompt for testing after running the command `kafka-console-producer.sh --topic messages --broker-list localhost:9092`. As each line is typed into this terminal, a transformed version of this message will show up in the first terminal's console.

## Big Data Technologies Used
- `Zookeeper`
- `Kafka`
- `Spark Structured Streaming`
- `Cassandra`
- Undecided for data visualization/analysis so far - perhaps `Kibana` + `Elastic Search`
