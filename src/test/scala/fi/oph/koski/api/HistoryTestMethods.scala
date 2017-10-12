package fi.oph.koski.api

import fi.oph.koski.history.OpiskeluoikeusHistory
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.schema.{Henkilö, KoskiSchema}
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.jackson.JsonMethods

trait HistoryTestMethods extends OpiskeluoikeusTestMethods {
  import KoskiSchema.deserializationContext

  def readHistory = SchemaValidatingExtractor.extract[List[OpiskeluoikeusHistory]](JsonMethods.parse(body)).right.get

  def getHistory(opiskeluoikeusOid: String, user: UserWithPassword = defaultUser): List[OpiskeluoikeusHistory] = {
    authGet("api/opiskeluoikeus/historia/" + opiskeluoikeusOid, user = user) {
      verifyResponseStatus(200)
      readHistory
    }
  }

  def verifyHistory(opiskeluoikeusOid: String, versions: List[Int]): Unit = {
    val historia: List[OpiskeluoikeusHistory] = getHistory(opiskeluoikeusOid)
    historia.map(_.versionumero) should equal(versions)

    authGet("api/opiskeluoikeus/validate/" + opiskeluoikeusOid) {
      // Validates version history integrity by applying all history patches on top of first version and comparing to stored final value.
      verifyResponseStatus(200)
    }
  }
}