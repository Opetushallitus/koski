package fi.oph.koski.api

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema.Oppija
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.JValue

trait QueryTestMethods extends HttpSpecification {
  def queryOppijat(queryString: String = "", user: UserWithPassword = defaultUser): List[Oppija] = {
    authGet ("api/oppija" + queryString, user = user) {
      verifyResponseStatus(200)
      val oppijat: List[Oppija] = Json.read[List[JValue]](response.body).map { json =>
        import fi.oph.koski.schema.KoskiSchema.deserializationContext
        SchemaValidatingExtractor.extract[Oppija](json).right.get
      }
      oppijat
    }
  }
}
