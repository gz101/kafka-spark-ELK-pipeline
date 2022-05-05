# Kappa Architecture with Kafka, Spark Structured Streaming
My take on implementing a pipeline based on the Kappa Architecture by Jay Kreps to solve a real world problem.

## Current Status
This project has been completed in its local state. The progress so far has been to implement `Docker` containers for the required services, and ensure connectivity between the main stages of the pipeline, namely between the data sources, the `Kafka Producer`, the `Kafka Broker`, `Spark Structured Streaming`, `PostgreSQL`, `Logstash`, `Elasticsearch`, and `Kibana`. A unit testing suite has also been included. The target of the next few days is to deploy this application at scale to the cloud.

## Basic Architecture
![alt text](assets/KappaArchitecture.png)

A `Kafka` (with `Zookeeper`) message broker  ingests data from a fictional API in `JSON` format, with the help of a `Kafka Producer` written in the `Python` programming language. `Spark Structured Streaming` is used to transform and aggregate the ingested data. `Spark` will then write the processed data back to the `Kafka Broker`, as well as a relational database (`PostgreSQL`). This is done to handle both the real-time analytics queries using an ELK (`Elasticsearch`, `Logstash`, `Kibana`) stack, as well as the batch analytics queries (`Django` web application hosting a framework).

`Kafka`, `Zookeeper`, `Spark`, `PostgreSQL`, `Logstash`, `Elasticsearch`, and `Kibana` are currently containerized using `Docker`. It is envisioned that this project will investigate scaling once the business logic has been implemented in full, either through deployment to a cloud service like `AWS` or `Azure`, or through an increased number of local instances.

## Usage
For instructions to run, refer to Milestone 2 Submission template.

## Big Data Technologies Used
- `Zookeeper`
- `Kafka`
- `Spark Structured Streaming`
- `PostgreSQL`
- `Logstash`
- `Elasticsearch`
- `Kibana`
