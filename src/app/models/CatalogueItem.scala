package models

import utils.Constants

trait CatalogueItem {
	id: String,
	brand: String,
	model: String,
	description: String,
	comments: String,
	created: Long,
	updated: Long
}

object CatalogueItem {

	def getAllForUser(accountId: String) = {

	}
}