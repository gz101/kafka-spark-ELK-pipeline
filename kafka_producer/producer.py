import json
import time
import random
import requests
from kafka import KafkaProducer 


urls = [
    "https://geotech-api.herokuapp.com/params/water_standpipe",
    "https://geotech-api.herokuapp.com/params/pore_pressure",
    "https://geotech-api.herokuapp.com/params/settlement/x",
    "https://geotech-api.herokuapp.com/params/settlement/y",
    "https://geotech-api.herokuapp.com/params/settlement/z"
]

iterations = 10


def serializer(message):
    return json.dumps(message).encode('utf-8')


producer = KafkaProducer(
    bootstrap_servers=['localhost:9094'],
    value_serializer=serializer
)


if __name__ == '__main__':
    count = 0
    while count < iterations:
        try:
            for url in urls:
                resp = requests.get(url=url).json()
                producer.send('monitoring', resp)
            
            print(f'Producer sending input #{count} to Kafka.')
            count = count + 1
            time_to_sleep = random.randint(3, 8)
            time.sleep(time_to_sleep)
        except EOFError:
            print('No more input from producer.')
            break
