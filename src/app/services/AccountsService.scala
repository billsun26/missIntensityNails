package services

import javax.inject.{Inject, Singleton}

import play.api.libs.json._
import play.api.Configuration

import scala.concurrent.Future
import utils.BsunError
import utils.BsunError._
import models._

@Singleton
class AccountsService @Inject()(account: Account, token: Token)(configuration: Configuration) {

  val minPasswordLength = configuration.getInt("accounts.minPasswordLength").get

  def createAccount(requestBody: JsValue): Future[Either[BsunError, TokenResource]] = {

    val emailInput = (requestBody \ "email").asOpt[String]
    val passwordInput = (requestBody \ "password").asOpt[String]

    if (emailInput.isDefined) {
      validateEmailAddress(emailInput.get) match {
        case None => {
          if (passwordInput.isDefined && passwordInput.get.length >= minPasswordLength) {
            account.createAccount(requestBody).flatMap {
              case Right(creationRes) => {
                token.loginAccount(emailInput.get, passwordInput.get)
              }
              case Left(e) => {
                Future.successful(Left(BsunError(DB_ERROR, "Error creation account: " + e)))
              }
            }
          } else {
            Future.successful(Left(BsunError(PASSWORD_CRITERIA_NOT_MET,
              "Password must be minimum length " + minPasswordLength)))
          }
        }
        case Some(error) => Future.successful(Left(error))
      }
    } else {
      Future.successful(Left(BsunError(EMPTY_EMAIL_ADDRESS, "Empty email input")))
    }
  }

  def loginAccount(requestBody: JsValue): Future[Either[BsunError, TokenResource]] = {
    val emailInput = (requestBody \ "email").asOpt[String]
    val passwordInput = (requestBody \ "password").asOpt[String]

    if (emailInput.isDefined) {
      validateEmailAddress(emailInput.get) match {
        case None => {
          if (passwordInput.isDefined && passwordInput.get.length >= minPasswordLength) {
            token.loginAccount(emailInput.get, passwordInput.get)
          } else {
            Future.successful(Left(BsunError(PASSWORD_CRITERIA_NOT_MET,
              "Password must be minimum length " + minPasswordLength)))
          }
        }
        case Some(error) => Future.successful(Left(error))
      }
    } else {
      Future.successful(Left(BsunError(EMPTY_EMAIL_ADDRESS, "Empty email input")))
    }
  }

  private def validateEmailAddress(inputEmail: String): Option[BsunError] = {
    val regex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9-])?(?:\.[a-zA-Z0-9-](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9-])?)*$""".r

    if (inputEmail.trim.isEmpty) {
      Some(BsunError(EMPTY_EMAIL_ADDRESS, "Empty email address"))
    } else {
      regex.findFirstMatchIn(inputEmail) match {
        case Some(matchedEmail) => None
        case None => Some(BsunError(INVALID_EMAIL_ADDRESS, "Invalid email address"))
      }
    }
  }
}
