package controllers

import javax.inject._

import play.api._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import services.AccountsService
import utils.BsunError

case class AccountCreationResponse(
  email: String,
  created: Long,
  token: String
)

@Singleton
class AccountsController @Inject()(configuration: Configuration, accountsService: AccountsService) extends Controller {

  implicit val accountCreationResponse = Json.format[AccountCreationResponse]

  def createAccount(): Action[JsValue] =
    Action.async(parse.json) { request =>

      accountsService.createAccount(request.body).map {
        case Right(account) => {
          // redact out the password hash

          Ok(Json.toJson(AccountCreationResponse(
            account.email,
            account.created,
            account.token)))
        }
        case Left(e) => BsunError.toResponse(e)
      }
  }

  def loginAccount(): Action[JsValue] =
    Action.async(parse.json) { request =>
      accountsService.loginAccount(request.body).map {
        case Right(account) => {
          // redact out the password hash

          Ok(Json.toJson(AccountCreationResponse(
            account.email,
            account.created,
            account.token)))
        }
        case Left(e) => BsunError.toResponse(e)
      }
    }
}
