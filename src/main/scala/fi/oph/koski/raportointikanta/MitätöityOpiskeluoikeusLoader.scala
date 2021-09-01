package fi.oph.koski.raportointikanta

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryService
import rx.lang.scala.Observable

object MitätöityOpiskeluoikeusLoader {
  private val StatusName = "mitätöidyt_opiskeluoikeudet"
  private val DefaultBatchSize = 5000

  def load(
    opiskeluoikeusQueryRepository: OpiskeluoikeusQueryService,
    systemUser: KoskiSpecificSession,
    db: RaportointiDatabase,
  ): Observable[LoadResult] = {
    db.setStatusLoadStarted(StatusName)
    opiskeluoikeusQueryRepository.mapKaikkiMitätöidytOpiskeluoikeudetSivuittain(DefaultBatchSize, systemUser) { batch =>
      if (batch.nonEmpty) {
        loadBatch(db, batch)
      } else {
        // Last batch processed; finalize:
        db.setStatusLoadCompleted(StatusName)
        Seq(LoadCompleted())
      }
    }
  }

  private def loadBatch(db: RaportointiDatabase, batch: Seq[OpiskeluoikeusRow]) = {
    val (errors, outputRows) = batch.par.map(buildRow).seq.partition(_.isLeft)
    db.loadMitätöidytOpiskeluoikeudet(outputRows.map(_.right.get))
    errors.map(_.left.get)
  }

  private def buildRow(raw: OpiskeluoikeusRow): Either[LoadErrorResult, RMitätöityOpiskeluoikeusRow] = {
    for {
      oo <- raw.toOpiskeluoikeus.left.map(e => LoadErrorResult(raw.oid, e.toString()))
      mitätöityPvm <- oo.mitätöintiPäivä.toRight(LoadErrorResult(raw.oid, "Mitätöintipäivämäärän haku epäonnistui"))
    } yield RMitätöityOpiskeluoikeusRow(
        opiskeluoikeusOid = raw.oid,
        versionumero = raw.versionumero,
        aikaleima = raw.aikaleima,
        oppijaOid = raw.oppijaOid,
        mitätöity = mitätöityPvm,
        tyyppi = raw.koulutusmuoto,
        päätasonSuoritusTyypit = oo.suoritukset.map(_.tyyppi.koodiarvo).distinct
      )
  }
}
