import json
import time
import json
import random
from kafka import KafkaProducer


def serializer(message):
    return json.dumps(message).encode('utf-8')


producer = KafkaProducer(
    bootstrap_servers=['localhost:9094'],
    value_serializer=serializer
)


if __name__ == '__main__':
    count = 0
    while True:
        try:
            sample_point = input()
            # assert(sample_point == '{"ping": "pong!"}')
            producer.send('waterStandpipeIn', sample_point)
            print(f'Producer sending input #{count} to Kafka.')
            count = count + 1
            time_to_sleep = random.randint(5, 10)
            time.sleep(time_to_sleep)
        except EOFError:
            print('No more input from producer.')
            break
