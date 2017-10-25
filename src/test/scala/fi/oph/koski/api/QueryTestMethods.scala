package fi.oph.koski.api

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema.Oppija
import fi.oph.scalaschema.SchemaValidatingExtractor

trait QueryTestMethods extends HttpSpecification {
  import fi.oph.koski.schema.KoskiSchema.deserializationContext
  def queryOppijat(queryString: String = "", user: UserWithPassword = defaultUser): List[Oppija] = {
    authGet ("api/oppija" + queryString, user = user) {
      verifyResponseStatusOk()
      SchemaValidatingExtractor.extract[List[Oppija]](body).right.get
    }
  }
}
