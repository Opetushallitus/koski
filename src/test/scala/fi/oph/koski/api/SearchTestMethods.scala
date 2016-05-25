package fi.oph.koski.api

import fi.oph.koski.json.Json
import fi.oph.koski.schema.TaydellisetHenkilötiedot

trait SearchTestMethods extends LocalJettyHttpSpecification {
  def search[T](query: String)(f: => T) = {
    get("api/oppija/search", params = List(("query" -> query)), headers = authHeaders()) {
      f
    }
  }

  def searchForNames(query: String): List[String] = {
    searchForHenkilötiedot(query).map(_.kokonimi)
  }

  def searchForHenkilötiedot(query: String): List[TaydellisetHenkilötiedot] = {
    search(query) {
      verifyResponseStatus(200)
      Json.read[List[TaydellisetHenkilötiedot]](body)
    }
  }
}
