package helpers

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import play.Play

object MongoDB {

	val config = Play.application.configuration.getConfig("mongodb")
	val host = config.getString("host")
	val port = config.getInt("port")
	val database = config.getString("database")
	val consumerStackCollectionName = config.getString("consumer-stack-collection")

	val mongoClient = MongoClient(host, port)
	val consumerStackDatabase = mongoClient(database)
	val consumerStackCollection = consumerStackDatabase(consumerStackCollectionName)

	val builder = MongoDBObject.newBuilder
	builder += "message_id" -> 1
	consumerStackCollection.createIndex(builder.result)
}
