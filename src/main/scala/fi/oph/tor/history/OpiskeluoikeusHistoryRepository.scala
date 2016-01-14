package fi.oph.tor.history

import fi.oph.tor.db.PostgresDriverWithJsonSupport.api._
import fi.oph.tor.db.Tables._
import fi.oph.tor.db.TorDatabase._
import fi.oph.tor.db.{Futures, GlobalExecutionContext, OpiskeluOikeusHistoryRow}
import fi.oph.tor.toruser.TorUser
import fi.vm.sade.utils.slf4j.Logging
import org.json4s._
import slick.dbio.DBIOAction
import slick.dbio.Effect.Write

case class OpiskeluoikeusHistoryRepository(db: DB) extends Futures with GlobalExecutionContext with Logging {
  def findByOpiskeluoikeusId(id: Int, maxVersion: Int = Int.MaxValue)(implicit user: TorUser): Option[Seq[OpiskeluOikeusHistoryRow]] = {
    val query = OpiskeluOikeudetWithAccessCheck.filter(_.id === id)
      .join(OpiskeluOikeusHistoria.filter(_.versionumero <= maxVersion))
      .on(_.id === _.opiskeluoikeusId)
      .map(_._2)
      .sortBy(_.versionumero.asc)

    Some(await(db.run(query.result)))
  }

  def createAction(opiskeluoikeusId: Int, versionumero: Int, kayttäjäOid: String, muutos: JValue): DBIOAction[Int, NoStream, Write] = {
    OpiskeluOikeusHistoria.map { row =>
      (row.opiskeluoikeusId, row.kayttajaOid, row.muutos, row.versionumero)
    } +=(opiskeluoikeusId, kayttäjäOid, muutos, versionumero)
  }
}

