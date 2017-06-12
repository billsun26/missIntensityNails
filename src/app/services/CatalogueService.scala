package services

import javax.inject.{Inject, Singleton}

import play.api.libs.json._
import play.api.Configuration

import scala.concurrent.Future
import utils.BsunError
import utils.BsunError._
import models._
import utils.Constants

@Singleton
class CatalogueService @Inject()()(configuration: Configuration) {

	def createItem(): Future[Either[BsunError, CatalogueItem]] = {

	}
}