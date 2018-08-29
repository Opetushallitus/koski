package fi.oph.koski.api

import fi.oph.koski.history.OpiskeluoikeusHistory
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.json4s.jackson.JsonMethods

trait HistoryTestMethods extends OpiskeluoikeusTestMethods {
  import fi.oph.koski.schema.KoskiSchema.deserializationContext

  def readHistory = SchemaValidatingExtractor.extract[List[OpiskeluoikeusHistory]](JsonMethods.parse(body)).right.get

  def getHistory(opiskeluoikeusOid: String, user: UserWithPassword = defaultUser): List[OpiskeluoikeusHistory] = {
    authGet("api/opiskeluoikeus/historia/" + opiskeluoikeusOid, user = user) {
      verifyResponseStatusOk()
      readHistory
    }
  }

  def verifyHistory(opiskeluoikeusOid: String, versions: List[Int]): Unit = {
    val historia: List[OpiskeluoikeusHistory] = getHistory(opiskeluoikeusOid)
    historia.map(_.versionumero) should equal(versions)

    authGet("api/opiskeluoikeus/validate/" + opiskeluoikeusOid, user = MockUsers.paakayttaja) {
      // Validates version history integrity by applying all history patches on top of first version and comparing to stored final value.
      verifyResponseStatusOk()
    }
  }
}
