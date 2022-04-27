# Kappa Architecture with Kafka, Spark Structured Streaming
My take on implementing a pipeline based on the Kappa Architecture by Jay Kreps to solve a real world problem.

## Current Status
This project is currently partly implemented. The progress so far has been to implement `Docker` containers for the required services, and ensure connectivity between the main stages of the pipeline, namely between the data sources, the `Kafka Producer`, the `Kafka Broker`, `Spark Structured Streaming`, `PostgreSQL`, `Logstash`, `Elasticsearch`, and `Kibana`. This current progress also includes the implementation of some simple transformations of the data by `Spark Structured Streaming`. I have not implemented a full `Kafka Producer` that ingests data from all the sources, only from one data source so far. I have also not yet explored the data visualization side of this pipeline yet. These two issues will be the focus of my efforts in the coming week.

## Basic Architecture
![alt text](assets/KappaArchitecture.png)

A `Kafka` (with `Zookeeper`) message broker  ingests data from a fictional API in `JSON` format, with the help of a `Kafka Producer` written in the `Python` programming language. `Spark Structured Streaming` is used to transform and aggregate the ingested data. `Spark` will then write the processed data back to the `Kafka Broker`, as well as a relational database (`PostgreSQL`). This is done to handle both the real-time analytics queries using an ELK (`Elasticsearch`, `Logstash`, `Kibana`) stack, as well as the batch analytics queries (`Django` web application hosting a framework).

`Kafka`, `Zookeeper`, `Spark`, `PostgreSQL`, `Logstash`, `Elasticsearch`, and `Kibana` are currently containerized using `Docker` as this is still at a proof of concept stage. It is envisioned that this project will investigate scaling once the business logic has been implemented in full, either through deployment to a cloud service like `AWS` or `Azure`, or through an increased number of local instances.

## Usage
First, run the script `init.sh` to compile and assemble the `Spark` job `jar` file that will be later submitted to `Spark`. This script also starts up the required `Docker` containers, and copies the assembled `jar` file into the shared volume folder for the `Spark` container.

In a seperate terminal, use the `Spark`'s `Docker` terminal to submit the `Spark` job's `jar` file assembled in the initial step.
1. `docker exec -it spark-master /bin/bash`
2. `./opt/spark/bin/spark-submit --class StreamHandler --master local[*] --packages org.apache.spark:spark-sql-kafka-0-10_2.13:3.2.1 /opt/spark-apps/2022SpringSparkJob.jar`

Once the `Spark` application is started, the `Kafka Producer` will need to be run in order to ingest data into this pipeline. Run `python ./kafka-producer/producer.py` once the required `Python` environment is setup using the `/kafka-producer/requirements.txt` file. If everything was setup correctly, a line for each message sent should be printed onto the console.

To verify the data has been correctly processed on the real-time layer side, run the command `kafka-console-consumer.sh --topic waterStandpipeOut --broker-list localhost:9092`. This places the current terminal in a polling mode, waiting for transformed messages to arrive. To carry out the corresponding verification for the batch layer side, use the following commands to inspect the `PostgreSQL` database to see the written data in it's transformed state:
1. step 1...
2. step 2...

To check that the pipeline correctly pushes data into `Logstash`, `Elasticsearch`, and `Kibana`, check the following two URLs in a web browser:
- `http://localhost:9200` for `Elasticsearch`
- `http://localhost:5601` for `Kibana`

The `Kibana` GUI should successfully display an index pattern for the water standpipe monitoring data.

I have not yet implemented the respective dashboards for the real-time and batch layer views, and this will be a focus of the next week.

## Big Data Technologies Used
- `Zookeeper`
- `Kafka`
- `Spark Structured Streaming`
- `PostgreSQL`
- `Logstash`
- `Elasticsearch`
- `Kibana`
