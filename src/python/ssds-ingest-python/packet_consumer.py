import sys
import pika

# Import all adapters for easier experimentation
from pika.adapters import *
from pika.credentials import PlainCredentials


connection = None
channel = None


def on_connected(connection):
    global channel
    print("demo_receive: Connected to RabbitMQ")
    connection.channel(on_channel_open)


def on_channel_open(channel_):
    global channel
    channel = channel_
    print("demo_receive: Received our Channel")
    channel.queue_declare(queue="test", durable=True,
                          exclusive=False, auto_delete=False,
                          callback=on_queue_declared)


def on_queue_declared(frame):
    print("demo_receive: Queue Declared")
    channel.basic_consume(handle_delivery, queue='5a39851e-b343-11d9-8c1e-00306e389969')


def handle_delivery(channel, method_frame, header_frame, body):
    print("Basic.Deliver %s delivery-tag %i: %s",
                  header_frame.content_type,
                  method_frame.delivery_tag,
                  body)
    channel.basic_ack(delivery_tag=method_frame.delivery_tag)

if __name__ == '__main__':
    parameters = pika.ConnectionParameters(host="messaging.shore.mbari.org",
                                           credentials=PlainCredentials("ssdsadmin", "water4u"), port=5672,
                                           virtual_host="ssds")
    connection = SelectConnection(parameters, on_connected)
    try:
        connection.ioloop.start()
    except KeyboardInterrupt:
        connection.close()
        connection.ioloop.start()
