package fi.oph.koski.history

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.fge.jsonpatch.JsonPatch
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiDatabase._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db.{Futures, OpiskeluOikeusHistoryRow, OpiskeluOikeusStoredDataDeserializer}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiUser
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Opiskeluoikeus
import org.json4s._
import org.json4s.jackson.JsonMethods
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write

case class OpiskeluoikeusHistoryRepository(db: DB) extends Futures with Logging with JsonMethods {
  def findByOpiskeluoikeusId(id: Int, maxVersion: Int = Int.MaxValue)(implicit user: KoskiUser): Option[Seq[OpiskeluOikeusHistoryRow]] = {
    val query = OpiskeluOikeudetWithAccessCheck.filter(_.id === id)
      .join(OpiskeluOikeusHistoria.filter(_.versionumero <= maxVersion))
      .on(_.id === _.opiskeluoikeusId)
      .map(_._2)
      .sortBy(_.versionumero.asc)

    await(db.run(query.result)) match {
      case Nil => None
      case rows: Seq[OpiskeluOikeusHistoryRow] => Some(rows)
    }
  }

  def findVersion(id: Int, version: Int)(implicit user: KoskiUser): Either[HttpStatus, Opiskeluoikeus] = {
    findByOpiskeluoikeusId(id, version) match {
      case Some(diffs) =>
        if (diffs.length < version) {
          Left(KoskiErrorCategory.notFound.versiotaEiLöydy("Versiota " + version + " ei löydy opiskeluoikeuden " + id + " historiasta."))
        } else {
          val oikeusVersion = diffs.foldLeft(JsonNodeFactory.instance.objectNode(): JsonNode) { (current, diff) =>
            val patch = JsonPatch.fromJson(asJsonNode(diff.muutos))
            patch.apply(current)
          }

          Right(OpiskeluOikeusStoredDataDeserializer.read(fromJsonNode(oikeusVersion), id, version))
        }
      case None => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta " + id + " ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
    }
  }

  def createAction(opiskeluoikeusId: Int, versionumero: Int, kayttäjäOid: String, muutos: JValue): DBIOAction[Int, NoStream, Write] = {
    OpiskeluOikeusHistoria.map { row =>
      (row.opiskeluoikeusId, row.kayttajaOid, row.muutos, row.versionumero)
    } +=(opiskeluoikeusId, kayttäjäOid, muutos, versionumero)
  }
}

