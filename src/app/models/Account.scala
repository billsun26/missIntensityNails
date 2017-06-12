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

case class AccountResource(
  id: String,
  email: String,
  passwordHash: String,
  ownershipToken: String,
  created: Long
)

class Account @Inject()(configuration: Configuration) extends Model(configuration) {

  implicit val accountResource = Json.format[AccountResource]
  val mongoDriver = new reactivemongo.api.MongoDriver
  lazy val connection = mongoDriver.connection(List(mongoUrl))
  def collection = connection.database("")

  val accountCreationTransform: Reads[JsObject] = (
    (__ \ "email").json.pickBranch(Reads.of[JsString]) and
    (__ \ "password").json.pickBranch(Reads.of[JsString]) and
    __.json.put(Json.obj("created" -> JsNumber(System.currentTimeMillis()))) and
    __.json.put(Json.obj("ownershipToken" -> JsString(Random.alphanumeric.take(8).mkString)))
  ).reduce

  def createAccount(
    requestBody: JsValue
  ): Future[Either[BsunError, AccountResource]] = {

    requestBody.transform(accountCreationTransform) match {
      case s: JsSuccess[JsObject] => {
        val transformedObj = s.get
        val passwordHashedAccount = hashAccountPassword(transformedObj)
        // TODO: make db call here
        // add _id onto response
        passwordHashedAccount ++ (Json.obj("id" -> "someRandomId"))
        Future.successful(Right(passwordHashedAccount.as[AccountResource]))
      }
      case e: JsError => {
        Future.successful(Left(BsunError(VALIDATION_ERROR, "Account creation validation error: " + e)))
      }
    }
  }

  def findAccountByEmail(email: String): Future[Option[AccountResource]] = {
    val query = Json.obj(
      "email" -> email
    )

    collection.find(query).map {
      case Some(findRes) => {
        val foundAccount = findRes.as[AccountResource]
      }
      case None => None
    }
  }

  private def hashAccountPassword(input: JsObject): JsObject = {
    val rawPassword = (input \ "password").as[String]
    val passwordHashAndSalt = rawPassword + "testedHash"

    input.-("password").+("passwordHash", JsString(passwordHashAndSalt))
  }
}
