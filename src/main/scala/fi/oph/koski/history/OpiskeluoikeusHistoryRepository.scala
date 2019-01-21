package fi.oph.koski.history

import java.sql.Timestamp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonpatch.JsonPatch
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db.{KoskiDatabaseMethods, OpiskeluoikeusHistoryRow, OpiskeluoikeusRow, Tables}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{KoskiSession, Rooli}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.KoskeenTallennettavaOpiskeluoikeus
import fi.oph.koski.schema.annotation.SensitiveData
import org.json4s._
import org.json4s.jackson.JsonMethods
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write

case class OpiskeluoikeusHistoryRepository(db: DB) extends KoskiDatabaseMethods with Logging with JsonMethods {

  def findByOpiskeluoikeusOid(oid: String, maxVersion: Int = Int.MaxValue)(implicit user: KoskiSession): Option[List[OpiskeluoikeusHistory]] = {
    val query = OpiskeluOikeudetWithAccessCheck.filter(_.oid === oid)
      .join(OpiskeluoikeusHistoria.filter(_.versionumero <= maxVersion))
      .on(_.id === _.opiskeluoikeusId)
      .sortBy(_._2.versionumero.asc)

    runDbSync(query.result).map(toOpiskeluoikeusHistory) match {
      case Nil => None
      case rows: Seq[OpiskeluoikeusHistory] => Some(rows.toList)
    }
  }

  def findVersion(oid: String, version: Int)(implicit user: KoskiSession): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    findByOpiskeluoikeusOid(oid, version) match {
      case Some(diffs) =>
        if (diffs.length < version) {
          Left(KoskiErrorCategory.notFound.versiotaEiLöydy("Versiota " + version + " ei löydy opiskeluoikeuden " + oid + " historiasta."))
        } else {
          val oikeusVersion: JsonNode = diffs.foldLeft(JsonNodeFactory.instance.objectNode(): JsonNode) { (current, diff) =>
            val patch = JsonPatch.fromJson(asJsonNode(diff.muutos))
            patch.apply(current)
          }

          Tables.OpiskeluoikeusTable.readAsOpiskeluoikeus(fromJsonNode(oikeusVersion), oid, version, diffs.last.aikaleima).left.map { errors =>
            logger.error(s"Opiskeluoikeuden $oid version $version deserialisointi epäonnistui: $errors")
            KoskiErrorCategory.internalError("Historiaversion deserialisointi epäonnistui")
          }
        }
      case None => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + oid + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
    }
  }

  def createAction(opiskeluoikeusId: Int, versionumero: Int, kayttäjäOid: String, muutos: JValue): DBIOAction[Int, NoStream, Write] = {
    OpiskeluoikeusHistoria.map { row =>
      (row.opiskeluoikeusId, row.kayttajaOid, row.muutos, row.versionumero)
    } +=(opiskeluoikeusId, kayttäjäOid, muutos, versionumero)
  }

  def toOpiskeluoikeusHistory(row: (OpiskeluoikeusRow, OpiskeluoikeusHistoryRow)) = OpiskeluoikeusHistory(row._1.oid, row._2.versionumero, row._2.aikaleima, row._2.kayttajaOid, row._2.muutos)
}

// TODO: use LocalDateTime instead of Timestamp for consistency with KoskeenTallennettavaOpiskeluoikeus
case class OpiskeluoikeusHistory(opiskeluoikeusOid: String, versionumero: Int, aikaleima: Timestamp, kayttajaOid: String, @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN)) muutos: JValue)
