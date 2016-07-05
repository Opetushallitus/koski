package fi.oph.koski.api

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema.Oppija

trait QueryTestMethods extends HttpSpecification {
  def queryOppijat(queryString: String = "", user: UserWithPassword = defaultUser): List[Oppija] = {
    authGet ("api/oppija" + queryString, user = user) {
      verifyResponseStatus(200)
      val oppijat: List[Oppija] = Json.read[List[Oppija]](response.body)
      oppijat
    }
  }
}
