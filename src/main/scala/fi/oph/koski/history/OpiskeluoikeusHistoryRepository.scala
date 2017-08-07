package fi.oph.koski.history

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonpatch.JsonPatch
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db.{KoskiDatabaseMethods, OpiskeluoikeusHistoryRow, Tables}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Opiskeluoikeus
import org.json4s._
import org.json4s.jackson.JsonMethods
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write

case class OpiskeluoikeusHistoryRepository(db: DB) extends KoskiDatabaseMethods with Logging with JsonMethods {
  def findByOpiskeluoikeusOid(oid: String, maxVersion: Int = Int.MaxValue)(implicit user: KoskiSession): Option[Seq[OpiskeluoikeusHistoryRow]] = {
    val query = OpiskeluOikeudetWithAccessCheck.filter(_.oid === oid)
      .join(OpiskeluoikeusHistoria.filter(_.versionumero <= maxVersion))
      .on(_.id === _.opiskeluoikeusId)
      .map(_._2)
      .sortBy(_.versionumero.asc)

    runDbSync(query.result) match {
      case Nil => None
      case rows: Seq[OpiskeluoikeusHistoryRow] => Some(rows)
    }
  }

  def findVersion(oid: String, version: Int)(implicit user: KoskiSession): Either[HttpStatus, Opiskeluoikeus] = {
    findByOpiskeluoikeusOid(oid, version) match {
      case Some(diffs) =>
        if (diffs.length < version) {
          Left(KoskiErrorCategory.notFound.versiotaEiLöydy("Versiota " + version + " ei löydy opiskeluoikeuden " + oid + " historiasta."))
        } else {
          val oikeusVersion: JsonNode = diffs.foldLeft(JsonNodeFactory.instance.objectNode(): JsonNode) { (current, diff) =>
            val patch = JsonPatch.fromJson(asJsonNode(diff.muutos))
            patch.apply(current)
          }
          try {
            Right(Tables.OpiskeluoikeusTable.readData(fromJsonNode(oikeusVersion), diffs.head.opiskeluoikeusId, oid, version))
          } catch {
            case e: Exception =>
              logger.error(e)(s"Opiskeluoikeuden $oid version $version deserialisointi epäonnistui")
              Left(KoskiErrorCategory.internalError("Historiaversion deserialisointi epäonnistui"))
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
}

