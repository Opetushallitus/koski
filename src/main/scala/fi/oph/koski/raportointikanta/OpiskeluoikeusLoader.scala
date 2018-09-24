package fi.oph.koski.raportointikanta

import java.sql.{Date, Timestamp}
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

import fi.oph.koski.util.DateOrdering
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.json.JsonManipulation
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryService
import fi.oph.koski.schema._
import fi.oph.koski.raportointikanta.LoaderUtils.{convertLocalizedString, convertKoodisto}
import org.json4s.JValue
import rx.lang.scala.{Observable, Subscriber}
import scala.concurrent.duration._

import scala.util.Try

object OpiskeluoikeusLoader extends Logging {
  private val DefaultBatchSize = 500

  def loadOpiskeluoikeudet(opiskeluoikeusQueryRepository: OpiskeluoikeusQueryService, systemUser: KoskiSession, raportointiDatabase: RaportointiDatabase, batchSize: Int = DefaultBatchSize): Observable[LoadResult] = {
    raportointiDatabase.setStatusLoadStarted("opiskeluoikeudet")
    deleteEverything(raportointiDatabase)
    val result = opiskeluoikeusQueryRepository.mapKaikkiOpiskeluoikeudetSivuittain(batchSize, systemUser) { opiskeluoikeusRows =>
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
        createIndexes(raportointiDatabase)
        raportointiDatabase.setStatusLoadCompleted("opiskeluoikeudet")
        Seq(LoadCompleted())
      }
    }
    result.doOnEach(progressLogger)
  }

  private def deleteEverything(raportointiDatabase: RaportointiDatabase): Unit = {
    raportointiDatabase.deleteOpiskeluoikeudet
    raportointiDatabase.deleteOpiskeluoikeusAikajaksot
    raportointiDatabase.deletePäätasonSuoritukset
    raportointiDatabase.deleteOsasuoritukset
  }

  private def progressLogger: Subscriber[LoadResult] = new Subscriber[LoadResult] {
    val LoggingInterval = 5.minutes.toMillis
    val startTime = System.currentTimeMillis
    logger.info("Ladataan opiskeluoikeuksia...")

    var opiskeluoikeusCount = 0
    var suoritusCount = 0
    var errors = 0
    var lastLogged = System.currentTimeMillis
    override def onNext(r: LoadResult) = {
      r match {
        case LoadErrorResult(oid, error) =>
          logger.warn(s"Opiskeluoikeuden lataus epäonnistui: $oid $error")
          errors += 1
        case LoadProgressResult(o, s) => {
          opiskeluoikeusCount += o
          suoritusCount += s
        }
        case LoadCompleted(_) =>
      }
      val now = System.currentTimeMillis
      if ((now - lastLogged) > LoggingInterval) {
        logIt(false)
        lastLogged = now
      }
    }
    override def onError(e: Throwable) {
      logger.error(e)("Opiskeluoikeuksien lataus epäonnistui")
    }
    override def onCompleted() {
      logIt(true)
    }
    private def logIt(done: Boolean) = {
      val elapsedSeconds = (System.currentTimeMillis - startTime) / 1000.0
      val rate = (opiskeluoikeusCount + errors) / Math.max(1.0, elapsedSeconds)
      logger.info(s"${if (done) "Ladattiin" else "Ladattu tähän mennessä"} $opiskeluoikeusCount opiskeluoikeutta, $suoritusCount suoritusta, $errors virhettä, ${(rate*60).round} opiskeluoikeutta/min")
    }
  }

  private def createIndexes(raportointiDatabase: RaportointiDatabase): Unit = {
    val indexStartTime = System.currentTimeMillis
    logger.info("Luodaan indeksit opiskeluoikeuksille...")
    raportointiDatabase.createOpiskeluoikeusIndexes
    val indexElapsedSeconds = (System.currentTimeMillis - indexStartTime)/1000
    logger.info(s"Luotiin indeksit opiskeluoikeuksille, ${indexElapsedSeconds} s")
  }

  private val suoritusIds = new AtomicLong()

  private def buildRow(inputRow: OpiskeluoikeusRow): Either[LoadErrorResult, Tuple4[ROpiskeluoikeusRow, Seq[ROpiskeluoikeusAikajaksoRow], Seq[RPäätasonSuoritusRow], Seq[ROsasuoritusRow]]] = {
    Try {
      val oo = inputRow.toOpiskeluoikeus
      val ooRow = buildROpiskeluoikeusRow(inputRow.oppijaOid, inputRow.aikaleima, oo, inputRow.data)
      val aikajaksoRows = buildROpiskeluoikeusAikajaksoRows(inputRow.oid, oo)
      val suoritusRows = oo.suoritukset.zipWithIndex.map { case (ps, i) => buildSuoritusRows(inputRow.oid, oo.getOppilaitos, ps, (inputRow.data \ "suoritukset")(i), suoritusIds.incrementAndGet) }
      (ooRow, aikajaksoRows, suoritusRows.map(_._1), suoritusRows.flatMap(_._2))
    }.toEither.left.map(t => LoadErrorResult(inputRow.oid, t.toString))
  }

  private val fieldsToExcludeFromOpiskeluoikeusJson = Set("oid", "versionumero", "aikaleima", "oppilaitos", "koulutustoimija", "suoritukset", "tyyppi", "alkamispäivä", "päättymispäivä")

  private def buildROpiskeluoikeusRow(oppijaOid: String, aikaleima: Timestamp, o: KoskeenTallennettavaOpiskeluoikeus, data: JValue) =
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
      }.getOrElse(false),
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromOpiskeluoikeusJson)
    )


  private[raportointikanta] def buildROpiskeluoikeusAikajaksoRows(opiskeluoikeusOid: String, o: KoskeenTallennettavaOpiskeluoikeus): Seq[ROpiskeluoikeusAikajaksoRow] = {
    var edellinenTila: Option[String] = None
    var edellinenTilaAlkanut: Option[Date] = None
    for ((alku, loppu) <- aikajaksot(o)) yield {
      val rivi = buildROpiskeluoikeusAikajaksoRowForOneDay(opiskeluoikeusOid, o, alku).copy(loppu = Date.valueOf(loppu))
      if (edellinenTila.isDefined && edellinenTila.get == rivi.tila) {
        rivi.copy(tilaAlkanut = edellinenTilaAlkanut.get)
      } else {
        edellinenTila = Some(rivi.tila)
        edellinenTilaAlkanut = Some(rivi.alku)
        rivi
      }
    }
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
    val lukionLisätiedot: Option[LukionOpiskeluoikeudenLisätiedot] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case l: LukionOpiskeluoikeudenLisätiedot => Some(l)
      case _ => None
    } else None
    val lukioonValmistavanLisätiedot: Option[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case l: LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot => Some(l)
      case _ => None
    } else None

    def ammatillinenAikajakso(lisätieto: AmmatillisenOpiskeluoikeudenLisätiedot => Option[List[Aikajakso]]): Byte =
      ammatillisenLisätiedot.flatMap(lisätieto).flatMap(_.find(_.contains(päivä))).size.toByte

    val oppisopimus = oppisopimusAikajaksot(o)

    ROpiskeluoikeusAikajaksoRow(
      opiskeluoikeusOid = opiskeluoikeusOid,
      alku = Date.valueOf(päivä),
      loppu = Date.valueOf(päivä), // korvataan oikealla päivällä ylempänä
      tila = jakso.tila.koodiarvo,
      tilaAlkanut = Date.valueOf(päivä), // korvataan oikealla päivällä ylempänä
      opiskeluoikeusPäättynyt = jakso.opiskeluoikeusPäättynyt,
      opintojenRahoitus = jakso match {
        case k: KoskiOpiskeluoikeusjakso => k.opintojenRahoitus.map(_.koodiarvo)
        case _ => None
      },
      majoitus = ammatillinenAikajakso(_.majoitus),
      sisäoppilaitosmainenMajoitus = (
        ammatillinenAikajakso(_.sisäoppilaitosmainenMajoitus) +
        aikuistenPerusopetuksenLisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus).flatMap(_.find(_.contains(päivä))).size +
        perusopetuksenLisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus).flatMap(_.find(_.contains(päivä))).size +
        lukionLisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus).flatMap(_.find(_.contains(päivä))).size +
        lukioonValmistavanLisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus).flatMap(_.find(_.contains(päivä))).size
      ).toByte,
      vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = ammatillinenAikajakso(_.vaativanErityisenTuenYhteydessäJärjestettäväMajoitus),
      erityinenTuki = ammatillinenAikajakso(_.erityinenTuki),
      vaativanErityisenTuenErityinenTehtävä = ammatillinenAikajakso(_.vaativanErityisenTuenErityinenTehtävä),
      hojks = ammatillisenLisätiedot.flatMap(_.hojks).find(_.contains(päivä)).size.toByte,
      vaikeastiVammainen = (
        ammatillinenAikajakso(_.vaikeastiVammainen) +
        aikuistenPerusopetuksenLisätiedot.flatMap(_.vaikeastiVammainen).flatMap(_.find(_.contains(päivä))).size +
        perusopetuksenLisätiedot.flatMap(_.vaikeastiVammainen).flatMap(_.find(_.contains(päivä))).size
      ).toByte,
      vammainenJaAvustaja = ammatillinenAikajakso(_.vammainenJaAvustaja),
      osaAikaisuus = ammatillisenLisätiedot.flatMap(_.osaAikaisuusjaksot).flatMap(_.find(_.contains(päivä))).map(_.osaAikaisuus).getOrElse(100).toByte,
      opiskeluvalmiuksiaTukevatOpinnot = ammatillisenLisätiedot.flatMap(_.opiskeluvalmiuksiaTukevatOpinnot).flatMap(_.find(_.contains(päivä))).size.toByte,
      vankilaopetuksessa = ammatillinenAikajakso(_.vankilaopetuksessa),
      oppisopimusJossainPäätasonSuorituksessa = oppisopimus.find(_.contains(päivä)).size.toByte
    )
    // Note: When adding something here, remember to update aikajaksojenAlkupäivät (below), too
  }

  val IndefiniteFuture = LocalDate.of(9999, 12, 31) // no special meaning, but must be after any possible real alkamis/päättymispäivä

  private def aikajaksot(o: KoskeenTallennettavaOpiskeluoikeus): Seq[(LocalDate, LocalDate)] = {
    val alkupäivät: Seq[LocalDate] = mahdollisetAikajaksojenAlkupäivät(o)
    val alkamispäivä: LocalDate = o.tila.opiskeluoikeusjaksot.headOption.map(_.alku).getOrElse(throw new RuntimeException(s"Alkamispäivä puuttuu ${o.oid}"))
    val päättymispäivä: LocalDate = o.tila.opiskeluoikeusjaksot.lastOption.filter(_.opiskeluoikeusPäättynyt).map(_.alku).getOrElse(IndefiniteFuture)
    val rajatutAlkupäivät = alkupäivät
      .filterNot(_.isBefore(alkamispäivä))
      .filterNot(_.isAfter(päättymispäivä))
    if (rajatutAlkupäivät.isEmpty) {
      // Can happen only if alkamispäivä or päättymispäivä are totally bogus (e.g. in year 10000)
      throw new RuntimeException(s"Virheelliset alkamis-/päättymispäivät: ${o.oid} $alkamispäivä $päättymispäivä")
    }
    rajatutAlkupäivät.zip(rajatutAlkupäivät.tail.map(_.minusDays(1)) :+ päättymispäivä)
  }

  private def mahdollisetAikajaksojenAlkupäivät(o: KoskeenTallennettavaOpiskeluoikeus): Seq[LocalDate] = {
    // logiikka: uusi r_opiskeluoikeus_aikajakso-rivi pitää aloittaa, jos ko. päivänä alkaa joku jakso (erityinen tuki tms),
    // tai jos edellisenä päivänä on loppunut joku jakso.
    val lisätiedotAikajaksot: Seq[Aikajakso] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case aol: AmmatillisenOpiskeluoikeudenLisätiedot =>
        Seq(
          aol.majoitus,
          aol.sisäoppilaitosmainenMajoitus,
          aol.vaativanErityisenTuenYhteydessäJärjestettäväMajoitus,
          aol.erityinenTuki,
          aol.vaativanErityisenTuenErityinenTehtävä,
          aol.vaikeastiVammainen,
          aol.vammainenJaAvustaja,
          aol.vankilaopetuksessa
        ).flatMap(_.getOrElse(List.empty)) ++
        aol.opiskeluvalmiuksiaTukevatOpinnot.getOrElse(Seq.empty).map(j => Aikajakso(j.alku, Some(j.loppu))) ++
        aol.osaAikaisuusjaksot.getOrElse(Seq.empty).map(j => Aikajakso(j.alku, j.loppu)) ++
        aol.hojks.toList.map(h => Aikajakso(h.alku.getOrElse(o.alkamispäivä.getOrElse(throw new RuntimeException(s"Alkamispäivä puuttuu ${o.oid}"))), h.loppu))
      case apol: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot =>
        Seq(
          apol.sisäoppilaitosmainenMajoitus,
          apol.vaikeastiVammainen
        ).flatMap(_.getOrElse(List.empty))
      case pol: PerusopetuksenOpiskeluoikeudenLisätiedot =>
        Seq(
          pol.sisäoppilaitosmainenMajoitus,
          pol.vaikeastiVammainen
        ).flatMap(_.getOrElse(List.empty))
      case lol: LukionOpiskeluoikeudenLisätiedot =>
        Seq(
          lol.sisäoppilaitosmainenMajoitus
        ).flatMap(_.getOrElse(List.empty))
      case lvol: LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot =>
        Seq(
          lvol.sisäoppilaitosmainenMajoitus
        ).flatMap(_.getOrElse(List.empty))
      case _ => Seq()
    } else Seq()

    val jaksot = lisätiedotAikajaksot ++ oppisopimusAikajaksot(o)

    (o.tila.opiskeluoikeusjaksot.map(_.alku) ++
      jaksot.map(_.alku) ++
      jaksot.map(_.loppu).filter(_.nonEmpty).map(_.get.plusDays(1))
    ).sorted(DateOrdering.localDateOrdering).distinct
  }

  private val JarjestamismuotoOppisopimus = Koodistokoodiviite("20", "jarjestamismuoto")
  private val OsaamisenhankkimistapaOppisopimus = Koodistokoodiviite("oppisopimus", "osaamisenhankkimistapa")

  private def oppisopimusAikajaksot(o: KoskeenTallennettavaOpiskeluoikeus): Seq[Jakso] = {
    def convert(järjestämismuodot: Option[List[Järjestämismuotojakso]], osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]]): Seq[Jakso] = {
      järjestämismuodot.getOrElse(List.empty).filter(_.järjestämismuoto.tunniste == JarjestamismuotoOppisopimus) ++
      osaamisenHankkimistavat.getOrElse(List.empty).filter(_.osaamisenHankkimistapa.tunniste == OsaamisenhankkimistapaOppisopimus)
    }
    o.suoritukset.flatMap {
      case s: NäyttötutkintoonValmistavanKoulutuksenSuoritus => convert(s.järjestämismuodot, s.osaamisenHankkimistavat)
      case s: AmmatillisenTutkinnonSuoritus => convert(s.järjestämismuodot, s.osaamisenHankkimistavat)
      case s: AmmatillisenTutkinnonOsittainenSuoritus => convert(s.järjestämismuodot, s.osaamisenHankkimistavat)
      case _ => Seq.empty
    }
  }

  private val fieldsToExcludeFromPäätasonSuoritusJson = Set("osasuoritukset", "tyyppi", "toimipiste", "koulutustyyppi")
  private val fieldsToExcludeFromOsasuoritusJson = Set("osasuoritukset", "tyyppi")

  private def buildSuoritusRows(opiskeluoikeusOid: String, oppilaitos: OrganisaatioWithOid, ps: PäätasonSuoritus, data: JValue, idGenerator: => Long) = {
    val päätasonSuoritusId: Long = idGenerator
    val toimipiste = (ps match {
      case stp: MahdollisestiToimipisteellinen => stp.toimipiste
      case _ => None
    }).getOrElse(oppilaitos)
    val päätaso = RPäätasonSuoritusRow(
      päätasonSuoritusId = päätasonSuoritusId,
      opiskeluoikeusOid = opiskeluoikeusOid,
      suorituksenTyyppi = ps.tyyppi.koodiarvo,
      koulutusmoduuliKoodisto = convertKoodisto(ps.koulutusmoduuli.tunniste),
      koulutusmoduuliKoodiarvo = ps.koulutusmoduuli.tunniste.koodiarvo,
      koulutusmoduuliKoulutustyyppi = ps.koulutusmoduuli match {
        case k: Koulutus => k.koulutustyyppi.map(_.koodiarvo)
        case _ => None
      },
      koulutusmoduuliLaajuusArvo = ps.koulutusmoduuli.laajuus.map(_.arvo),
      koulutusmoduuliLaajuusYksikkö = ps.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo),
      vahvistusPäivä = ps.vahvistus.map(v => Date.valueOf(v.päivä)),
      arviointiArvosanaKoodiarvo = ps.viimeisinArviointi.map(_.arvosana.koodiarvo),
      arviointiArvosanaKoodisto = ps.viimeisinArviointi.flatMap(a => convertKoodisto(a.arvosana)),
      arviointiHyväksytty = ps.viimeisinArviointi.map(_.hyväksytty),
      arviointiPäivä = ps.viimeisinArviointi.flatMap(_.arviointipäivä).map(v => Date.valueOf(v)),
      toimipisteOid = toimipiste.oid,
      toimipisteNimi = convertLocalizedString(toimipiste.nimi),
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromPäätasonSuoritusJson)
    )
    val osat = ps.osasuoritukset.getOrElse(List.empty).zipWithIndex.flatMap {
      case (os, i) => buildROsasuoritusRow(päätasonSuoritusId, None, opiskeluoikeusOid, os, (data \ "osasuoritukset")(i), idGenerator)
    }
    (päätaso, osat)
  }

  private def buildROsasuoritusRow(päätasonSuoritusId: Long, ylempiOsasuoritusId: Option[Long], opiskeluoikeusOid: String, os: Suoritus, data: JValue, idGenerator: => Long): Seq[ROsasuoritusRow] = {
    val osasuoritusId: Long = idGenerator
    ROsasuoritusRow(
      osasuoritusId = osasuoritusId,
      ylempiOsasuoritusId = ylempiOsasuoritusId,
      päätasonSuoritusId = päätasonSuoritusId,
      opiskeluoikeusOid = opiskeluoikeusOid,
      suorituksenTyyppi = os.tyyppi.koodiarvo,
      koulutusmoduuliKoodisto = convertKoodisto(os.koulutusmoduuli.tunniste),
      koulutusmoduuliKoodiarvo = os.koulutusmoduuli.tunniste.koodiarvo,
      koulutusmoduuliLaajuusArvo = os.koulutusmoduuli.laajuus.map(_.arvo),
      koulutusmoduuliLaajuusYksikkö = os.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo),
      koulutusmoduuliPaikallinen = os.koulutusmoduuli.tunniste match {
        case k: Koodistokoodiviite => false
        case k: PaikallinenKoodi => true
      },
      koulutusmoduuliPakollinen = os.koulutusmoduuli match {
        case v: Valinnaisuus => Some(v.pakollinen)
        case _ => None
      },
      vahvistusPäivä = os.vahvistus.map(v => Date.valueOf(v.päivä)),
      arviointiArvosanaKoodiarvo = os.viimeisinArviointi.map(_.arvosana.koodiarvo),
      arviointiArvosanaKoodisto = os.viimeisinArviointi.flatMap(a => convertKoodisto(a.arvosana)),
      arviointiHyväksytty = os.viimeisinArviointi.map(_.hyväksytty),
      arviointiPäivä = os.viimeisinArviointi.flatMap(_.arviointipäivä).map(v => Date.valueOf(v)),
      näytönArviointiPäivä = os match {
        case atos: AmmatillisenTutkinnonOsanSuoritus => atos.näyttö.flatMap(_.arviointi).map(v => Date.valueOf(v.päivä))
        case vkos: ValmaKoulutuksenOsanSuoritus => vkos.näyttö.flatMap(_.arviointi).map(v => Date.valueOf(v.päivä))
        case tkos: TelmaKoulutuksenOsanSuoritus => tkos.näyttö.flatMap(_.arviointi).map(v => Date.valueOf(v.päivä))
        case _ => None
      },
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
