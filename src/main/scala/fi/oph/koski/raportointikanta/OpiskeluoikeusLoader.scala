package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}

import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluoikeusRow}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryFilter, OpiskeluoikeusQueryService}
import fi.oph.koski.schema._
import fi.oph.koski.util.PaginationSettings
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.raportointikanta.LoaderUtils.convertLocalizedString
import rx.Observable.{create => createObservable}
import rx.Observer
import rx.functions.{Func0, Func2}
import rx.lang.scala.{Observable, Subscriber}
import rx.observables.SyncOnSubscribe.createStateful
import scala.util.Try

object OpiskeluoikeusLoader extends Logging {
  private val BatchSize = 250

  def loadOpiskeluoikeudet(opiskeluoikeusQueryRepository: OpiskeluoikeusQueryService, filters: List[OpiskeluoikeusQueryFilter], systemUser: KoskiSession, raportointiDatabase: RaportointiDatabase): Observable[LoadResult] = {
    logger.info("Ladataan opiskeluoikeuksia...")
    raportointiDatabase.deleteOpiskeluoikeudet
    raportointiDatabase.deletePäätasonSuoritukset
    val result = processByPage[OpiskeluoikeusRow, LoadResult](
      page => opiskeluoikeusQueryRepository.opiskeluoikeusQuerySync(filters, Some(Ascending("id")), Some(PaginationSettings(page, BatchSize)))(systemUser).map(_._1),
      opiskeluoikeusRows => {
        if (opiskeluoikeusRows.nonEmpty) {
          val (errors, outputRows) = opiskeluoikeusRows.par.map(buildRow).seq.partition(_.isLeft)
          raportointiDatabase.loadOpiskeluoikeudet(outputRows.map(_.right.get._1))
          raportointiDatabase.loadPäätasonSuoritukset(outputRows.map(_.right.get._2).flatten)
          errors.map(_.left.get) :+ LoadProgressResult(outputRows.size)
        } else {
          Seq(LoadCompleted())
        }
      }
    )
    result.doOnEach(new Subscriber[LoadResult] {
      val startTime = System.currentTimeMillis
      var count = 0
      var errors = 0
      override def onNext(r: LoadResult) = r match {
        case LoadErrorResult(_, _) => errors += 1
        case LoadProgressResult(n) => count += n
        case LoadCompleted(_) =>
      }
      override def onError(e: Throwable) { logger.error(e)("Opiskeluoikeuksien lataus epäonnistui") }
      override def onCompleted() {
        val elapsedSeconds = (System.currentTimeMillis - startTime) / 1000.0
        val rate = (count + errors) / Math.max(1.0, elapsedSeconds)
        logger.info(s"Ladattiin $count opiskeluoikeutta, $errors virhettä, ${(rate*60).round}/min")
      }
    })
  }

  private def processByPage[A, B](loadRows: Int => Seq[A], processRows: Seq[A] => Seq[B]): Observable[B] = {
    import rx.lang.scala.JavaConverters._
    def loadRowsInt(page: Int): (Seq[B], Int, Boolean) = {
      val rows = loadRows(page)
      (processRows(rows), page, rows.isEmpty)
    }
    createObservable(createStateful[(Seq[B], Int, Boolean), Seq[B]](
      (() => loadRowsInt(0)): Func0[_ <: (Seq[B], Int, Boolean)],
      ((state, observer) => {
        val (loadResults, page, done) = state
        observer.onNext(loadResults)
        if (done) {
          observer.onCompleted()
          (Nil, 0, true)
        } else {
          loadRowsInt(page + 1)
        }
      }): Func2[_ >: (Seq[B], Int, Boolean), _ >: Observer[_ >: Seq[B]], _ <: (Seq[B], Int, Boolean)]
    )).asScala.flatMap(Observable.from(_))
  }

  private def buildRow(inputRow: OpiskeluoikeusRow): Either[LoadErrorResult, Tuple2[ROpiskeluoikeusRow, Seq[RPäätasonSuoritusRow]]] = {
    Try {
      val oo = inputRow.toOpiskeluoikeus
      (
        buildROpiskeluoikeusRow(inputRow.oppijaOid, inputRow.aikaleima, oo),
        oo.suoritukset.map(s => buildRPäätasonSuoritusRow(inputRow.oid, oo.getOppilaitos, s))
      )
    }.toEither.left.map(t => LoadErrorResult(inputRow.oid, t.toString))
  }

  private def buildROpiskeluoikeusRow(_oppijaOid: String, _aikaleima: Timestamp, o: KoskeenTallennettavaOpiskeluoikeus) =
    ROpiskeluoikeusRow(
      opiskeluoikeusOid = o.oid.get,
      versionumero = o.versionumero.get,
      aikaleima = _aikaleima,
      oppijaOid = _oppijaOid,
      oppilaitosOid = o.getOppilaitos.oid,
      oppilaitosNimi = convertLocalizedString(o.oppilaitos.flatMap(_.nimi)),
      oppilaitosKotipaikka = o.oppilaitos.flatMap(_.kotipaikka).map(_.koodiarvo.stripPrefix("kunta_")),
      oppilaitosnumero = o.oppilaitos.flatMap(_.oppilaitosnumero).map(_.koodiarvo),
      koulutustoimijaOid = o.koulutustoimija.getOrElse(throw new RuntimeException("Koulutustoimija puuttuu")).oid,
      koulutustoimijaNimi = convertLocalizedString(o.koulutustoimija.flatMap(_.nimi)),
      koulutusmuoto = o.tyyppi.koodiarvo
    )
  private def buildRPäätasonSuoritusRow(opiskeluoikeusOid: String, oppilaitos: OrganisaatioWithOid, s: PäätasonSuoritus) = {
    val toimipiste = (s match {
      case stp: MahdollisestiToimipisteellinen => stp.toimipiste
      case _ => None
    }).getOrElse(oppilaitos)
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
      vahvistusPäivä = s.vahvistus.map(v => Date.valueOf(v.päivä)),
      toimipisteOid = toimipiste.oid,
      toimipisteNimi = convertLocalizedString(toimipiste.nimi)
    )
  }
}

sealed trait LoadResult
case class LoadErrorResult(oid: String, error: String) extends LoadResult
case class LoadProgressResult(count: Int) extends LoadResult
case class LoadCompleted(done: Boolean = true) extends LoadResult
