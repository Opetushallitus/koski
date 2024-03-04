package fi.oph.koski.raportointikanta

import fi.oph.koski.db.{KoskiOpiskeluoikeusRow, OpiskeluoikeusRow, PoistettuOpiskeluoikeusRow, YtrOpiskeluoikeusRow}
import fi.oph.koski.json.JsonManipulation
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.raportointikanta.LoaderUtils.{convertKoodisto, convertLocalizedString}
import fi.oph.koski.schema._
import fi.oph.koski.validation.MaksuttomuusValidation
import org.json4s.JValue

import java.sql.{Date, Timestamp}
import java.time.temporal.ChronoField
import java.util.concurrent.atomic.AtomicLong
import scala.util.Try

object OpiskeluoikeusLoaderRowBuilder extends Logging {

  // mitätöityError sisältö käytössä AWS hälytyksessä
  private val mitätöityError = "[mitatoity]"

  val suoritusIds = new AtomicLong()

  type KoskiSuoritusRows = List[(
    RPäätasonSuoritusRow,
      List[ROsasuoritusRow],
      List[MuuAmmatillinenOsasuoritusRaportointiRow],
      List[TOPKSAmmatillinenRaportointiRow]
    )]

  type AikajaksoRows = (Seq[ROpiskeluoikeusAikajaksoRow], Seq[EsiopetusOpiskeluoikeusAikajaksoRow])

  type TutkintokokonaisuusId = Int
  case class YtrTutkintokokonaisuusRows(
    tutkintokokonaisuudenSuoritusRow: RYtrTutkintokokonaisuudenSuoritusRow,
    tutkintokerranSuoritukset: Map[String, RYtrTutkintokerranSuoritusRow],
    sisältyvätKokeet: List[YlioppilastutkinnonSisältyväKoe]
  )

