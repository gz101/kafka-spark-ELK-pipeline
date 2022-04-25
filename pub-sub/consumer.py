import json 
from kafka import KafkaConsumer


if __name__ == '__main__':
    consumer = KafkaConsumer(
        'waterStandpipeOut',
        bootstrap_servers=['localhost:9094'],
        auto_offset_reset='earliest',
        consumer_timeout_ms=12000,
        group_id=None
    )

    for message in consumer:
        print('Hello from consumer!')
        print(json.loads(message.value))

    print('Goodbye from consumer')
    consumer.close()
