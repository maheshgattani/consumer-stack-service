package controllers

import akka.util.Timeout
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import producers.Message
import app.GlobalConfig
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask

case class IncomingMessage(id: Long, data: String)
class MessageProducerController extends Controller {

	implicit val timeout = Timeout(1 minutes)
	val postForm = Form(
		mapping(
			"id" -> longNumber,
			"data" -> nonEmptyText
		)(IncomingMessage.apply)(IncomingMessage.unapply)
	)

	def post = Action { implicit request =>
		val message = postForm.bindFromRequest().get
		val routingKey = GlobalConfig.initialRoutingKey
		val future = GlobalConfig.messageProducerRouter ? Message(Some(message.id), message.data, Some(routingKey))
		val didPublish: Boolean = Await.result(future.mapTo[Boolean], 1 minute)
		if (didPublish)
			Ok
		else
			ServiceUnavailable
	}
}
