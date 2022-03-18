package fi.oph.koski.raportointikanta

import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.json.JsonManipulation
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryService
import fi.oph.koski.raportointikanta.LoaderUtils.{convertKoodisto, convertLocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.validation.MaksuttomuusValidation
import org.json4s.JValue
import rx.lang.scala.{Observable, Subscriber}

import java.sql.{Date, Timestamp}
import java.time.temporal.ChronoField
import java.util.concurrent.atomic.AtomicLong
import scala.concurrent.duration.DurationInt
import scala.util.Try

object OpiskeluoikeusLoader extends Logging {
  val DefaultBatchSize = 500

  private val statusName = "opiskeluoikeudet"
  private val mitätöidytStatusName = "mitätöidyt_opiskeluoikeudet"

  def loadOpiskeluoikeudet(
    opiskeluoikeusQueryRepository: OpiskeluoikeusQueryService,
    db: RaportointiDatabase,
    update: Option[RaportointiDatabaseUpdate] = None,
    batchSize: Int = DefaultBatchSize,
    onAfterPage: (Int, Seq[OpiskeluoikeusRow]) => Unit = (_, _) => ()
  ): Observable[LoadResult] = {
    db.setStatusLoadStarted(statusName)
    db.setStatusLoadStarted(mitätöidytStatusName)
    update.foreach(u => db.cloneUpdateableTables(u.sourceDb))

    var loopCount = 0
    val result = opiskeluoikeusQueryRepository.mapOpiskeluoikeudetSivuittainWithoutAccessCheck(batchSize, update.map(_.since)) { batch =>
      if (batch.nonEmpty) {
        val result = if (update.isDefined) updateBatch(db, batch) else loadBatch(db, batch)
        onAfterPage(loopCount, batch)
        loopCount = loopCount + 1
        result
      } else {
        // Last batch processed; finalize
        createIndexes(db)
        db.setStatusLoadCompleted(statusName)
        db.setStatusLoadCompleted(mitätöidytStatusName)
        Seq(LoadCompleted())
      }
    }
    result.doOnEach(progressLogger)
  }

  private def loadBatch(db: RaportointiDatabase, batch: Seq[OpiskeluoikeusRow]): Seq[LoadResult] = {
    val (mitätöidytOot, olemassaolevatOot) = batch.partition(_.mitätöity)

    val resultOlemassaolevatOot = loadBatchOlemassaolevatOpiskeluoikeudet(db, olemassaolevatOot)
    val resultMitätöidyt = loadBatchMitätöidytOpiskeluoikeudet(db, mitätöidytOot)

    resultOlemassaolevatOot ++ resultMitätöidyt
  }

  private def updateBatch(db: RaportointiDatabase, batch: Seq[OpiskeluoikeusRow]): Seq[LoadResult] = {
    val (mitätöidytOot, olemassaolevatOot) = batch.partition(_.mitätöity)

    val resultOlemassaolevatOot = updateBatchOlemassaolevatOpiskeluoikeudet(db, olemassaolevatOot)
    val resultMitätöidyt = updateBatchMitätöidytOpiskeluoikeudet(db, mitätöidytOot)

    resultOlemassaolevatOot ++ resultMitätöidyt
  }

  private def loadBatchOlemassaolevatOpiskeluoikeudet(db: RaportointiDatabase, oot: Seq[OpiskeluoikeusRow]) = {
    val loadBatchStartTime = System.nanoTime()

    val (errors, outputRows) = oot.par
      .map(row => buildRow(row))
      .seq
      .partition(_.isLeft)

    db.loadOpiskeluoikeudet(outputRows.map(_.right.get.rOpiskeluoikeusRow))
    db.loadOrganisaatioHistoria(outputRows.flatMap(_.right.get.organisaatioHistoriaRows))
    val aikajaksoRows = outputRows.flatMap(_.right.get.rOpiskeluoikeusAikajaksoRows)
    val esiopetusOpiskeluoikeusAikajaksoRows = outputRows.flatMap(_.right.get.esiopetusOpiskeluoikeusAikajaksoRows)
    val päätasonSuoritusRows = outputRows.flatMap(_.right.get.rPäätasonSuoritusRows)
    val osasuoritusRows = outputRows.flatMap(_.right.get.rOsasuoritusRows)
    val muuAmmatillinenRaportointiRows = outputRows.flatMap(_.right.get.muuAmmatillinenOsasuoritusRaportointiRows)
    val topksAmmatillinenRaportointiRows = outputRows.flatMap(_.right.get.topksAmmatillinenRaportointiRows)
    db.loadOpiskeluoikeusAikajaksot(aikajaksoRows)
    db.loadEsiopetusOpiskeluoikeusAikajaksot(esiopetusOpiskeluoikeusAikajaksoRows)
    db.loadPäätasonSuoritukset(päätasonSuoritusRows)
    db.loadOsasuoritukset(osasuoritusRows)
    db.loadMuuAmmatillinenRaportointi(muuAmmatillinenRaportointiRows)
    db.loadTOPKSAmmatillinenRaportointi(topksAmmatillinenRaportointiRows)
    db.setLastUpdate(statusName)
    db.updateStatusCount(statusName, outputRows.size)
    val result = errors.map(_.left.get) :+ LoadProgressResult(outputRows.size, päätasonSuoritusRows.size + osasuoritusRows.size)

    val loadBatchDuration: Long = (System.nanoTime() - loadBatchStartTime) / 1000000
    val toOpiskeluoikeusUnsafeDuration: Long = outputRows.map(_.right.get.toOpiskeluoikeusUnsafeDuration).sum / 1000000
    logger.info(s"Batchin käsittely kesti ${loadBatchDuration} ms, jossa toOpiskeluOikeusUnsafe ${toOpiskeluoikeusUnsafeDuration} ms.")
    result
  }

  private def updateBatchOlemassaolevatOpiskeluoikeudet(db: RaportointiDatabase, oot: Seq[OpiskeluoikeusRow]) = {
    val loadBatchStartTime = System.nanoTime()

    val (errors, outputRows) = oot.par
      .map(row => buildRow(row))
      .seq
      .partition(_.isLeft)

    db.updateOpiskeluoikeudet(outputRows.map(_.right.get.rOpiskeluoikeusRow))
    db.updateOrganisaatioHistoria(outputRows.flatMap(_.right.get.organisaatioHistoriaRows))

    val aikajaksoRows = outputRows.flatMap(_.right.get.rOpiskeluoikeusAikajaksoRows)
    db.updateOpiskeluoikeusAikajaksot(aikajaksoRows)

    val esiopetusOpiskeluoikeusAikajaksoRows = outputRows.flatMap(_.right.get.esiopetusOpiskeluoikeusAikajaksoRows)
    db.updateEsiopetusOpiskeluoikeusAikajaksot(esiopetusOpiskeluoikeusAikajaksoRows)

    val päätasonSuoritusRows = outputRows.flatMap(_.right.get.rPäätasonSuoritusRows)
    db.updatePäätasonSuoritukset(päätasonSuoritusRows)

    val osasuoritusRows = outputRows.flatMap(_.right.get.rOsasuoritusRows)
    db.updateOsasuoritukset(osasuoritusRows)

    val muuAmmatillinenRaportointiRows = outputRows.flatMap(_.right.get.muuAmmatillinenOsasuoritusRaportointiRows)
    db.updateMuuAmmatillinenRaportointi(muuAmmatillinenRaportointiRows)

    val topksAmmatillinenRaportointiRows = outputRows.flatMap(_.right.get.topksAmmatillinenRaportointiRows)
    db.updateTOPKSAmmatillinenRaportointi(topksAmmatillinenRaportointiRows)

    db.setLastUpdate(statusName)
    db.updateStatusCount(statusName, outputRows.size)
    val result = errors.map(_.left.get) :+ LoadProgressResult(outputRows.size, päätasonSuoritusRows.size + osasuoritusRows.size)

    val loadBatchDuration: Long = (System.nanoTime() - loadBatchStartTime) / 1000000
    val toOpiskeluoikeusUnsafeDuration: Long = outputRows.map(_.right.get.toOpiskeluoikeusUnsafeDuration).sum / 1000000
    logger.info(s"Batchin käsittely kesti ${loadBatchDuration} ms, jossa toOpiskeluOikeusUnsafe ${toOpiskeluoikeusUnsafeDuration} ms.")
    result
  }

  private def loadBatchMitätöidytOpiskeluoikeudet(db: RaportointiDatabase, oot: Seq[OpiskeluoikeusRow]) = {
    val (errors, outputRows) = oot.par.filterNot(_.poistettu).map(buildRowMitätöity).seq.partition(_.isLeft)
    db.loadMitätöidytOpiskeluoikeudet(outputRows.map(_.right.get))
    db.updateStatusCount(mitätöidytStatusName, outputRows.size)
    errors.map(_.left.get)
  }

  private def updateBatchMitätöidytOpiskeluoikeudet(db: RaportointiDatabase, oot: Seq[OpiskeluoikeusRow]) = {
    val (errors, outputRows) = oot.par.filterNot(_.poistettu).map(buildRowMitätöity).seq.partition(_.isLeft)
    db.updateMitätöidytOpiskeluoikeudet(outputRows.map(_.right.get))
    db.updateStatusCount(mitätöidytStatusName, outputRows.size)
    errors.map(_.left.get)
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

  type SuoritusRows = List[(
    RPäätasonSuoritusRow,
      List[ROsasuoritusRow],
      List[MuuAmmatillinenOsasuoritusRaportointiRow],
      List[TOPKSAmmatillinenRaportointiRow]
    )]

  type AikajaksoRows = (Seq[ROpiskeluoikeusAikajaksoRow], Seq[EsiopetusOpiskeluoikeusAikajaksoRow])

  private def buildRow(inputRow: OpiskeluoikeusRow): Either[LoadErrorResult, OutputRows] = {
    Try {
      val toOpiskeluoikeusUnsafeStartTime = System.nanoTime()
      val oo = inputRow.toOpiskeluoikeusUnsafe(KoskiSpecificSession.systemUser)
      val toOpiskeluoikeusUnsafeDuration = System.nanoTime() - toOpiskeluoikeusUnsafeStartTime
      val ooRow = buildROpiskeluoikeusRow(inputRow.oppijaOid, inputRow.aikaleima, oo, inputRow.data)
      val aikajaksoRows: AikajaksoRows = buildAikajaksoRows(inputRow.oid, oo)
      val suoritusRows: SuoritusRows = oo.suoritukset.zipWithIndex.map {
        case (ps, i) => buildSuoritusRows(
          inputRow.oid,
          inputRow.sisältäväOpiskeluoikeusOid,
          oo.getOppilaitos,
          ps,
          (inputRow.data \ "suoritukset") (i),
          suoritusIds.incrementAndGet
        )
      }
      OutputRows(
        rOpiskeluoikeusRow = ooRow,
        organisaatioHistoriaRows = OrganisaatioHistoriaRowBuilder.buildOrganisaatioHistoriaRows(oo),
        rOpiskeluoikeusAikajaksoRows = aikajaksoRows._1,
        esiopetusOpiskeluoikeusAikajaksoRows = aikajaksoRows._2,
        rPäätasonSuoritusRows = suoritusRows.map(_._1),
        rOsasuoritusRows = suoritusRows.flatMap(_._2),
        muuAmmatillinenOsasuoritusRaportointiRows = suoritusRows.flatMap(_._3),
        topksAmmatillinenRaportointiRows = suoritusRows.flatMap(_._4),
        toOpiskeluoikeusUnsafeDuration = toOpiskeluoikeusUnsafeDuration
      )
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
      oppilaitosNimi = convertLocalizedString(o.oppilaitos.flatMap(_.nimi), "fi"),
      oppilaitosNimiSv = convertLocalizedString(o.oppilaitos.flatMap(_.nimi), "sv"),
      oppilaitosKotipaikka = o.oppilaitos.flatMap(_.kotipaikka).map(_.koodiarvo.stripPrefix("kunta_")),
      oppilaitosnumero = o.oppilaitos.flatMap(_.oppilaitosnumero).map(_.koodiarvo),
      koulutustoimijaOid = o.koulutustoimija.map(_.oid).getOrElse(""),
      koulutustoimijaNimi = convertLocalizedString(o.koulutustoimija.flatMap(_.nimi), "fi"),
      koulutustoimijaNimiSv = convertLocalizedString(o.koulutustoimija.flatMap(_.nimi), "sv"),
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
      lähdejärjestelmäKoodiarvo = o.lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmäId = o.lähdejärjestelmänId.flatMap(_.id),
      luokka = o.luokka,
      oppivelvollisuudenSuorittamiseenKelpaava = oppivelvollisuudenSuorittamiseenKelpaava(o),
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromOpiskeluoikeusJson)
    )

  private def oppivelvollisuudenSuorittamiseenKelpaava(o: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    o.tyyppi.koodiarvo match {
      case "perusopetus" => true
      case "internationalschool" => true
      case "esiopetus" => true
      case "perusopetukseenvalmistavaopetus" => true
      case _ => MaksuttomuusValidation.oppivelvollisuudenSuorittamiseenKelpaavaMuuKuinPeruskoulunOpiskeluoikeus(o)
    }

  private def buildAikajaksoRows(opiskeluoikeusOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): AikajaksoRows = {
    val opiskeluoikeusAikajaksot = AikajaksoRowBuilder.buildROpiskeluoikeusAikajaksoRows(opiskeluoikeusOid, opiskeluoikeus)
    val esiopetusOpiskeluoikeusAikajaksot = opiskeluoikeus match {
      case esiopetus: EsiopetuksenOpiskeluoikeus => AikajaksoRowBuilder.buildEsiopetusOpiskeluoikeusAikajaksoRows(opiskeluoikeusOid, esiopetus)
      case _ => Nil
    }

    (opiskeluoikeusAikajaksot, esiopetusOpiskeluoikeusAikajaksot)
  }

  private val fieldsToExcludeFromPäätasonSuoritusJson = Set("osasuoritukset", "tyyppi", "toimipiste", "koulutustyyppi")
  private val fieldsToExcludeFromOsasuoritusJson = Set("osasuoritukset", "tyyppi")

  private[raportointikanta] def buildSuoritusRows(opiskeluoikeusOid: String, sisältyyOpiskeluoikeuteenOid: Option[String], oppilaitos: OrganisaatioWithOid, ps: PäätasonSuoritus, data: JValue, idGenerator: => Long) = {
    val päätasonSuoritusId: Long = idGenerator
    val päätaso = buildRPäätasonSuoritusRow(opiskeluoikeusOid, sisältyyOpiskeluoikeuteenOid, oppilaitos, ps, data, päätasonSuoritusId)
    val osat = ps.osasuoritukset.getOrElse(List.empty).zipWithIndex.flatMap {
      case (os, i) => buildROsasuoritusRow(päätasonSuoritusId, None, opiskeluoikeusOid, sisältyyOpiskeluoikeuteenOid, os, (data \ "osasuoritukset")(i), idGenerator)
    }
    val muuAmmatillinenRaportointi = ps match {
      case s: MuunAmmatillisenKoulutuksenSuoritus => s.rekursiivisetOsasuoritukset.map(MuuAmmatillinenRaporttiRowBuilder.build(opiskeluoikeusOid, päätasonSuoritusId, _))
      case _ => Nil
    }
    val topksAmmatillinenRaportointi = ps match {
      case s: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus => s.rekursiivisetOsasuoritukset.map(TOPKSAmmatillinenRaporttiRowBuilder.build(opiskeluoikeusOid, päätasonSuoritusId, _))
      case _ => Nil
    }
    (päätaso, osat, muuAmmatillinenRaportointi, topksAmmatillinenRaportointi)
  }

  private def buildRPäätasonSuoritusRow(opiskeluoikeusOid: String, sisältyyOpiskeluoikeuteenOid: Option[String], oppilaitos: OrganisaatioWithOid, ps: PäätasonSuoritus, data: JValue, päätasonSuoritusId: Long) = {
    val toimipiste = ps match {
      case tp: Toimipisteellinen => tp.toimipiste
      case stp: MahdollisestiToimipisteellinen if stp.toimipiste.nonEmpty => stp.toimipiste.get
      case _ => oppilaitos
    }
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
      koulutusmoduuliLaajuusArvo = ps.koulutusmoduuli.getLaajuus.map(_.arvo),
      koulutusmoduuliLaajuusYksikkö = ps.koulutusmoduuli.getLaajuus.map(_.yksikkö.koodiarvo),
      koulutusmoduuliNimi = ps.koulutusmoduuli.tunniste.getNimi.map(_.get("fi")),
      suorituskieliKoodiarvo = ps match {
        case s: Suorituskielellinen => Some(s.suorituskieli.koodiarvo)
        case m: MahdollisestiSuorituskielellinen => m.suorituskieli.map(_.koodiarvo)
        case _ => None
      },
      oppimääräKoodiarvo = ps match {
        case o: Oppimäärällinen => Some(o.oppimäärä.koodiarvo)
        case _ => None
      },
      alkamispäivä = ps.alkamispäivä.map(v => Date.valueOf(v)),
      vahvistusPäivä = ps.vahvistus.map(v => Date.valueOf(v.päivä)),
      arviointiArvosanaKoodiarvo = ps.viimeisinArviointi.map(_.arvosana.koodiarvo),
      arviointiArvosanaKoodisto = ps.viimeisinArviointi.flatMap(a => convertKoodisto(a.arvosana)),
      arviointiHyväksytty = ps.viimeisinArviointi.map(_.hyväksytty),
      arviointiPäivä = ps.viimeisinArviointi.flatMap(_.arviointipäivä).map(v => Date.valueOf(v)),
      toimipisteOid = toimipiste.oid,
      toimipisteNimi = convertLocalizedString(toimipiste.nimi, "fi"),
      toimipisteNimiSv = convertLocalizedString(toimipiste.nimi, "sv"),
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromPäätasonSuoritusJson),
      sisältyyOpiskeluoikeuteenOid = sisältyyOpiskeluoikeuteenOid
    )
    päätaso
  }

  private def buildROsasuoritusRow(
    päätasonSuoritusId: Long,
    ylempiOsasuoritusId: Option[Long],
    opiskeluoikeusOid: String,
    sisältyyOpiskeluoikeuteenOid: Option[String],
    os: Suoritus,
    data: JValue,
    idGenerator: => Long
  ): Seq[ROsasuoritusRow] = {
    val osasuoritusId: Long = idGenerator
    ROsasuoritusRow(
      osasuoritusId = osasuoritusId,
      ylempiOsasuoritusId = ylempiOsasuoritusId,
      päätasonSuoritusId = päätasonSuoritusId,
      opiskeluoikeusOid = opiskeluoikeusOid,
      suorituksenTyyppi = os.tyyppi.koodiarvo,
      koulutusmoduuliKoodisto = convertKoodisto(os.koulutusmoduuli.tunniste),
      koulutusmoduuliKoodiarvo = os.koulutusmoduuli.tunniste.koodiarvo,
      koulutusmoduuliLaajuusArvo = os.koulutusmoduuli.getLaajuus.map(_.arvo),
      koulutusmoduuliLaajuusYksikkö = os.koulutusmoduuli.getLaajuus.map(_.yksikkö.koodiarvo),
      koulutusmoduuliPaikallinen = os.koulutusmoduuli.tunniste match {
        case k: Koodistokoodiviite => false
        case k: PaikallinenKoodi => true
      },
      koulutusmoduuliPakollinen = os.koulutusmoduuli match {
        case v: Valinnaisuus => Some(v.pakollinen)
        case _ => None
      },
      koulutusmoduuliNimi = os.koulutusmoduuli.tunniste.getNimi.map(_.get("fi")),
      koulutusmoduuliOppimääräNimi = os.koulutusmoduuli match {
        case k: Oppimäärä => k.oppimäärä.nimi.map(_.get("fi"))
        case k: Uskonto => k.uskonnonOppimäärä.flatMap(_.nimi.map(_.get("fi")))
        case _ => None
      },
      koulutusmoduuliKieliaineNimi = os.koulutusmoduuli match {
        case k: Kieliaine => k.kieli.nimi.map(_.get("fi"))
        case _ => None
      },
      koulutusmoduuliKurssinTyyppi = os.koulutusmoduuli match {
        case l: LukionKurssi2015 => Some(l.kurssinTyyppi.koodiarvo)
        case l: LukionModuuli2019 => l.pakollinen match {
          case true => Some("pakollinen")
          case false => None
        }
        case _ => None
      },
      vahvistusPäivä = os.vahvistus.map(v => Date.valueOf(v.päivä)),
      arviointiArvosanaKoodiarvo = os.parasArviointi.map(_.arvosana.koodiarvo),
      arviointiArvosanaKoodisto = os.parasArviointi.flatMap(a => convertKoodisto(a.arvosana)),
      arviointiHyväksytty = os.parasArviointi.map(_.hyväksytty),
      arviointiPäivä = os.parasArviointi.flatMap(_.arviointipäivä).map(v => Date.valueOf(v)),
      ensimmäinenArviointiPäivä = os.arviointi.toList.flatten.map(_.arviointipäivä).flatten.map(v => Date.valueOf(v)).reduceOption((a, b) => {
        if (a.toLocalDate.isBefore(b.toLocalDate)) {
          a
        } else {
          b
        }
      }),
      korotettuEriVuonna = (os.ensimmäinenArviointiPäivä, os.parasArviointiPäivä) match {
        case (Some(eka), Some(paras)) => {
          if (eka.get(ChronoField.YEAR) != paras.get(ChronoField.YEAR)) {
            true
          } else {
            false
          }
        }
        case _ => false
      },
      näytönArviointiPäivä = os match {
        case atos: AmmatillisenTutkinnonOsanSuoritus => atos.näyttö.flatMap(_.arviointi).map(v => Date.valueOf(v.päivä))
        case vkos: ValmaKoulutuksenOsanSuoritus => vkos.näyttö.flatMap(_.arviointi).map(v => Date.valueOf(v.päivä))
        case tkos: TelmaKoulutuksenOsanSuoritus => tkos.näyttö.flatMap(_.arviointi).map(v => Date.valueOf(v.päivä))
        case _ => None
      },
      tunnustettu = os match {
        case m: MahdollisestiTunnustettu => m.tunnustettu.isDefined
        case _ => false
      },
      tunnustettuRahoituksenPiirissä = os match {
        case m: MahdollisestiTunnustettu => m.tunnustettu.exists(_.rahoituksenPiirissä)
        case _ => false
      },
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromOsasuoritusJson),
      sisältyyOpiskeluoikeuteenOid = sisältyyOpiskeluoikeuteenOid
    ) +: os.osasuoritukset.getOrElse(List.empty).zipWithIndex.flatMap {
      case (os2, i) => buildROsasuoritusRow(
        päätasonSuoritusId,
        Some(osasuoritusId),
        opiskeluoikeusOid,
        sisältyyOpiskeluoikeuteenOid,
        os2,
        (data \ "osasuoritukset")(i),
        idGenerator
      )
    }
  }

  private def buildRowMitätöity(raw: OpiskeluoikeusRow): Either[LoadErrorResult, RMitätöityOpiskeluoikeusRow] = {
    for {
      oo <- raw.toOpiskeluoikeus(KoskiSpecificSession.systemUser).left.map(e => LoadErrorResult(raw.oid, "[mitätöity] " + e.toString()))
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

sealed trait LoadResult
case class LoadErrorResult(oid: String, error: String) extends LoadResult
case class LoadProgressResult(opiskeluoikeusCount: Int, suoritusCount: Int) extends LoadResult
case class LoadCompleted(done: Boolean = true) extends LoadResult

case class OutputRows(
  rOpiskeluoikeusRow: ROpiskeluoikeusRow,
  organisaatioHistoriaRows: Seq[ROrganisaatioHistoriaRow],
  rOpiskeluoikeusAikajaksoRows: Seq[ROpiskeluoikeusAikajaksoRow],
  esiopetusOpiskeluoikeusAikajaksoRows: Seq[EsiopetusOpiskeluoikeusAikajaksoRow],
  rPäätasonSuoritusRows: Seq[RPäätasonSuoritusRow],
  rOsasuoritusRows: Seq[ROsasuoritusRow],
  muuAmmatillinenOsasuoritusRaportointiRows: Seq[MuuAmmatillinenOsasuoritusRaportointiRow],
  topksAmmatillinenRaportointiRows: Seq[TOPKSAmmatillinenRaportointiRow],
  toOpiskeluoikeusUnsafeDuration: Long = 0
)

case class RaportointiDatabaseUpdate(
  sourceDb: RaportointiDatabase,
  since: Timestamp,
)
