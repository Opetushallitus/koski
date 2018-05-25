package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

import fi.oph.koski.date.DateOrdering
import fi.oph.koski.db.{GlobalExecutionContext, OpiskeluoikeusRow}
import fi.oph.koski.json.JsonManipulation
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.{OpiskeluoikeusQueryFilter, OpiskeluoikeusQueryService}
import fi.oph.koski.schema._
import fi.oph.koski.util.PaginationSettings
import fi.oph.koski.util.SortOrder.Ascending
import fi.oph.koski.raportointikanta.LoaderUtils.convertLocalizedString
import org.json4s.JValue
import rx.Observable.{create => createObservable}
import rx.Observer
import rx.functions.{Func0, Func2}
import rx.lang.scala.{Observable, Subscriber}
import rx.observables.SyncOnSubscribe.createStateful

import scala.util.Try

object OpiskeluoikeusLoader extends Logging {
  private val DefaultBatchSize = 250

  def loadOpiskeluoikeudet(opiskeluoikeusQueryRepository: OpiskeluoikeusQueryService, filters: List[OpiskeluoikeusQueryFilter], systemUser: KoskiSession, raportointiDatabase: RaportointiDatabase, batchSize: Int = DefaultBatchSize): Observable[LoadResult] = {
    logger.info("Ladataan opiskeluoikeuksia...")
    raportointiDatabase.deleteOpiskeluoikeudet
    raportointiDatabase.deleteOpiskeluoikeusAikajaksot
    raportointiDatabase.deletePäätasonSuoritukset
    raportointiDatabase.deleteOsasuoritukset
    val result = processByPage[OpiskeluoikeusRow, LoadResult](
      page => opiskeluoikeusQueryRepository.opiskeluoikeusQuerySync(filters, Some(Ascending("id")), Some(PaginationSettings(page, batchSize)))(systemUser).map(_._1),
      opiskeluoikeusRows => {
        if (opiskeluoikeusRows.nonEmpty) {
          val (errors, outputRows) = opiskeluoikeusRows.par.map(buildRow).seq.partition(_.isLeft)
          raportointiDatabase.loadOpiskeluoikeudet(outputRows.map(_.right.get._1))
          val aikajaksoRows = outputRows.flatMap(_.right.get._2)
          val päätasonSuoritusRows = outputRows.flatMap(_.right.get._3)
          val osasuoritusRows = outputRows.flatMap(_.right.get._4)
          raportointiDatabase.loadOpiskeluoikeusAikajaksot(aikajaksoRows)
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
        val indexStartTime = System.currentTimeMillis
        logger.info("Luodaan indeksit opiskeluoikeuksille...")
        raportointiDatabase.createOpiskeluoikeusIndexes
        val indexElapsedSeconds = (System.currentTimeMillis - indexStartTime)/1000
        logger.info(s"Luotiin indeksit opiskeluoikeuksille, ${indexElapsedSeconds} s")
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

  private val suoritusIds = new AtomicLong()

  private def buildRow(inputRow: OpiskeluoikeusRow): Either[LoadErrorResult, Tuple4[ROpiskeluoikeusRow, Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROsasuoritusRow]]] = {
    Try {
      val oo = inputRow.toOpiskeluoikeus
      val ooRow = buildROpiskeluoikeusRow(inputRow.oppijaOid, inputRow.aikaleima, oo)
      val aikajaksoRows = buildROpiskeluoikeusAikajaksoRows(inputRow.oid, oo)
      val suoritusRows = oo.suoritukset.zipWithIndex.map { case (ps, i) => buildSuoritusRows(inputRow.oid, oo.getOppilaitos, ps, (inputRow.data \ "suoritukset")(i), suoritusIds.incrementAndGet) }
      (ooRow, aikajaksoRows, suoritusRows.map(_._1), suoritusRows.flatMap(_._2))
    }.toEither.left.map(t => LoadErrorResult(inputRow.oid, t.toString))
  }

  private def buildROpiskeluoikeusRow(oppijaOid: String, aikaleima: Timestamp, o: KoskeenTallennettavaOpiskeluoikeus) =
    ROpiskeluoikeusRow(
      opiskeluoikeusOid = o.oid.get,
      versionumero = o.versionumero.get,
      aikaleima = aikaleima,
      sisältyyOpiskeluoikeuteenOid = o.sisältyyOpiskeluoikeuteen.map(_.oid),
      oppijaOid = oppijaOid,
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


  private def buildROpiskeluoikeusAikajaksoRows(opiskeluoikeusOid: String, o: KoskeenTallennettavaOpiskeluoikeus): Seq[ROpiskeluoikeusAikajaksoRow] = {
    aikajaksot(o).map { case (alku, loppu) => buildROpiskeluoikeusAikajaksoRowForOneDay(opiskeluoikeusOid, o, alku).copy(loppu = Date.valueOf(loppu)) }
  }

  private def buildROpiskeluoikeusAikajaksoRowForOneDay(opiskeluoikeusOid: String, o: KoskeenTallennettavaOpiskeluoikeus, päivä: LocalDate): ROpiskeluoikeusAikajaksoRow = {
    val jakso = o.tila.opiskeluoikeusjaksot
      .filterNot(_.alku.isAfter(päivä))
      .lastOption.getOrElse(throw new RuntimeException(s"Opiskeluoikeusjaksoa ei löydy $opiskeluoikeusOid $päivä"))

    val ammatillisenLisätiedot: Option[AmmatillisenOpiskeluoikeudenLisätiedot] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case a: AmmatillisenOpiskeluoikeudenLisätiedot => Some(a)
      case _ => None
    } else None
    val aikuistenPerusopetuksenLisätiedot: Option[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case l: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot => Some(l)
      case _ => None
    } else None
    val perusopetuksenLisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case l: PerusopetuksenOpiskeluoikeudenLisätiedot => Some(l)
      case _ => None
    } else None

    def ammatillinenAikajakso(lisätieto: AmmatillisenOpiskeluoikeudenLisätiedot => Option[List[Aikajakso]]): Byte =
      ammatillisenLisätiedot.flatMap(lisätieto).flatMap(_.find(_.contains(päivä))).size.toByte

    ROpiskeluoikeusAikajaksoRow(
      opiskeluoikeusOid = opiskeluoikeusOid,
      alku = Date.valueOf(päivä),
      loppu = Date.valueOf(päivä),
      tila = jakso.tila.koodiarvo,
      opiskeluoikeusPäättynyt = jakso.opiskeluoikeusPäättynyt,
      opintojenRahoitus = jakso match {
        case k: KoskiOpiskeluoikeusjakso => k.opintojenRahoitus.map(_.koodiarvo)
        case _ => None
      },
      erityinenTuki = ammatillinenAikajakso(_.erityinenTuki),
      vaativanErityisenTuenErityinenTehtävä = ammatillinenAikajakso(_.vaativanErityisenTuenErityinenTehtävä),
      vaikeastiVammainen = (
        ammatillinenAikajakso(_.vaikeastiVammainen) +
        aikuistenPerusopetuksenLisätiedot.flatMap(_.vaikeastiVammainen).flatMap(_.find(_.contains(päivä))).size +
        perusopetuksenLisätiedot.flatMap(_.vaikeastiVammainen).flatMap(_.find(_.contains(päivä))).size
      ).toByte,
      vammainenJaAvustaja = ammatillinenAikajakso(_.vammainenJaAvustaja),
      osaAikaisuus = ammatillisenLisätiedot.flatMap(_.osaAikaisuusjaksot).flatMap(_.find(_.contains(päivä))).map(_.osaAikaisuus).getOrElse(100).toByte,
      opiskeluvalmiuksiaTukevatOpinnot = ammatillisenLisätiedot.flatMap(_.opiskeluvalmiuksiaTukevatOpinnot).flatMap(_.find(_.contains(päivä))).size.toByte,
      vankilaopetuksessa = ammatillinenAikajakso(_.vankilaopetuksessa)
    )
    // Note: When adding something here, remember to update aikajaksojenAlkupäivät (below), too
  }

  val IndefiniteFuture = LocalDate.of(2112, 12, 21) // no special meaning, but same value used in Virta to mean "forever"

  private def aikajaksot(o: KoskeenTallennettavaOpiskeluoikeus): Seq[(LocalDate, LocalDate)] = {
    val alkupäivät: Seq[LocalDate] = mahdollisetAikajaksojenAlkupäivät(o)
    val alkamispäivä: LocalDate = o.tila.opiskeluoikeusjaksot.headOption.map(_.alku).getOrElse(throw new RuntimeException(s"Alkamispäivä puuttuu ${o.oid}"))
    val päättymispäivä: LocalDate = o.tila.opiskeluoikeusjaksot.lastOption.filter(_.opiskeluoikeusPäättynyt).map(_.alku).getOrElse(IndefiniteFuture)
    alkupäivät
      .filterNot(_.isBefore(alkamispäivä))
      .filterNot(_.isAfter(päättymispäivä))
      .zipWithIndex
      .map {
        case (alkupäivä, index) if index < (alkupäivät.size - 1) => (alkupäivä, alkupäivät(index + 1))
        case (alkupäivä, _) => (alkupäivä, päättymispäivä)
      }
  }

  private def mahdollisetAikajaksojenAlkupäivät(o: KoskeenTallennettavaOpiskeluoikeus): Seq[LocalDate] = {
    // logiikka: uusi r_opiskeluoikeus_aikajakso-rivi pitää aloittaa, jos ko. päivänä alkaa joku jakso (erityinen tuki tms),
    // tai jos edellisenä päivänä on loppunut joku jakso.
    val lisätiedotAikajaksot: Seq[Aikajakso] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case aol: AmmatillisenOpiskeluoikeudenLisätiedot =>
        Seq(
          aol.erityinenTuki,
          aol.vaativanErityisenTuenErityinenTehtävä,
          aol.vaikeastiVammainen,
          aol.vammainenJaAvustaja,
          aol.vankilaopetuksessa
        ).flatMap(_.getOrElse(List.empty)) ++
        aol.opiskeluvalmiuksiaTukevatOpinnot.getOrElse(Seq.empty).map(j => Aikajakso(j.alku, Some(j.loppu)))
      case apol: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot =>
        Seq(
          apol.vaikeastiVammainen
        ).flatMap(_.getOrElse(List.empty))
      case pol: PerusopetuksenOpiskeluoikeudenLisätiedot =>
        Seq(
          pol.vaikeastiVammainen
        ).flatMap(_.getOrElse(List.empty))
      case _ => Seq()
    } else Seq()

    (o.tila.opiskeluoikeusjaksot.map(_.alku) ++
      lisätiedotAikajaksot.map(_.alku) ++
      lisätiedotAikajaksot.map(_.loppu).filter(_.nonEmpty).map(_.get.plusDays(1)))
      .sorted(DateOrdering.localDateOrdering).distinct
  }

  private val fieldsToExcludeFromPäätasonSuoritusJson = Set("osasuoritukset", "tyyppi", "toimipiste", "koulutustyyppi")
  private val fieldsToExcludeFromOsasuoritusJson = Set("osasuoritukset", "tyyppi")

  private def buildSuoritusRows(opiskeluoikeusOid: String, oppilaitos: OrganisaatioWithOid, ps: PäätasonSuoritus, data: JValue, idGenerator: () => Long) = {
    val päätasonSuoritusId: Long = idGenerator()
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
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromPäätasonSuoritusJson)
    )
    val osat = ps.osasuoritukset.getOrElse(List.empty).zipWithIndex.flatMap {
      case (os, i) => buildROsasuoritusRow(päätasonSuoritusId, None, opiskeluoikeusOid, os, (data \ "osasuoritukset")(i), idGenerator)
    }
    (päätaso, osat)
  }

  private def buildROsasuoritusRow(päätasonSuoritusId: Long, ylempiOsasuoritusId: Option[Long], opiskeluoikeusOid: String, os: Suoritus, data: JValue, idGenerator: () => Long): Seq[ROsasuoritusRow] = {
    val osasuoritusId: Long = idGenerator()
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
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromOsasuoritusJson)
    ) +: os.osasuoritukset.getOrElse(List.empty).zipWithIndex.flatMap {
      case (os2, i) => buildROsasuoritusRow(päätasonSuoritusId, Some(osasuoritusId), opiskeluoikeusOid, os2, (data \ "osasuoritukset")(i), idGenerator)
    }
  }
}

sealed trait LoadResult
case class LoadErrorResult(oid: String, error: String) extends LoadResult
case class LoadProgressResult(opiskeluoikeusCount: Int, suoritusCount: Int) extends LoadResult
case class LoadCompleted(done: Boolean = true) extends LoadResult