  def buildKoskiRow(inputRow: OpiskeluoikeusRow): Either[LoadErrorResult, KoskiOutputRows] = {
    Try {
      val toOpiskeluoikeusUnsafeStartTime = System.nanoTime()
      val oo = inputRow.toOpiskeluoikeusUnsafe(KoskiSpecificSession.systemUser)
      val toOpiskeluoikeusUnsafeDuration = System.nanoTime() - toOpiskeluoikeusUnsafeStartTime
      val ooRow = buildROpiskeluoikeusRow(inputRow.oppijaOid, inputRow.aikaleima, oo, inputRow.data)

      val aikajaksoRows: AikajaksoRows = buildAikajaksoRows(inputRow.oid, oo)
      val suoritusRows: KoskiSuoritusRows = oo.suoritukset.zipWithIndex.map {
        case (ps, i) => OpiskeluoikeusLoaderRowBuilder.buildKoskiSuoritusRows(
          inputRow.oid,
          inputRow.sisältäväOpiskeluoikeusOid,
          oo.getOppilaitos,
          ps,
          (inputRow.data \ "suoritukset") (i),
          suoritusIds.incrementAndGet
        )
      }
      KoskiOutputRows(
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

  type YtrSuoritusRows = List[(
    RPäätasonSuoritusRow,
      List[RYtrTutkintokokonaisuudenSuoritusRow],
      List[RYtrTutkintokerranSuoritusRow],
      List[RYtrKokeenSuoritusRow],
      List[RYtrTutkintokokonaisuudenKokeenSuoritusRow]
    )]

  private[raportointikanta]def buildYtrRow(inputRow: YtrOpiskeluoikeusRow): Either[LoadErrorResult, YtrOutputRows] = {
    Try {
      val toOpiskeluoikeusUnsafeStartTime = System.nanoTime()
      val oo = inputRow.toOpiskeluoikeusUnsafe(KoskiSpecificSession.systemUser)
      val toOpiskeluoikeusUnsafeDuration = System.nanoTime() - toOpiskeluoikeusUnsafeStartTime
      val ooRow = buildROpiskeluoikeusRow(inputRow.oppijaOid, inputRow.aikaleima, oo, inputRow.data)

      val suoritusRows: YtrSuoritusRows = oo.suoritukset.zipWithIndex.map {
        case (ps, i) => OpiskeluoikeusLoaderRowBuilder.buildYtrSuoritusRows(
          oo,
          inputRow.sisältäväOpiskeluoikeusOid,
          oo.getOppilaitos,
          ps,
          inputRow.data \ "lisätiedot",
          (inputRow.data \ "suoritukset") (i),
          suoritusIds.incrementAndGet
        )
      }
      YtrOutputRows(
        rOpiskeluoikeusRow = ooRow,
        rPäätasonSuoritusRows = suoritusRows.map(_._1),
        rTutkintokokonaisuudenSuoritusRows = suoritusRows.flatMap(_._2),
        rTutkintokerranSuoritusRows = suoritusRows.flatMap(_._3),
        rKokeenSuoritusRows = suoritusRows.flatMap(_._4),
        rTutkintokokonaisuudenKokeenSuoritusRows = suoritusRows.flatMap(_._5),
        toOpiskeluoikeusUnsafeDuration = toOpiskeluoikeusUnsafeDuration
      )
    }.toEither.left.map(t => LoadErrorResult(inputRow.oid, t.toString))
  }

  private val fieldsToExcludeFromOpiskeluoikeusJson = Set("oid", "versionumero", "aikaleima", "oppilaitos", "koulutustoimija", "suoritukset", "tyyppi", "alkamispäivä", "päättymispäivä")

  private def buildROpiskeluoikeusRow(oppijaOid: String, aikaleima: Timestamp, o: KoskeenTallennettavaOpiskeluoikeus, data: JValue) = {
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
      oppilaitosnumero = o match {
        case _: YlioppilastutkinnonOpiskeluoikeus => None
        case _ => o.oppilaitos.flatMap(_.oppilaitosnumero).map(_.koodiarvo)
      },
      koulutustoimijaOid = o.koulutustoimija.map(_.oid).getOrElse(""),
      koulutustoimijaNimi = convertLocalizedString(o.koulutustoimija.flatMap(_.nimi), "fi"),
      koulutustoimijaNimiSv = convertLocalizedString(o.koulutustoimija.flatMap(_.nimi), "sv"),
      koulutusmuoto = o.tyyppi.koodiarvo,
      alkamispäivä = o match {
        case _: YlioppilastutkinnonOpiskeluoikeus => None
        case _ => o.alkamispäivä.map(Date.valueOf)
      },
      päättymispäivä = o match {
        case _: YlioppilastutkinnonOpiskeluoikeus => None
        case _ => o.tila.opiskeluoikeusjaksot.lastOption.filter(_.opiskeluoikeusPäättynyt).map(v => Date.valueOf(v.alku))
      },
      viimeisinTila = o match {
        case _: YlioppilastutkinnonOpiskeluoikeus => None
        case _ => o.tila.opiskeluoikeusjaksot.lastOption.map(_.tila.koodiarvo)
      },
      lisätiedotHenkilöstökoulutus = o.lisätiedot.collect {
        case l: AmmatillisenOpiskeluoikeudenLisätiedot => l.henkilöstökoulutus
      }.getOrElse(false),
      lisätiedotKoulutusvienti = o.lisätiedot.collect {
        case l: AmmatillisenOpiskeluoikeudenLisätiedot => l.koulutusvienti
      }.getOrElse(false),
      tuvaJärjestämislupa = o match {
        case l: TutkintokoulutukseenValmentavanOpiskeluoikeus => Some(l.järjestämislupa.koodiarvo)
        case _ => None
      },
      lähdejärjestelmäKoodiarvo = o.lähdejärjestelmänId.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmäId = o.lähdejärjestelmänId.flatMap(_.id),
      oppivelvollisuudenSuorittamiseenKelpaava = oppivelvollisuudenSuorittamiseenKelpaava(o),
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromOpiskeluoikeusJson)
    )
  }

  private def oppivelvollisuudenSuorittamiseenKelpaava(o: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    o.tyyppi.koodiarvo match {
      case "perusopetus" => true
      case "internationalschool" => true
      case "ebtutkinto" => true
      case "europeanschoolofhelsinki"
        if o.asInstanceOf[EuropeanSchoolOfHelsinkiOpiskeluoikeus].suoritukset.exists {
          case _: OppivelvollisuudenSuorittamiseenKelpaavaESHVuosiluokanSuoritus => true
          case _ => false
        } => true
      case "esiopetus" => true
      case "perusopetukseenvalmistavaopetus" => true
      case "lukiokoulutus"
        if o.asInstanceOf[LukionOpiskeluoikeus].suoritukset.exists {
          case _: LukionOppiaineidenOppimäärienSuoritus2019 | _: LukionOppiaineenOppimääränSuoritus2015 => true
          case _ => false
        } => true
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
  private val fieldsToExcludeFromYtrTutkintokokonaisuudenSuoritusJson = Set("tutkintokerrat", "tunniste")
  private val fieldsToExcludeFromYtrTutkintokerranSuoritusJson: Set[String] = Set()
  private val fieldsToExcludeFromYtrKokeenSuoritusJson = Set("osasuoritukset", "tyyppi", "tutkintokokonaisuudenTunniste")

  private[raportointikanta] def buildKoskiSuoritusRows(
    opiskeluoikeusOid: String,
    sisältyyOpiskeluoikeuteenOid: Option[String],
    oppilaitos: OrganisaatioWithOid,
    ps: PäätasonSuoritus,
    data: JValue,
    idGenerator: => Long
  ): (
       RPäätasonSuoritusRow,
       List[ROsasuoritusRow],
       List[MuuAmmatillinenOsasuoritusRaportointiRow],
       List[TOPKSAmmatillinenRaportointiRow]
     ) =
  {
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

  private[raportointikanta] def buildYtrSuoritusRows(
    oo: YlioppilastutkinnonOpiskeluoikeus,
    sisältyyOpiskeluoikeuteenOid: Option[String],
    oppilaitos: OrganisaatioWithOid,
    ps: YlioppilastutkinnonSuoritus,
    lisätiedotData: JValue,
    psData: JValue,
    idGenerator: => Long
  ): (
       RPäätasonSuoritusRow,
       List[RYtrTutkintokokonaisuudenSuoritusRow],
       List[RYtrTutkintokerranSuoritusRow],
       List[RYtrKokeenSuoritusRow],
       List[RYtrTutkintokokonaisuudenKokeenSuoritusRow]
    ) =
  {
    val opiskeluoikeusOid = oo.oid.get

    val päätasonSuoritusId: Long = idGenerator
    val päätaso = buildRPäätasonSuoritusRow(opiskeluoikeusOid, sisältyyOpiskeluoikeuteenOid, oppilaitos, ps, psData, päätasonSuoritusId)

    val tutkintokokonaisuudet: Map[TutkintokokonaisuusId, YtrTutkintokokonaisuusRows] =
      oo.lisätiedot.flatMap(_.tutkintokokonaisuudet).getOrElse(List.empty).zipWithIndex.map {
        case (tutkintokokonaisuus, i) =>
          val tutkintokokonaisuusId = idGenerator
          val tutkintokokonaisuusRow =
            buildRYtrTutkintokokonaisuudenSuoritusRow(
              tutkintokokonaisuusId,
              päätasonSuoritusId,
              opiskeluoikeusOid,
              tutkintokokonaisuus,
              (lisätiedotData \ "tutkintokokonaisuudet")(i)
            )

          val tutkintokerrat = tutkintokokonaisuus.tutkintokerrat.zipWithIndex.map {
            case (tutkintokerta, j) =>
              val tutkintokertaId = idGenerator
              val tutkintokertaRow =
                buildRYtrTutkintokerranSuoritusRow(
                  tutkintokertaId,
                  tutkintokokonaisuusId,
                  päätasonSuoritusId,
                  opiskeluoikeusOid,
                  tutkintokerta,
                  ((lisätiedotData \ "tutkintokokonaisuudet")(i) \ "tutkintokerrat")(j)
                )
              tutkintokerta.tutkintokerta.koodiarvo -> tutkintokertaRow
          }.toMap

          val aiemminSuoritetutKokeet = tutkintokokonaisuus.aiemminSuoritetutKokeet.getOrElse(List.empty)

          tutkintokokonaisuus.tunniste -> YtrTutkintokokonaisuusRows(tutkintokokonaisuusRow, tutkintokerrat, aiemminSuoritetutKokeet)
      }.toMap

    val kokeet: Seq[(RYtrKokeenSuoritusRow, Seq[RYtrTutkintokokonaisuudenKokeenSuoritusRow])] =
      ps.osasuoritukset.getOrElse(List.empty).zipWithIndex.map {
        case (koe, i) =>
          val koeId = idGenerator
          val tutkintoKokonaisuus = tutkintokokonaisuudet(koe.tutkintokokonaisuudenTunniste.get)
          val tutkintokertaId = tutkintoKokonaisuus.tutkintokerranSuoritukset(koe.tutkintokerta.koodiarvo).ytrTutkintokerranSuoritusId
          val tutkintokokonaisuusId = tutkintoKokonaisuus.tutkintokokonaisuudenSuoritusRow.ytrTutkintokokonaisuudenSuoritusId
          val koeRow = buildRYtrKokeenSuoritusRow(
            koeId,
            tutkintokertaId,
            tutkintokokonaisuusId,
            päätasonSuoritusId,
            opiskeluoikeusOid,
            koe,
            (psData \ "osasuoritukset")(i)
          )
          val tutkintokokonaisuudenKoeRow =
            buildRYtrTutkintokokonaisuudenKokeenSuoritusRow(
              tutkintokokonaisuusId,
              koeId,
              tutkintokertaId,
              sisällytetty = false
            )

          val kokeenSisällyttävätTutkintokokonaisuudet = tutkintokokonaisuudet.filter{
            case (_, tutkintokokonaisuus) =>
              tutkintokokonaisuus.sisältyvätKokeet.exists { sisältyväKoe =>
                sisältyväKoe.tutkintokerta.koodiarvo == koe.tutkintokerta.koodiarvo &&
                  sisältyväKoe.koulutusmoduuli.tunniste.koodiarvo == koe.koulutusmoduuli.tunniste.koodiarvo
              }
          }.map(_._2.tutkintokokonaisuudenSuoritusRow).toSeq

          val kokeenSisällytetytTutkintokokonaisuudenSuoritusRivit = kokeenSisällyttävätTutkintokokonaisuudet.map(kokonaisuus =>
            buildRYtrTutkintokokonaisuudenKokeenSuoritusRow(
              kokonaisuus.ytrTutkintokokonaisuudenSuoritusId,
              koeId,
              tutkintokertaId,
              sisällytetty = true
            )
          )

          val kelvollisetSisällytetytKokeet =
            kokeenSisällytetytTutkintokokonaisuudenSuoritusRivit.filter(tsr => tsr.ytrTutkintokokonaisuudenSuoritusId != tutkintokokonaisuusId)

          // YTL:n datoissa on toistaiseksi bugi, jolloin sama koe voi sisältyä samaan tutkintokokonaisuuteen kuin kokeen varsinainen suoritus. Voi poistaa myöhemmin kun data on kunnossa.
          if(kelvollisetSisällytetytKokeet.size !=  kokeenSisällytetytTutkintokokonaisuudenSuoritusRivit.size) {
            logger.warn(s"Koe on samassa tutkintokokonaisuudessa sekä varsinaisena että sisällytettynä, koeId: $koeId, tutkintokokonaisuusId: $tutkintokokonaisuusId. Ei lisätty sisällytettyä riviä samaan tutkintokokonaisuuteen.")
            (koeRow, Seq(tutkintokokonaisuudenKoeRow) ++ kelvollisetSisällytetytKokeet)
          } else {
            (koeRow, Seq(tutkintokokonaisuudenKoeRow) ++ kokeenSisällytetytTutkintokokonaisuudenSuoritusRivit)
          }
      }

    (
      päätaso,
      tutkintokokonaisuudet.values.map(_.tutkintokokonaisuudenSuoritusRow).toList,
      tutkintokokonaisuudet.values.map(_.tutkintokerranSuoritukset).flatMap(_.values).toList,
      kokeet.map(_._1).toList,
      kokeet.flatMap(_._2).toList
    )
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
        case k: KoulutustyypinSisältäväKoulutusmoduuli => k.koulutustyyppi.map(_.koodiarvo)
        case _ => None
      },
      koulutusmoduuliLaajuusArvo = ps.koulutusmoduuli.getLaajuus.map(_.arvo),
      koulutusmoduuliLaajuusYksikkö = ps.koulutusmoduuli.getLaajuus.map(_.yksikkö.koodiarvo),
      koulutusmoduuliNimi = ps.koulutusmoduuli.tunniste.getNimi.map(_.get("fi")),
      tutkinnonNimiPerusteessa = ps.koulutusmoduuli match {
        case k: PerusteenNimellinen => k.perusteenNimi.map(_.get("fi"))
        case _ => None
      },
      suorituskieliKoodiarvo = ps match {
        case s: Suorituskielellinen => Some(s.suorituskieli.koodiarvo)
        case m: MahdollisestiSuorituskielellinen => m.suorituskieli.map(_.koodiarvo)
        case _ => None
      },
      oppimääräKoodiarvo = ps match {
        case o: Oppimäärällinen => Some(o.oppimäärä.koodiarvo)
        //case l: LukionPäätasonSuoritus2015 => l.oppimääränKoodiarvo
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
      ensimmäinenArviointiPäivä = os.sortedArviointi.flatMap(_.arviointipäivä).headOption.map(Date.valueOf),
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
        case m: MahdollisestiTunnustettu => m.tunnustettuRahoituksenPiirissä
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

  private def buildRYtrTutkintokokonaisuudenSuoritusRow(
    id: Long,
    päätasonSuoritusId: Long,
    opiskeluoikeusOid: String,
    tk: YlioppilastutkinnonTutkintokokonaisuudenLisätiedot,
    data: JValue
  ): RYtrTutkintokokonaisuudenSuoritusRow = {
    val tyyppiKoodiarvo = tk.tyyppi.map(_.koodiarvo)
    val tilaKoodiarvo = tk.tila.map(_.koodiarvo)
    val hyväksytystiValmistunutTutkinto =
      tyyppiKoodiarvo.exists(_ == "candidate") &&
        tilaKoodiarvo.exists(_ == "graduated")
    RYtrTutkintokokonaisuudenSuoritusRow(
      ytrTutkintokokonaisuudenSuoritusId = id,
      päätasonSuoritusId = päätasonSuoritusId,
      opiskeluoikeusOid = opiskeluoikeusOid,
      tyyppiKoodiarvo = tyyppiKoodiarvo,
      tilaKoodiarvo = tilaKoodiarvo,
      suorituskieliKoodiarvo = tk.suorituskieli.map(_.koodiarvo),
      hyväksytystiValmistunutTutkinto = Some(hyväksytystiValmistunutTutkinto),
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromYtrTutkintokokonaisuudenSuoritusJson)
    )
  }

  private def buildRYtrTutkintokerranSuoritusRow(
    id: Long,
    tutkintokokonaisuusId: Long,
    päätasonSuoritusId: Long,
    opiskeluoikeusOid: String,
    tk:  YlioppilastutkinnonTutkintokerranLisätiedot,
    data: JValue
  ): RYtrTutkintokerranSuoritusRow = {
    RYtrTutkintokerranSuoritusRow(
      ytrTutkintokerranSuoritusId = id,
      ytrTutkintokokonaisuudenSuoritusId = tutkintokokonaisuusId,
      päätasonSuoritusId = päätasonSuoritusId,
      opiskeluoikeusOid = opiskeluoikeusOid,
      tutkintokertaKoodiarvo = tk.tutkintokerta.koodiarvo,
      vuosi = tk.tutkintokerta.vuosi,
      vuodenaikaKoodiarvo = tk.tutkintokerta.koodiarvo.takeRight(1),
      koulutustaustaKoodiarvo = tk.koulutustausta.map(_.koodiarvo),

      oppilaitosOid = tk.oppilaitos.map(_.oid),
      oppilaitosNimi = tk.oppilaitos.map(ol => convertLocalizedString(ol.nimi, "fi")),
      oppilaitosNimiSv = tk.oppilaitos.map(ol => convertLocalizedString(ol.nimi, "sv")),
      oppilaitosKotipaikka = tk.oppilaitos.flatMap(_.kotipaikka).map(_.koodiarvo.stripPrefix("kunta_")),
      oppilaitosnumero = tk.oppilaitos.flatMap(_.oppilaitosnumero).map(_.koodiarvo),
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromYtrTutkintokerranSuoritusJson)
    )
  }

  private def buildRYtrKokeenSuoritusRow(
    id: Long,
    tutkintokertaId: Long,
    tutkintokokonaisuusId: Long,
    päätasonSuoritusId: Long,
    opiskeluoikeusOid: String,
    ks: YlioppilastutkinnonKokeenSuoritus,
    data: JValue
  ) = {
    RYtrKokeenSuoritusRow(
      ytrKokeenSuoritusId = id,
      ytrTutkintokerranSuoritusId = tutkintokertaId,
      ytrTutkintokokonaisuudenSuoritusId = tutkintokokonaisuusId,
      päätasonSuoritusId = päätasonSuoritusId,
      opiskeluoikeusOid = opiskeluoikeusOid,
      suorituksenTyyppi = ks.tyyppi.koodiarvo,
      koulutusmoduuliKoodisto = convertKoodisto(ks.koulutusmoduuli.tunniste),
      koulutusmoduuliKoodiarvo = ks.koulutusmoduuli.tunniste.koodiarvo,
      koulutusmoduuliNimi = ks.koulutusmoduuli.tunniste.getNimi.map(_.get("fi")),
      arviointiArvosanaKoodiarvo = ks.viimeisinArviointi.map(_.arvosana.koodiarvo),
      arviointiArvosanaKoodisto = ks.viimeisinArviointi.flatMap(a => convertKoodisto(a.arvosana)),
      arviointiHyväksytty = ks.viimeisinArviointi.map(_.hyväksytty),
      arviointiPisteet = ks.viimeisinArviointi.flatMap(_.pisteet),
      keskeytynyt = ks.keskeytynyt,
      maksuton = ks.maksuton,
      data = JsonManipulation.removeFields(data, fieldsToExcludeFromYtrKokeenSuoritusJson),
    )
  }

  private def buildRYtrTutkintokokonaisuudenKokeenSuoritusRow(
    tutkintokokonaisuusId: Long,
    koeId: Long,
    tutkintokertaId: Long,
    sisällytetty: Boolean
  ) =
    RYtrTutkintokokonaisuudenKokeenSuoritusRow(
      ytrTutkintokokonaisuudenSuoritusId = tutkintokokonaisuusId,
      ytrKokeenSuoritusId = koeId,
      ytrTutkintokerranSuoritusId = tutkintokertaId,
      sisällytetty = sisällytetty
    )

  private[raportointikanta] def buildRowMitätöity(raw: KoskiOpiskeluoikeusRow): Either[LoadErrorResult, RMitätöityOpiskeluoikeusRow] = {
    for {
      oo <- raw.toOpiskeluoikeus(KoskiSpecificSession.systemUser).left.map(e => LoadErrorResult(raw.oid, mitätöityError + " " + e.toString()))
      mitätöityPvm <- oo.mitätöintiPäivä.toRight(LoadErrorResult(raw.oid, "Mitätöintipäivämäärän haku epäonnistui"))
    } yield RMitätöityOpiskeluoikeusRow(
      opiskeluoikeusOid = raw.oid,
      versionumero = raw.versionumero,
      aikaleima = raw.aikaleima,
      oppijaOid = raw.oppijaOid,
      mitätöity = Some(mitätöityPvm),
      suostumusPeruttu = None,
      tyyppi = raw.koulutusmuoto,
      päätasonSuoritusTyypit = oo.suoritukset.map(_.tyyppi.koodiarvo).distinct
    )
  }

  private[raportointikanta] def buildRowMitätöity(row: PoistettuOpiskeluoikeusRow): Either[LoadErrorResult, RMitätöityOpiskeluoikeusRow] = {
    val aikaleima = row
      .mitätöityAikaleima
      .orElse(row.suostumusPeruttuAikaleima)
      .toRight(LoadErrorResult(
        row.oid,
        "Poistetun opiskeluoikeuden aikaleimaa ei ollut olemassa"
      ))

    aikaleima.map(al =>
      RMitätöityOpiskeluoikeusRow(
        opiskeluoikeusOid = row.oid,
        versionumero = row.versio.getOrElse(0),
        aikaleima = al,
        oppijaOid = row.oppijaOid,
        mitätöity = row.mitätöityAikaleima.map(_.toLocalDateTime.toLocalDate),
        suostumusPeruttu = row.suostumusPeruttuAikaleima.map(_.toLocalDateTime.toLocalDate),
        tyyppi = row.koulutusmuoto,
        päätasonSuoritusTyypit = row.suoritustyypit
      )
    )
  }
}

case class KoskiOutputRows(
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

case class YtrOutputRows(
  rOpiskeluoikeusRow: ROpiskeluoikeusRow,
  rPäätasonSuoritusRows: Seq[RPäätasonSuoritusRow],
  rTutkintokokonaisuudenSuoritusRows: Seq[RYtrTutkintokokonaisuudenSuoritusRow],
  rTutkintokerranSuoritusRows: Seq[RYtrTutkintokerranSuoritusRow],
  rKokeenSuoritusRows: Seq[RYtrKokeenSuoritusRow],
  rTutkintokokonaisuudenKokeenSuoritusRows: Seq[RYtrTutkintokokonaisuudenKokeenSuoritusRow],
  toOpiskeluoikeusUnsafeDuration: Long = 0
)
