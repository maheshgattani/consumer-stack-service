package producers

import akka.actor.Actor
import com.rabbitmq.client.{ShutdownSignalException, ConfirmListener}
import helpers.RabbitMQ
import play.Play
import play.api.libs.json._

case class Message(id: Option[Long], data: String, routingKey: Option[String])
case class NackException(message: String) extends Exception

class MessageProducer extends Actor {

	val config = Play.application.configuration.getConfig("rabbitmq")
	val exchange = config.getString("exchange")
	var channel = setup

	def setup = {
		val channel = RabbitMQ.setupChannel
		channel.confirmSelect()
		channel.addConfirmListener(new MessageProducerConfirmListener)
		channel
	}

	def post(message: Message) : Boolean = {
		try {
			val newData = if (message.id.isDefined) {
				JsObject(Seq(
					"id" -> JsString(message.id.get.toString),
					"data" -> JsString(message.data)
				))
			}
			else {
				JsObject(Seq(
					"data" -> JsString(message.data)
				))
			}
			channel.basicPublish(exchange, message.routingKey.get, null, Json.stringify(newData).getBytes("UTF-8"))
			channel.waitForConfirmsOrDie(10)
			true
		} catch {
			case _: ShutdownSignalException | _: NackException => {
				channel = setup
				false
			}
			case e: Exception => {
				false
			}
		}
	}

	def receive = {
		case Message(id, message, routingKey) => {
			sender ! post(Message(id, message, routingKey))
		}
	}
}

class MessageProducerConfirmListener extends ConfirmListener {
	def handleAck(deliveryTag: Long, multiple: Boolean): Unit = {}

	def handleNack(deliveryTag: Long, multiple: Boolean): Unit = {
		throw new NackException("Nack Received")
	}
}