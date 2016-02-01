package fi.oph.tor.history

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonpatch.JsonPatch
import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.Tables._
import fi.oph.tor.db.TorDatabase._
import fi.oph.tor.db.{Futures, GlobalExecutionContext, OpiskeluOikeusHistoryRow}
import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.json.Json
import fi.oph.tor.schema.OpiskeluOikeus
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.log.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write

case class OpiskeluoikeusHistoryRepository(db: DB) extends Futures with GlobalExecutionContext with Logging with JsonMethods {
  def findByOpiskeluoikeusId(id: Int, maxVersion: Int = Int.MaxValue)(implicit user: TorUser): Option[Seq[OpiskeluOikeusHistoryRow]] = {
    val query = OpiskeluOikeudetWithAccessCheck.filter(_.id === id)
      .join(OpiskeluOikeusHistoria.filter(_.versionumero <= maxVersion))
      .on(_.id === _.opiskeluoikeusId)
      .map(_._2)
      .sortBy(_.versionumero.asc)

    await(db.run(query.result)) match {
      case Nil => None
      case rows => Some(rows)
    }
  }

  def findVersion(id: Int, version: Int)(implicit user: TorUser): Either[HttpStatus, OpiskeluOikeus] = {
    findByOpiskeluoikeusId(id, version) match {
      case Some(diffs) =>
        if (diffs.length < version) {
          Left(HttpStatus(TorErrorCategory.notFound, "Version: " + version + " not found for opiskeluoikeus: " + id))
        } else {
          val oikeusVersion = diffs.foldLeft(JsonNodeFactory.instance.objectNode(): JsonNode) { (current, diff) =>
            val patch = JsonPatch.fromJson(asJsonNode(diff.muutos))
            patch.apply(current)
          }
          Right(Json.fromJValue[OpiskeluOikeus](fromJsonNode(oikeusVersion)).copy(id = Some(id), versionumero = Some(version)))
        }
      case None => Left(HttpStatus(TorErrorCategory.notFound, "Opiskeluoikeus not found with id: " + id))
    }
  }

  def createAction(opiskeluoikeusId: Int, versionumero: Int, kayttäjäOid: String, muutos: JValue): DBIOAction[Int, NoStream, Write] = {
    OpiskeluOikeusHistoria.map { row =>
      (row.opiskeluoikeusId, row.kayttajaOid, row.muutos, row.versionumero)
    } +=(opiskeluoikeusId, kayttäjäOid, muutos, versionumero)
  }
}

