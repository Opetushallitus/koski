package fi.oph.koski.history

import java.sql.Timestamp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonpatch.JsonPatch
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiTables.OpiskeluoikeusTable.readAsOpiskeluoikeus
import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Rooli}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus
import fi.oph.koski.schema.annotation.SensitiveData
import fi.oph.koski.util.OptionalLists.optionalList
import org.json4s._
import org.json4s.jackson.JsonMethods
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write

case class OpiskeluoikeusHistoryRepository(db: DB) extends DatabaseExecutionContext with QueryMethods with Logging with JsonMethods {
  def findByOpiskeluoikeusOid(oid: String, maxVersion: Int = Int.MaxValue)(implicit user: KoskiSpecificSession): Option[List[OpiskeluoikeusHistoryPatch]] = {
    runDbSync(findByOpiskeluoikeusOidAction(oid, maxVersion).map(_.map(_.patches)))
  }

  def findByOpiskeluoikeusOidAction(oid: String, maxVersion: Int)(implicit user: KoskiSpecificSession): DBIOAction[Option[OpiskeluoikeusHistory], NoStream, Effect.Read] = {
    OpiskeluOikeudetWithAccessCheck.filter(_.oid === oid)
      .join(OpiskeluoikeusHistoria.filter(_.versionumero <= maxVersion))
      .on(_.id === _.opiskeluoikeusId)
      .sortBy(_._2.versionumero.asc)
      .result
      .map { result =>
        val diffs = result.map(toOpiskeluoikeusHistory).toList
        optionalList(diffs).map(patches => OpiskeluoikeusHistory(oid, maxVersion, patches))
      }
  }

  def findVersion(oid: String, version: Int)(implicit user: KoskiSpecificSession): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    runDbSync(findVersionAction(oid, version))
  }

  def createAction(opiskeluoikeusId: Int, versionumero: Int, kayttäjäOid: String, muutos: JValue): DBIOAction[Int, NoStream, Write] = {
    OpiskeluoikeusHistoria.map { row =>
      (row.opiskeluoikeusId, row.kayttajaOid, row.muutos, row.versionumero)
    } +=(opiskeluoikeusId, kayttäjäOid, muutos, versionumero)
  }

  private def findVersionAction(oid: String, version: Int)(implicit user: KoskiSpecificSession): DBIOAction[Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus], NoStream, Nothing] = {
    findByOpiskeluoikeusOidAction(oid, version).map(_
      .toRight(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + oid + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
      .flatMap(_.toOpiskeluoikeus)
    )
  }

  private def toOpiskeluoikeusHistory(row: (OpiskeluoikeusRow, OpiskeluoikeusHistoryRow)) = OpiskeluoikeusHistoryPatch(
    opiskeluoikeusOid = row._1.oid,
    versionumero = row._2.versionumero,
    aikaleima = row._2.aikaleima,
    kayttajaOid = row._2.kayttajaOid,
    muutos = row._2.muutos
  )
}

// TODO: use LocalDateTime instead of Timestamp for consistency with KoskeenTallennettavaOpiskeluoikeus
case class OpiskeluoikeusHistoryPatch(opiskeluoikeusOid: String, versionumero: Int, aikaleima: Timestamp, kayttajaOid: String, @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)) muutos: JValue)

case class OpiskeluoikeusHistory(oid: String, version: Int, patches: List[OpiskeluoikeusHistoryPatch]) extends Logging {
  def toOpiskeluoikeus: Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] =
    if (patches.length < version) {
      Left(KoskiErrorCategory.notFound.versiotaEiLöydy("Versiota " + version + " ei löydy opiskeluoikeuden " + oid + " historiasta."))
    } else {
      readAsOpiskeluoikeus(asOpiskeluoikeusJson, oid, version, patches.last.aikaleima).left.map { errors =>
        logger.error(s"Opiskeluoikeuden $oid version $version deserialisointi epäonnistui: $errors")
        KoskiErrorCategory.internalError("Historiaversion deserialisointi epäonnistui")
      }
    }

  lazy val asOpiskeluoikeusJson: JValue = {
    JsonMethods.fromJsonNode(patches.foldLeft(JsonNodeFactory.instance.objectNode(): JsonNode) { (current, diff) =>
      patch(current, diff)
    })
  }

  private def patch(current: JsonNode, diff: OpiskeluoikeusHistoryPatch): JsonNode = try {
    JsonPatch.fromJson(JsonMethods.asJsonNode(diff.muutos)).apply(current)
  } catch {
    case e: Exception =>
      throw new JsonPatchException(s"Opiskeluoikeuden $oid historiaversion patch ${diff.versionumero} epäonnistui", e)
  }
}

class JsonPatchException(msg: String, cause: Throwable) extends Exception(msg, cause)
