import json
import time
import random
import requests
from kafka import KafkaProducer 


url = "https://geotech-api.herokuapp.com/params/water_standpipe"
iterations = 4


def serializer(message):
    return json.dumps(message).encode('utf-8')


producer = KafkaProducer(
    bootstrap_servers=['localhost:9094'],
    value_serializer=serializer
)


if __name__ == '__main__':
    resp = requests.get(url=url).json()
    count = 0
    
    while count < iterations:
        try:
            producer.send('waterStandpipeIn', resp)
            print(f'Producer sending input #{count} to Kafka.')
            count = count + 1
            time_to_sleep = random.randint(10, 20)
            time.sleep(time_to_sleep)
        except EOFError:
            print('No more input from producer.')
            break
