# consumer-stack-service

This service allows a stackable queue consumer framework.

The service accepts a message via a REST API. Once the message is in the system, it's processed via a set of queue consumers, run in order as specified in the configuration.
An adapter is considered as a combination of a queue and a queue consumer and you can add as many adapters as you want and run then in any order. All this is run via the configuration.

The initial framework already has 2 adapters. First one saves the message in a MondoDB collection and the last one updates the same collection.

The REST API posts the message to the exchange which is bind to a queue based on the config. This API is backed by a number of Akka Actors to post to the queue. The number of actors is configurable.

Every Consumer will be backed by a number of Akka Actors as well. Number of actors is configurable per consumer.

Better documentation to follow.
