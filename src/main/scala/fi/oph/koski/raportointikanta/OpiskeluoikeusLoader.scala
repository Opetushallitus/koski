package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}
import java.util.concurrent.atomic.AtomicLong

import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluoikeusRow}
import fi.oph.koski.json.{JsonManipulation, JsonSerializer}
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
    raportointiDatabase.deleteOsasuoritukset
    val result = processByPage[OpiskeluoikeusRow, LoadResult](
      page => opiskeluoikeusQueryRepository.opiskeluoikeusQuerySync(filters, Some(Ascending("id")), Some(PaginationSettings(page, BatchSize)))(systemUser).map(_._1),
      opiskeluoikeusRows => {
        if (opiskeluoikeusRows.nonEmpty) {
          val (errors, outputRows) = opiskeluoikeusRows.par.map(buildRow).seq.partition(_.isLeft)
          raportointiDatabase.loadOpiskeluoikeudet(outputRows.map(_.right.get._1))
          val päätasonSuoritusRows = outputRows.flatMap(_.right.get._2)
          val osasuoritusRows = outputRows.flatMap(_.right.get._3)
          raportointiDatabase.loadPäätasonSuoritukset(päätasonSuoritusRows)
          raportointiDatabase.loadOsasuoritukset(osasuoritusRows)
          errors.map(_.left.get) :+ LoadProgressResult(outputRows.size, päätasonSuoritusRows.size + osasuoritusRows.size)
        } else {
          Seq(LoadCompleted())
        }
      }
    )
    result.doOnEach(new Subscriber[LoadResult] {
      val startTime = System.currentTimeMillis
      var opiskeluoikeusCount = 0
      var suoritusCount = 0
      var errors = 0
      override def onNext(r: LoadResult) = r match {
        case LoadErrorResult(_, _) => errors += 1
        case LoadProgressResult(o, s) => {
          opiskeluoikeusCount += o
          suoritusCount += s
        }
        case LoadCompleted(_) =>
      }
      override def onError(e: Throwable) { logger.error(e)("Opiskeluoikeuksien lataus epäonnistui") }
      override def onCompleted() {
        val elapsedSeconds = (System.currentTimeMillis - startTime) / 1000.0
        val rate = (opiskeluoikeusCount + errors) / Math.max(1.0, elapsedSeconds)
        logger.info(s"Ladattiin $opiskeluoikeusCount opiskeluoikeutta, $suoritusCount suoritusta, $errors virhettä, ${(rate*60).round} opiskeluoikeutta/min")
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

  private def buildRow(inputRow: OpiskeluoikeusRow): Either[LoadErrorResult, Tuple3[ROpiskeluoikeusRow, Seq[RPäätasonSuoritusRow], Seq[ROsasuoritusRow]]] = {
    Try {
      val oo = inputRow.toOpiskeluoikeus
      val ooRow = buildROpiskeluoikeusRow(inputRow.oppijaOid, inputRow.aikaleima, oo)
      val suoritusRows = oo.suoritukset.map(ps => buildSuoritusRows(inputRow.oid, oo.getOppilaitos, ps))
      (ooRow, suoritusRows.map(_._1), suoritusRows.flatMap(_._2))
    }.toEither.left.map(t => LoadErrorResult(inputRow.oid, t.toString))
  }

  private def buildROpiskeluoikeusRow(_oppijaOid: String, _aikaleima: Timestamp, o: KoskeenTallennettavaOpiskeluoikeus) =
    ROpiskeluoikeusRow(
      opiskeluoikeusOid = o.oid.get,
      versionumero = o.versionumero.get,
      aikaleima = _aikaleima,
      sisältyyOpiskeluoikeuteenOid = o.sisältyyOpiskeluoikeuteen.map(_.oid),
      oppijaOid = _oppijaOid,
      oppilaitosOid = o.getOppilaitos.oid,
      oppilaitosNimi = convertLocalizedString(o.oppilaitos.flatMap(_.nimi)),
      oppilaitosKotipaikka = o.oppilaitos.flatMap(_.kotipaikka).map(_.koodiarvo.stripPrefix("kunta_")),
      oppilaitosnumero = o.oppilaitos.flatMap(_.oppilaitosnumero).map(_.koodiarvo),
      koulutustoimijaOid = o.koulutustoimija.getOrElse(throw new RuntimeException("Koulutustoimija puuttuu")).oid,
      koulutustoimijaNimi = convertLocalizedString(o.koulutustoimija.flatMap(_.nimi)),
      koulutusmuoto = o.tyyppi.koodiarvo,
      alkamispäivä = o.alkamispäivä.map(Date.valueOf),
      päättymispäivä = o.tila.opiskeluoikeusjaksot.lastOption.filter(_.opiskeluoikeusPäättynyt).map(v => Date.valueOf(v.alku)),
      viimeisinTila = o.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo),
      lisätiedotHenkilöstökoulutus = o.lisätiedot.collect {
        case l: AmmatillisenOpiskeluoikeudenLisätiedot => l.henkilöstökoulutus
      }.getOrElse(false),
      lisätiedotKoulutusvienti = o.lisätiedot.collect {
        case l: AmmatillisenOpiskeluoikeudenLisätiedot => l.koulutusvienti
      }.getOrElse(false)
    )

  private val suoritusId = new AtomicLong()

  private def buildSuoritusRows(opiskeluoikeusOid: String, oppilaitos: OrganisaatioWithOid, ps: PäätasonSuoritus) = {
    val päätasonSuoritusId = suoritusId.incrementAndGet()
    val toimipiste = (ps match {
      case stp: MahdollisestiToimipisteellinen => stp.toimipiste
      case _ => None
    }).getOrElse(oppilaitos)
    val päätaso = RPäätasonSuoritusRow(
      päätasonSuoritusId = päätasonSuoritusId,
      opiskeluoikeusOid = opiskeluoikeusOid,
      suorituksenTyyppi = ps.tyyppi.koodiarvo,
      koulutusmoduuliKoodisto = ps.koulutusmoduuli.tunniste match {
        case k: Koodistokoodiviite => Some(k.koodistoUri)
        case _ => None
      },
      koulutusmoduuliKoodiarvo = ps.koulutusmoduuli.tunniste.koodiarvo,
      koulutusmoduuliKoulutustyyppi = ps.koulutusmoduuli match {
        case k: Koulutus => k.koulutustyyppi.map(_.koodiarvo)
        case _ => None
      },
      vahvistusPäivä = ps.vahvistus.map(v => Date.valueOf(v.päivä)),
      toimipisteOid = toimipiste.oid,
      toimipisteNimi = convertLocalizedString(toimipiste.nimi),
      data = JsonManipulation.removeFields(JsonSerializer.serializeWithRoot(ps), Set("osasuoritukset", "tyyppi", "toimipiste", "koulutustyyppi"))
    )
    val osat = ps.osasuoritukset.getOrElse(List.empty).flatMap(os => {
      buildROsasuoritusRow(päätasonSuoritusId, None, opiskeluoikeusOid, os)
    })
    (päätaso, osat)
  }

  private def buildROsasuoritusRow(päätasonSuoritusId: Long, ylempiOsasuoritusId: Option[Long], opiskeluoikeusOid: String, os: Suoritus): Seq[ROsasuoritusRow] = {
    val osasuoritusId = suoritusId.incrementAndGet()
    ROsasuoritusRow(
      osasuoritusId = osasuoritusId,
      ylempiOsasuoritusId = ylempiOsasuoritusId,
      päätasonSuoritusId = päätasonSuoritusId,
      opiskeluoikeusOid = opiskeluoikeusOid,
      suorituksenTyyppi = os.tyyppi.koodiarvo,
      koulutusmoduuliKoodisto = os.koulutusmoduuli.tunniste match {
        case k: Koodistokoodiviite => Some(k.koodistoUri)
        case _ => None
      },
      koulutusmoduuliKoodiarvo = os.koulutusmoduuli.tunniste.koodiarvo,
      koulutusmoduuliPaikallinen = os.koulutusmoduuli.tunniste match {
        case k: Koodistokoodiviite => false
        case k: PaikallinenKoodi => true
      },
      koulutusmoduuliPakollinen = os.koulutusmoduuli match {
        case v: Valinnaisuus => Some(v.pakollinen)
        case _ => None
      },
      vahvistusPäivä = os.vahvistus.map(v => Date.valueOf(v.päivä)),
      data = JsonManipulation.removeFields(JsonSerializer.serializeWithRoot(os), Set("osasuoritukset", "tyyppi"))
    ) +: os.osasuoritukset.getOrElse(List.empty).flatMap(os2 => {
      buildROsasuoritusRow(päätasonSuoritusId, Some(osasuoritusId), opiskeluoikeusOid, os2)
    })
  }
}

sealed trait LoadResult
case class LoadErrorResult(oid: String, error: String) extends LoadResult
case class LoadProgressResult(opiskeluoikeusCount: Int, suoritusCount: Int) extends LoadResult
case class LoadCompleted(done: Boolean = true) extends LoadResult
