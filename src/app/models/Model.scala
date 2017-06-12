package models

import javax.inject.Inject

import play.api.Configuration

class Model @Inject()(configuration: Configuration) {

  val mongoDriver = new reactivemongo.api.MongoDriver
  val mongoUrl = configuration.getString("mongodb.uri").get
  lazy val connection = mongoDriver.connection(List(mongoUrl))
//  def collection = connection.database("")
}
