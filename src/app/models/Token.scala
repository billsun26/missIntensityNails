package models

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Reads.JsObjectReducer
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.BsunError
import utils.BsunError._
import play.api.Configuration

import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future
import scala.util.Random

case class TokenResource(
  email: String,
  token: String,
  created: Long,
  expires: Long
)

class Token @Inject()(configuration: Configuration) extends Model(configuration) {

  implicit val tokenResource = Json.format[TokenResource]
  val mongoDriver = new reactivemongo.api.MongoDriver
  lazy val connection = mongoDriver.connection(List(mongoUrl))
  def collection = connection.database("")
  val expiryPeriod = configuration.getLong("accounts.tokenExpiry").get

  val accountLoginTransform: Reads[JsObject] = (
    (__ \ "email").json.pickBranch(Reads.of[JsString]) and
    (__ \ "password").json.pickBranch(Reads.of[JsString]) and
    __.json.put(Json.obj("token" -> JsString(Random.alphanumeric.take(8).mkString))) and
    __.json.put(Json.obj("created" -> JsNumber(System.currentTimeMillis()))) and
    __.json.put(Json.obj("expiry" -> JsNumber(System.currentTimeMillis() + expiryPeriod)))
  ).reduce

  def loginAccount(
    email: String,
    password: String
  ): Future[Either[BsunError, TokenResource]] = {

  	
    requestBody.transform(accountLoginTransform) match {
      case s: JsSuccess[JsObject] => {
        val transformedObj = s.get
        // TODO: make db call here
        Future.successful(Right(passwordHashedAccount.as[TokenResource]))

      }
      case e: JsError => {
        Future.successful(Left(BsunError(VALIDATION_ERROR, "Token creation validation error: " + e)))
      }
    }
  }
}
