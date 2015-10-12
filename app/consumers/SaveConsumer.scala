package consumers

import models.ConsumerStackModel

class SaveConsumer(val queueConfig: Map[String, String]) extends MessageConsumer {

	def processMessage(id: Option[Long], body: String): Option[String] = {
		ConsumerStackModel.saveObject(id, body)
		Some(body)
	}
}
