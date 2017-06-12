package controllers

import javax.inject._

import play.api._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import services.AccountsService
import utils.BsunError

@Singleton
class CatalogueController @Inject()(configuration: Configuration, catalogueService: CatalogueService) extends Controller {

  def createItem(): Action[JsValue] =
	Action.async(parse.json) { request =>

      catalogueService.createItem(request.body).map {
        case Right(createdItem) => {

          Ok()
        }
        case Left(e) => BsunError.toResponse(e)
      }
  }
}
