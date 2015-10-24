# consumer-stack-service

This service allows a stackable queue consumer framework.
Requirements: Scala, Play, RabbitMQ, Mongo

The components are
1. REST API (To allow messages to be written to the system)
2. A configurable number of queues (To act as part of consumer stack)
3. An actor system for message producers (To ensure high performance in a scalable and predicatble manner)
4. Actor systems for message consumers (To ensure high performance in a scalable and predicatble manner)
5. Mongo as the backend datastore (To save the messages and processed data)

Config driven queue system
1. The primary exchange name
2. Number of actors in the actor system for the producers
3. Queue consumers. You can add as many queue consumers as you want. For each consumer, configurable parameters are queue name, incoming routing key for the queue, sorting order (order in which to put this consumer in the consumer stack), consumer class name, number of actors in the actor system for this consumer.
4. RabbitMQ settings
5. Mongo settings

A user can call the REST API to put a message in the system. The system will put the message on the first queue defined in the config. The consumer attached to that queue will consume the message, process it, save it to the database and push a new message back to the exchange with a new routing key. This new routing key is automatically configured so that the next queue in the stack will get the message. Then the consumer attached to that queue will consumer the message and the cycle will carry on till the last consumer.

The logic for the first and last consumer is already provided in the framework. They save and update the message in Mongo.

![Alt text](/project/design.jpeg?raw=true "Optional Title")
