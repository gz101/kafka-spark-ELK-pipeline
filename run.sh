#!/bin/bash

source kafka_producer/env/bin/activate
echo 'Starting to send messages to Kafka...'
cd kafka_producer && \
python producer.py && \
cd ..
echo 'Finished sending messages to Kafka...'
deactivate
