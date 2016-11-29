package fi.oph.koski.api

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema.TäydellisetHenkilötiedot

trait SearchTestMethods extends HttpSpecification {
  def search[T](query: String, user: UserWithPassword)(f: => T) = {
    get("api/henkilo/search", params = List(("query" -> query)), headers = authHeaders(user)) {
      f
    }
  }

  def searchForNames(query: String, user: UserWithPassword = defaultUser): List[String] = {
    searchForHenkilötiedot(query, user).map(_.kokonimi)
  }

  def searchForHenkilötiedot(query: String, user: UserWithPassword = defaultUser): List[TäydellisetHenkilötiedot] = {
    search(query, user) {
      verifyResponseStatus(200)
      Json.read[List[TäydellisetHenkilötiedot]](body)
    }
  }
}
