package helpers

import com.rabbitmq.client.ConnectionFactory
import com.thenewmotion.akka.rabbitmq._
import play.Play

object RabbitMQ {

	def setupChannel = {
		val config = Play.application.configuration.getConfig("rabbitmq")
		val factory = new ConnectionFactory()
		factory.setHost(config.getString("host"))
		factory.setPort(config.getString("port").toInt)
		factory.setUsername(config.getString("user"))
		factory.setPassword(config.getString("password"))
		val connection: Connection = factory.newConnection()
		connection.createChannel()
	}
}
