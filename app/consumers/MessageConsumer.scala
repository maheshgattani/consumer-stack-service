package consumers

import akka.actor.Actor
import akka.util.Timeout
import app.GlobalConfig
import com.thenewmotion.akka.rabbitmq._
import helpers.RabbitMQ
import play.Play
import producers.Message
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask
import play.api.libs.json._

trait MessageConsumer extends Actor {

	val queueConfig: Map[String, String]
	implicit val timeout = Timeout(1 minutes)

	val queue = queueConfig.get("queue-name").get
	val channel = RabbitMQ.setupChannel
	val rabbitMQConfig = Play.application.configuration.getConfig("rabbitmq")
	val exchange = rabbitMQConfig.getString("exchange")
	setupSubscriber

	def setupSubscriber {
		val consumer = new DefaultConsumer(channel) {
			override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]) {
				val json = Json.parse(body)
				val messageId = (json \ "id").asOpt[String].map(_.toLong)
				val messageData = (json \ "data").asOpt[String].get
				val newMessage = processMessage(messageId, messageData)
				val ack = if (newMessage.isDefined) {
					val outgoingRoutingKey = queueConfig.get("outgoing-routing-key")
					if (outgoingRoutingKey.isDefined) {
						val future = GlobalConfig.messageProducerRouter ? Message(messageId, newMessage.get.toString, outgoingRoutingKey)
						try {
							Await.result(future.mapTo[Boolean], 1 minute)
							true
						} catch {
							case e: Exception => false
						}
					}
					else
						true
				}
				else
					true

				if (ack)
					channel.basicAck(envelope.getDeliveryTag, false)
				else
					channel.basicNack(envelope.getDeliveryTag, false, true)
			}
		}
		channel.basicConsume(queue, false, consumer)
	}

	// We will never be sending messages the usual way
	def receive = {
		case a : Any =>
	}

	def processMessage(id: Option[Long], body: String): Option[String]
}