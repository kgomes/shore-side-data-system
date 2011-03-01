import sys
import pika
import time
from pika.credentials import PlainCredentials

#pika.log.setup(color=True)

connection = None
channel = None

# Import all adapters for easier experimentation
from pika.adapters import *


def on_connected(connection):
    pika.log.info("demo_send: Connected to RabbitMQ")
    connection.channel(on_channel_open)


def on_channel_open(channel_):
    global channel
    channel = channel_
    pika.log.info("demo_send: Received our Channel")
    channel.queue_declare(queue="5a39851e-b343-11d9-8c1e-00306e389969", durable=True,
                          exclusive=False, auto_delete=False,
                          callback=on_queue_declared)


def on_queue_declared(frame):
    pika.log.info("demo_send: Queue Declared")
    for x in xrange(0, 10):
        message = "Hello World #%i: %.8f" % (x, time.time())
        pika.log.info("Sending: %s" % message)
        channel.basic_publish(exchange='',
                              routing_key="5a39851e-b343-11d9-8c1e-00306e389969",
                              body=message,
                              properties=pika.BasicProperties(
                                      content_type="text/plain",
                                      delivery_mode=1))

    # Close our connection
    connection.close()

if __name__ == '__main__':
    parameters = pika.ConnectionParameters(host="messaging.shore.mbari.org", port=5672,
                                           credentials=PlainCredentials("ssdsadmin", "water4u"), virtual_host="ssds")
    connection = SelectConnection(parameters, on_connected)
    try:
        connection.ioloop.start()
    except KeyboardInterrupt:
        connection.close()
        connection.ioloop.start()