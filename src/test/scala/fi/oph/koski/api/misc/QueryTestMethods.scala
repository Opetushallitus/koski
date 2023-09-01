package fi.oph.koski.api.misc

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.Oppija
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}

trait QueryTestMethods extends HttpSpecification {
  def queryOppijat(queryString: String = "", user: UserWithPassword = defaultUser): List[Oppija] = {
    authGet ("api/oppija" + queryString, user = user) {
      verifyResponseStatusOk()
      implicit val context: ExtractionContext = strictDeserialization
      SchemaValidatingExtractor.extract[List[Oppija]](body).right.get
    }
  }
}
