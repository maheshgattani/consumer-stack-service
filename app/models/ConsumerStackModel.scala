package models

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.conversions.scala._
import helpers.MongoDB

object ConsumerStackModel {

	DeregisterJodaTimeConversionHelpers()

	def saveObject(id: Option[Long], data: String): Unit = {
		val builder = MongoDBObject.newBuilder
		if (id.isDefined) {
			builder += "message_id" -> id.get
		}
		builder += "data" -> data

		if (id.isDefined) {
			val query = MongoDBObject("message_id" -> id.get)
			MongoDB.consumerStackCollection.update(query, builder.result, upsert=true)
		}
		else {
			MongoDB.consumerStackCollection.insert(builder.result)
		}
	}
}