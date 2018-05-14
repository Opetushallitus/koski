package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryFilter, OpiskeluoikeusQueryService}
import fi.oph.koski.schema.{Koodistokoodiviite, KoskeenTallennettavaOpiskeluoikeus, Koulutus, PäätasonSuoritus}
import fi.oph.koski.util.PaginationSettings
import fi.oph.koski.util.SortOrder.Ascending
import rx.Observable.{create => createObservable}
import rx.Observer
import rx.functions.{Func0, Func2}
import rx.lang.scala.{Observable, Subscriber}
import rx.observables.SyncOnSubscribe.createStateful

object OpiskeluoikeusLoader extends Logging {
  private val BatchSize = 1000

  def loadOpiskeluoikeudet(opiskeluoikeusQueryRepository: OpiskeluoikeusQueryService, filters: List[OpiskeluoikeusQueryFilter], systemUser: KoskiSession, raportointiDatabase: RaportointiDatabase): Observable[LoadResult] = {
    logger.info("Ladataan opiskeluoikeuksia...")
    raportointiDatabase.deleteOpiskeluoikeudet
    raportointiDatabase.deletePäätasonSuoritukset
    val result = processByPage[OpiskeluoikeusRow, LoadResult](
      page => opiskeluoikeusQueryRepository.opiskeluoikeusQuerySync(filters, Some(Ascending("id")), Some(PaginationSettings(page, BatchSize)))(systemUser).map(_._1),
      opiskeluoikeusRows => {
        val rows = opiskeluoikeusRows.map(row => {
          val oo = row.toOpiskeluoikeus
          (buildROpiskeluoikeusRow(row.oppijaOid, row.aikaleima, oo), oo.suoritukset.map(s => buildRPäätasonSuoritusRow(row.oid, s)))
        })
        raportointiDatabase.loadOpiskeluoikeudet(rows.map(_._1))
        raportointiDatabase.loadPäätasonSuoritukset(rows.map(_._2).flatten)
        rows.map(r => LoadResult(r._1.opiskeluoikeusOid))
      }
    )
    result.doOnEach(new Subscriber[LoadResult] {
      var count = 0
      override def onNext(r: LoadResult) = { count += 1 }
      override def onError(e: Throwable) { logger.error(e)("Opiskeluoikeuksien lataus epäonnistui") }
      override def onCompleted() { logger.info(s"Ladattiin $count opiskeluoikeutta") }
    })
  }

  private def processByPage[A, B](loadRows: Int => Seq[A], processRows: Seq[A] => Seq[B]): Observable[B] = {
    import rx.lang.scala.JavaConverters._
    def loadRowsInt(page: Int): (Seq[B], Int) = {
      val rows = loadRows(page)
      (processRows(rows), page)
    }
    createObservable(createStateful[(Seq[B], Int), Seq[B]](
      (() => loadRowsInt(0)): Func0[_ <: (Seq[B], Int)],
      ((state, observer) => {
        val (loadResults, page) = state
        if (loadResults.isEmpty) {
          observer.onCompleted()
          (Nil, 0)
        } else {
          observer.onNext(loadResults)
          loadRowsInt(page + 1)
        }
      }): Func2[_ >: (Seq[B], Int), _ >: Observer[_ >: Seq[B]], _ <: (Seq[B], Int)]
    )).asScala.flatMap(Observable.from(_))
  }

  private def buildROpiskeluoikeusRow(_oppijaOid: String, _aikaleima: Timestamp, o: KoskeenTallennettavaOpiskeluoikeus) =
    ROpiskeluoikeusRow(
      opiskeluoikeusOid = o.oid.get,
      versionumero = o.versionumero.get,
      aikaleima = _aikaleima,
      oppijaOid = _oppijaOid,
      oppilaitosOid = o.oppilaitos.get.oid,
      koulutustoimijaOid = o.koulutustoimija.get.oid,
      koulutusmuoto = o.tyyppi.koodiarvo
    )
  private def buildRPäätasonSuoritusRow(opiskeluoikeusOid: String, s: PäätasonSuoritus) =
    RPäätasonSuoritusRow(
      opiskeluoikeusOid = opiskeluoikeusOid,
      suorituksenTyyppi = s.tyyppi.koodiarvo,
      koulutustyyppi = s.koulutusmoduuli match {
        case k: Koulutus => k.koulutustyyppi.map(_.koodiarvo)
        case _ => None
      },
      koulutusmoduuliKoodisto = s.koulutusmoduuli.tunniste match {
        case k: Koodistokoodiviite => Some(k.koodistoUri)
        case _ => None
      },
      koulutusmoduuliKoodiarvo = s.koulutusmoduuli.tunniste.koodiarvo,
      vahvistusPäivä = s.vahvistus.map(v => Date.valueOf(v.päivä))
    )
}

case class LoadResult(oid: String, error: Option[String] = None)
