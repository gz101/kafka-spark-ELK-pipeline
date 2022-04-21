# import json 
from kafka import KafkaConsumer
import logging


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)

    consumer = KafkaConsumer(
        'responses',
        bootstrap_servers=['172.21.0.0:9092'],
        auto_offset_reset='earliest',
        consumer_timeout_ms=8000,
        group_id=None
    )

    for message in consumer:
        # print(f'Hello from consumer: {json.loads(message.value)}')
        print(f'{message.value}')

    print('Goodbye from consumer')
    consumer.close()
