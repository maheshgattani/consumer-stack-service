package app

import java.util.HashMap

import akka.actor.{ActorSystem, Props}
import akka.routing.RoundRobinPool
import helpers.RabbitMQ
import play.Play
import producers.MessageProducer
import scala.collection.JavaConverters._

object GlobalConfig {
	val config = Play.application.configuration.getConfig("rabbitmq")
	val messageProducerSystem = ActorSystem("messageProducerSystem")
	val numberOfProducers = config.getString("number-of-producers").toInt
	val messageProducerRouter = messageProducerSystem.actorOf(
		Props[MessageProducer].withRouter(RoundRobinPool(numberOfProducers)), name = "messageProducerRouter")

	val channel = RabbitMQ.setupChannel
	val exchange = config.getString("exchange")
	channel.exchangeDeclare(exchange, "topic", true)

	// declare Queues
	val consumerStackConfig =
		Play.application.configuration.getConfig("consumer-stack")
			.asMap.asScala.map { case (key, config) =>
					val configMap = config.asInstanceOf[HashMap[String, AnyRef]].asScala.map { case (k, v) =>
						(k, v.toString)
					}.toMap
					(key, configMap)
			}.toMap

	val consumerStackConfigList =
		consumerStackConfig.toList.sortWith((a,b) => a._2.get("sort").get.toFloat < b._2.get("sort").get.toFloat)

	val initialRoutingKey = consumerStackConfigList(0)._2.get("incoming-routing-key").get

	val updatedStackConfigList =
		consumerStackConfigList.zipWithIndex.map { case ((key, config), index) =>
				val newConfig = if (index < consumerStackConfigList.size - 1) {
					val outgoingRoutingKey = consumerStackConfigList(index + 1)._2.get("incoming-routing-key").get
					config ++ Map("outgoing-routing-key" -> outgoingRoutingKey)
				}
				else
					config
				createAndBindQueue(newConfig.get("queue-name").get, newConfig.get("incoming-routing-key").get)
				createConsumers(key, newConfig)
				(key, newConfig)
		}

	def createAndBindQueue(queue: String, routingKey: String) = {
		channel.queueDeclare(queue, true, false, false, null)
		channel.queueBind(queue, exchange, routingKey)
	}

	def createConsumers(consumerName: String, config: Map[String, String])= {
		val consumerClass: Class[_] = Class.forName("consumers." + config.get("consumer-class").get)
		val consumerSystem = ActorSystem(consumerName + "System")
		val numberOfWorkers = config.get("no-of-workers").get.toInt
		val props = Props.create(consumerClass, config)
		consumerSystem.actorOf(
			props.withRouter(RoundRobinPool(numberOfWorkers)), name = "messageProducerRouter"
		)
	}
}