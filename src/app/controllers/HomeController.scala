package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import play.api.libs.ws
import play.api.libs.ws._
import play.api.libs.json._
import play.api.Play._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(configuration: Configuration, ws: WSClient) extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action.async(parse.empty) { request =>

    getAccessToken.flatMap {
      case Some(accessToken) => {
        doSearch(accessToken).map { response =>
          Logger.info("search response: " + response)
          Ok(Json.obj())
        }
      }
      case None => {
        Future.successful(BadRequest(Json.obj()))
      }
    }
  }

  private def getAccessToken: Future[Option[String]] = {
    val accessTokenUrl = configuration.getString("yelp.accessTokenUrl").get
    val clientId = configuration.getString("yelp.clientId").get
    val clientSecret = configuration.getString("yelp.secret").get

    val data = Map(
      "grant_type" -> Seq("client_credentials"),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret)
    )

    val request: WSRequest = ws.url(accessTokenUrl)
        .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")

    val responseFut: Future[WSResponse] = request.post(data)

    responseFut.map { response =>
      (response.json \ "access_token").asOpt[String]
    }
  }

  private def doSearch(accessToken: String): Future[JsValue] = {
    val searchUrl = configuration.getString("yelp.searchUrl").get

    val request: WSRequest = ws.url(searchUrl)
        .withHeaders("Authorization" -> ("Bearer " + accessToken))
        .withQueryString("location" -> "San Francisco", "sort_by" -> "review_count")

    request.get().map { response =>
      response.json
    }
  }
}
