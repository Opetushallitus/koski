package fi.oph.koski.api.misc

import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.jackson.JsonMethods

trait HistoryTestMethods extends OpiskeluoikeusTestMethods {
  private implicit val context: ExtractionContext = strictDeserialization

  def readHistory = SchemaValidatingExtractor.extract[List[OpiskeluoikeusHistoryPatch]](JsonMethods.parse(body)).right.get

  def getHistory(opiskeluoikeusOid: String, user: UserWithPassword = defaultUser): List[OpiskeluoikeusHistoryPatch] = {
    authGet("api/opiskeluoikeus/historia/" + opiskeluoikeusOid, user = user) {
      verifyResponseStatusOk()
      readHistory
    }
  }

  def verifyHistory(opiskeluoikeusOid: String, versions: List[Int]): Unit = {
    val historia: List[OpiskeluoikeusHistoryPatch] = getHistory(opiskeluoikeusOid)
    historia.map(_.versionumero) should equal(versions)

    authGet("api/opiskeluoikeus/validate/" + opiskeluoikeusOid, user = MockUsers.paakayttaja) {
      // Validates version history integrity by applying all history patches on top of first version and comparing to stored final value.
      verifyResponseStatusOk()
    }
  }
}
