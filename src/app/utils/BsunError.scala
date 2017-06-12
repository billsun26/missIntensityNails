package utils

import play.api.mvc.Results.Status
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.http.Status._

case class BsunError(
  internalErrorCode: Int,
  message: String
)

object BsunError {

  val EMPTY_EMAIL_ADDRESS = 1001
  val INVALID_EMAIL_ADDRESS = 1002
  val PASSWORD_CRITERIA_NOT_MET = 1003
  val VALIDATION_ERROR = 1004
  val DB_ERROR = 1005

  implicit val bsunErrorFormat = Json.format[BsunError]

  def toResponse(errorObj: BsunError): Result = {
    new Status(mapToHttpCode(errorObj.internalErrorCode))(Json.toJson(errorObj))
  }

  private def mapToHttpCode(internalCode: Int): Int = {
    internalCode match {
      case EMPTY_EMAIL_ADDRESS => BAD_REQUEST
      case INVALID_EMAIL_ADDRESS => BAD_REQUEST
      case PASSWORD_CRITERIA_NOT_MET => BAD_REQUEST
      case VALIDATION_ERROR => BAD_REQUEST
      case DB_ERROR => INTERNAL_SERVER_ERROR
      case _ => INTERNAL_SERVER_ERROR
    }
  }
}
