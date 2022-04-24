#!/bin/bash

function hit_api_ping() {
    counter=1
    while [[ $counter -le 10 ]]
    do
        response=$(curl -s -H "Accept: application/json" -H "Content-Type: application/json" -X GET https://geotech-api.herokuapp.com/ping/)
        echo "$response" >pipe
        ((counter++))
    done
}

# initialize and check requirements
echo "Starting by checking program requirements."
if [ ! "$(docker ps -q -f name=kafka)" ]; then
    echo "Error: Kafka container not started."
    exit 1
fi
# if [ ! "$(jq --version)" ]; then 
#     echo "Error: jq package not installed."
#     exit 2
# fi
if [ ! -d "env" ]; then
    echo "Error: python environment directory not found."
    exit 3
fi
if [ ! -f "producer.py" ]; then
    echo "Error: producer file not found."
    exit 4
fi
if [ ! -f "consumer.py" ]; then
    echo "Error: consumer file not found."
    exit 5
fi
echo "All requirements found, proceeding to ingest API into Kafka."

# driver code
source ./env/bin/activate && \
mkfifo pipe 
python3 ./producer.py <pipe &
hit_api_ping &
python3 ./consumer.py

# cleanup
rm pipe && \
deactivate
if [ -f "$pipe" ]; then
    echo "Error: failed cleanup."
    exit 6
fi
echo "Finished and cleaned up successfully."
