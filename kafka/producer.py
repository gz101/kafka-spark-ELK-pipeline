import json
import time
import json
import random
from kafka import KafkaProducer


def serializer(message):
    return json.dumps(message).encode('utf-8')


producer = KafkaProducer(
    bootstrap_servers=['kafka:9094'],
    # value_serializer=serializer
)


if __name__ == '__main__':
    # count = 0
    # while True:
        # try:
        #     sample_point = input()
        #     producer.send('messages', sample_point)
        #     count = count + 1
        #     print(f'Producer sending message #{count} to Kafka.')
        #     time_to_sleep = random.randint(3, 7)
        #     time.sleep(time_to_sleep)
        # except EOFError:
        #     print('No more input from producer.')
        #     break

    for i in range(10):
        producer.send('messages', b'sending message to Kafka')
        print(f'sending message #{i} to Kafka')
        time_to_sleep = random.randint(3, 7)
        time.sleep(time_to_sleep)