import json 
from kafka import KafkaConsumer


if __name__ == '__main__':
    consumer = KafkaConsumer(
        'messages',
        bootstrap_servers='localhost:9092',
        auto_offset_reset='earliest',
        consumer_timeout_ms=8000
    )

    for message in consumer:
        print(f'Hello from consumer: {json.loads(message.value)}')

    print('Goodbye from consumer')
    consumer.close()
