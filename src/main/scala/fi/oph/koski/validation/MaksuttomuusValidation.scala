package fi.oph.koski.validation


import com.typesafe.config.Config
import fi.oph.koski.henkilo.{KotikuntahistoriaConfig, LaajatOppijaHenkilöTiedot, OpintopolkuHenkilöFacade}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.{CompositeOpiskeluoikeusRepository, Päivämääräväli}
import fi.oph.koski.oppivelvollisuustieto.Oppivelvollisuustiedot
import fi.oph.koski.schema._
import fi.oph.koski.util.ChainingSyntax.localDateOps
import fi.oph.koski.util.{DateOrdering, FinnishDateFormat}
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasRajapäivätService
import fi.oph.koski.valpas.oppivelvollisuudestavapautus.ValpasOppivelvollisuudestaVapautusService

import java.time.LocalDate
import java.time.LocalDate.{of => date}

object MaksuttomuusValidation extends Logging {

  def checkOpiskeluoikeudenMaksuttomuus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
                                        oppijanHenkilötiedot: Option[LaajatOppijaHenkilöTiedot],
                                        oppijanOid: String,
                                        opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository,
                                        rajapäivät: ValpasRajapäivätService,
                                        oppijanumerorekisteri: OpintopolkuHenkilöFacade,
                                        valpasOppivelvollisuudestaVapautusService: ValpasOppivelvollisuudestaVapautusService,
                                        config: Config,
                                       ): HttpStatus = {
    val oppijanSyntymäpäivä = oppijanHenkilötiedot.flatMap(_.syntymäaika)
    val perusopetuksenAikavälit = opiskeluoikeusRepository.getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(Some(opiskeluoikeus), oppijanOid)

    val maksuttomuustietoSiirretty =
      opiskeluoikeus
        .lisätiedot
        .collect { case l: MaksuttomuusTieto => l.maksuttomuus.toList.flatten.nonEmpty }
        .getOrElse(false)

    val maksuttomuudenPidennysSiirretty =
      opiskeluoikeus
        .lisätiedot
        .collect { case l : MaksuttomuusTieto => l.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.nonEmpty }
        .getOrElse(false)

    // Peruskoulun jälkeinen koulutus on uuden lain mukaiseksi peruskoulun jälkeiseksi oppivelvollisuuskoulutukseksi kelpaavaa
    val koulutusOppivelvollisuuskoulutukseksiKelpaavaa = oppivelvollisuudenSuorittamiseenKelpaavaMuuKuinPeruskoulunOpiskeluoikeus(opiskeluoikeus)

    // Maksuttomuutta ei voi olla opiskeluoikeudessa, joka alkaa myöhemmin kuin vuonna, jolloin oppija täyttää 20 vuotta
    val opiskeluoikeusAlkanutHenkilönOllessaLiianVanha = (oppijanSyntymäpäivä.map(_.getYear), opiskeluoikeus.alkamispäivä.map(_.getYear)) match {
      case (Some(oppijanSyntymävuosi), Some(opiskeluoikeudenAlkamisvuosi))
        if opiskeluoikeudenAlkamisvuosi > oppijanSyntymävuosi + rajapäivät.maksuttomuusLoppuuIka => true
      case _ => false
    }

    // Lukion vanhan opsin mukaiseen opiskeluoikeuteen maksuttomuustiedon saa siirtää vain jos se on alkanut 1.1.2021 tai myöhemmin
    val lukioVanhallaOpsillaSallittuAlkamisjakso = new Aikajakso(
      date(2021, 1, 1),
      None
    )
    val kelpaamatonLukionVanhanOpsinOpiskeluoikeus = (opiskeluoikeus, opiskeluoikeus.alkamispäivä) match {
      case (oo: LukionOpiskeluoikeus, Some(alkamispäivä))
        if (oo.on2015Opiskeluoikeus && !lukioVanhallaOpsillaSallittuAlkamisjakso.contains(alkamispäivä)) => true
      case _ => false
    }

    // Tilanteet, joissa maksuttomuustietoja ei saa siirtää. Jos tuplen ensimmäinen arvo on true, ehto aktivoituu ja toinen arvon kertoo syyn.
    val eiLaajennettuOppivelvollinenSyyt =
      eiOppivelvollisuudenLaajentamislainPiirissäSyyt(oppijanSyntymäpäivä, perusopetuksenAikavälit, rajapäivät)


    val maksutonKoulutus = opiskeluoikeus
      .lisätiedot
      .collect { case l: MaksuttomuusTieto => l.maksuttomuus.toList.flatten }
      .exists(_.exists(_.maksuton))

    val vapautettuOppivelvollisuudesta = valpasOppivelvollisuudestaVapautusService
      .findVapautukset(List(oppijanOid)).exists(vapautus => !vapautus.tulevaisuudessa && !vapautus.mitätöitymässä)

    val vapautettuOppivelvollisuudestaJaKoulutusMaksuton = vapautettuOppivelvollisuudesta && maksutonKoulutus
    val oppijaOnHetuton = oppijanHenkilötiedot.exists(_.hetu.isEmpty)

    val (maksuttomuustietoVaaditaan, maksuttomuustietoVaaditaanLog) = maksuttomuustiedotVaaditaan(
      opiskeluoikeus,
      oppijanHenkilötiedot,
      perusopetuksenAikavälit,
      rajapäivät,
      oppijanumerorekisteri,
      vapautettuOppivelvollisuudesta,
      config,
    )

    val maksuttomuustietoEiSallittuSyyt =
      eiLaajennettuOppivelvollinenSyyt ++ validationTexts(
        (
          !koulutusOppivelvollisuuskoulutukseksiKelpaavaa,
          "koulutus ei siirrettyjen tietojen perusteella kelpaa oppivelvollisuuden suorittamiseen (tarkista, että koulutuskoodi, käytetyn opetussuunnitelman perusteen diaarinumero, suorituksen tyyppi ja/tai suoritustapa ovat oikein)"
        ),
        (
          opiskeluoikeusAlkanutHenkilönOllessaLiianVanha,
          s"opiskeluoikeus on merkitty alkavaksi vuonna, jona oppija täyttää enemmän kuin ${rajapäivät.maksuttomuusLoppuuIka} vuotta"
        ),
        (
          kelpaamatonLukionVanhanOpsinOpiskeluoikeus,
          s"oppija on aloittanut vanhojen lukion opetussuunnitelman perusteiden mukaisen koulutuksen aiemmin kuin ${lukioVanhallaOpsillaSallittuAlkamisjakso.alku}"
        ),
        (
          vapautettuOppivelvollisuudestaJaKoulutusMaksuton,
          s"oppija on vapautettu oppivelvollisuudesta ja koulutusta yritettiin merkitä maksuttomaksi"
        ),
        (
          oppijaOnHetuton,
          s"oppijan tiedoista ei löydy henkilötunnusta"
        ),
      )

    val maksuttomuustietojaSiirretty = maksuttomuustietoSiirretty || maksuttomuudenPidennysSiirretty

    logger.info(s"maksuttomuusValidaatio: ${maksuttomuustietoVaaditaanLog.message(oppijanOid, maksuttomuustietojaSiirretty, maksuttomuustietoEiSallittuSyyt)}")

    HttpStatus.fold(
      validateLiianVarhaisetMaksuttomuudenPidennykset(opiskeluoikeus, oppijanHenkilötiedot, rajapäivät),
      if (maksuttomuustietojaSiirretty) {
        // Maksuttomuustietoja on siirretty -> tarkasta ettei ole syytä, joka kieltää niiden siirtämisen
        HttpStatus.validate(maksuttomuustietoEiSallittuSyyt.isEmpty) {
          val syyt = maksuttomuustietoEiSallittuSyyt.mkString(" ja ")
          KoskiErrorCategory.badRequest.validation(s"Tieto koulutuksen maksuttomuudesta ei ole relevantti tässä opiskeluoikeudessa, sillä $syyt.")
        }
      } else {
        // Maksuttomuustietoja ei ole siirretty -> tarkasta ettei maksuttomuustietojen siirtämistä vaadita
        HttpStatus.validate(!maksuttomuustietoVaaditaan) {
          KoskiErrorCategory.badRequest.validation("Tieto koulutuksen maksuttomuudesta/maksullisuudesta vaaditaan opiskeluoikeudelle.")
        }
      }
    )
  }

  // Huom! Valpas käyttää myös tätä funktiota!
  def oppivelvollisuudenSuorittamiseenKelpaavaMuuKuinPeruskoulunOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean =
    opiskeluoikeus.suoritukset.collectFirst {
      case myp: MYPVuosiluokanSuoritus
        if InternationalSchoolOpiskeluoikeus.onLukiotaVastaavaInternationalSchoolinSuoritus(myp.tyyppi.koodiarvo, myp.koulutusmoduuli.tunniste.koodiarvo) => myp
      case esh: EuropeanSchoolOfHelsinkiVuosiluokanSuoritus
        if EuropeanSchoolOfHelsinkiOpiskeluoikeus.vuosiluokkaKelpaaOppivelvollisuudenSuorittamiseen(esh.koulutusmoduuli.tunniste.koodistoUri, esh.koulutusmoduuli.tunniste.koodiarvo) => esh
      case s: OppivelvollisuudenSuorittamiseenKelpaava => s
    }.isDefined

  // Huom! Valpas käyttää myös tätä funktiota!
  def eiOppivelvollisuudenLaajentamislainPiirissäSyyt(
    oppijanSyntymäpäivä: Option[LocalDate],
    perusopetuksenAikavälit: Seq[Päivämääräväli],
    rajapäivät: ValpasRajapäivätService
  ): Seq[String] =
  {
    val lakiVoimassaPeruskoulustaValmistuneille = rajapäivät.lakiVoimassaPeruskoulustaValmistuneillaAlku
    val lakiVoimassaVanhinSyntymäaika = rajapäivät.lakiVoimassaVanhinSyntymäaika

    // Oppijalla on Koskessa valmistumismerkintä peruskoulusta (tai vastaavasta) 31.12.2020 tai aiemmin
    val valmistunutPeruskoulustaEnnen2021 = perusopetuksenAikavälit.exists(p => p.vahvistuspäivä.exists(_.isBefore(lakiVoimassaPeruskoulustaValmistuneille)))

    val oppijanIkäOikeuttaaMaksuttomuuden = oppijanSyntymäpäivä.exists(bd => !lakiVoimassaVanhinSyntymäaika.isAfter(bd))

    validationTexts(
      (valmistunutPeruskoulustaEnnen2021, s"oppija on suorittanut oppivelvollisuutensa ennen ${lakiVoimassaPeruskoulustaValmistuneille.format(FinnishDateFormat.finnishDateFormat)} eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin"),
      (oppijanSyntymäpäivä.isEmpty, "oppijan syntymäaika puuttuu oppijanumerorekisteristä"),
      (oppijanSyntymäpäivä.isDefined && !oppijanIkäOikeuttaaMaksuttomuuden, s"oppija on syntynyt ennen vuotta ${lakiVoimassaVanhinSyntymäaika.getYear()} eikä tästä syystä kuulu laajennetun oppivelvollisuuden piiriin"),
    )
  }

  case class MaksuttomuustiedotVaaditaanLogData(
    vanha: Boolean,
    uusi: Boolean,
  ) {
    def message(oppijaOid: String, maksuttomuustietoSiirretty: Boolean, maksuttomuustietoEiSallittuSyyt: Seq[String]): String = {
      def validaatiotulos(maksuttomuustietoVaaditaan: Boolean): String =
        (maksuttomuustietoVaaditaan, maksuttomuustietoEiSallittuSyyt.nonEmpty) match {
          case (true, true) => "Logiikkabugi"
          case (false, false) => "OK (ihan sama onko maksuttomuustietoja)"
          case (true, false) => if (maksuttomuustietoSiirretty) "OK" else "Hylätty"
          case (false, true) => if (maksuttomuustietoSiirretty) "Hylätty" else "OK"
          case _ => "???"
        }

      List(
        "oppijaOid" -> oppijaOid,
        "maksuttomuustietoVaaditaanVanha" -> vanha,
        "maksuttomuustietoVaaditaanUusi" -> uusi,
        "maksuttomuustietoEiSallittuSyyt" -> maksuttomuustietoEiSallittuSyyt.mkString(" "),
        "maksuttomuustietoSiirretty" -> maksuttomuustietoSiirretty,
        "vanhaValidaatio" -> validaatiotulos(vanha),
        "uusiValidaatio" -> validaatiotulos(uusi),
      ).map { case (k, v) => s"$k=$v" }.mkString(" | ")
    }
  }

  def maksuttomuustiedotVaaditaan(
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    oppijanHenkilötiedot: Option[LaajatOppijaHenkilöTiedot],
    perusopetuksenAikavälit: Seq[Päivämääräväli],
    rajapäivät: ValpasRajapäivätService,
    oppijanumerorekisteri: OpintopolkuHenkilöFacade,
    vapautettuOppivelvollisuudesta: Boolean,
    config: Config,
  ): (Boolean, MaksuttomuustiedotVaaditaanLogData) = {
    val oppijanSyntymäpäivä = oppijanHenkilötiedot.flatMap(_.syntymäaika)
    val oppijaOid = oppijanHenkilötiedot.map(_.oid)

    // 1. henkilö syntynyt vuonna 2004 tai sen jälkeen
    val oppijaOnSyntymäajanPerusteellaLainPiirissä =
      oppijanSyntymäpäivä.exists(_.isEqualOrAfter(rajapäivät.lakiVoimassaVanhinSyntymäaika))

    // 2. oppijalla ei ole valmistunut-tilaista perusopetuksen opiskeluoikeutta ennen 1.1.2021
    val eiOleValmistunutPeruskoulustaEnnenOppivelvollisuuslainVoimaanAstumista =
      !perusopetuksenAikavälit.exists(_.vahvistuspäivä.exists(_.isBefore(rajapäivät.lakiVoimassaPeruskoulustaValmistuneillaAlku)))

    // 3. opintojen tulee olla alkanut ennen sen vuoden loppua, jolloin oppija täyttää 20 vuotta
    val maksuttomuusVoimassaSyntymäpäivänPerusteellaAsti =
      oppijanSyntymäpäivä.map(_.plusYears(rajapäivät.maksuttomuusLoppuuIka.toInt).atEndOfYear)
    val opinnotAlkaneetEnnenKuinMaksuttomuudenYläikärajaOnTäyttynyt =
      (maksuttomuusVoimassaSyntymäpäivänPerusteellaAsti, opiskeluoikeus.alkamispäivä) match {
        case (Some(rajapäivä), Some(alkamispäivä)) if alkamispäivä.isEqualOrBefore(rajapäivä) => true
        case _ => false
      }

    // 4. koulutus kelpaa oppivelvollisuuden suorittamiseen
    val koulutusKelpaaOppivelvollisuudenSuorittamiseen =
      oppivelvollisuudenSuorittamiseenKelpaavaMuuKuinPeruskoulunOpiskeluoikeus(opiskeluoikeus)

    // 5. oppija on kotikuntahistorian perusteella lain piirissä
    lazy val oppijaOnKotikuntahistorianPerusteellaLainPiirissä =
      (oppijaOid, oppijanSyntymäpäivä) match {
        case (Some(oid), Some(syntymäpäivä)) => Oppivelvollisuustiedot.oppivelvollinenKotikuntahistorianPerusteella(oid, syntymäpäivä, oppijanumerorekisteri)
        case _ => false
      }

    // TOR-2302 osatutkintotavoitteisiin ja ESH-tutkintoihin ei vaadita maksuttomuustietoa
    val eiOsatutkintotavoitteinen = !opiskeluoikeus.suoritukset.exists(_.isInstanceOf[AmmatillisenTutkinnonOsittainenSuoritus])
    val eiESH = !opiskeluoikeus.suoritukset.exists(_.isInstanceOf[EuropeanSchoolOfHelsinkiPäätasonSuoritus])

    val originalResult = false

    val newResult = oppijaOnSyntymäajanPerusteellaLainPiirissä &&
      eiOleValmistunutPeruskoulustaEnnenOppivelvollisuuslainVoimaanAstumista &&
      opinnotAlkaneetEnnenKuinMaksuttomuudenYläikärajaOnTäyttynyt &&
      koulutusKelpaaOppivelvollisuudenSuorittamiseen &&
      oppijaOnKotikuntahistorianPerusteellaLainPiirissä &&
      eiOsatutkintotavoitteinen &&
      eiESH &&
      // 6.oppijaa ei ole vapautettu oppivelvollisuudesta
      !vapautettuOppivelvollisuudesta

    val logData = MaksuttomuustiedotVaaditaanLogData(originalResult, newResult)

    (
      if (KotikuntahistoriaConfig(config).käytäMaksuttomuustietojenValidointiin) newResult else originalResult,
      logData,
    )
  }

  def validateAndFillJaksot(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
   opiskeluoikeus.lisätiedot.collect {
     case lisätiedot: MaksuttomuusTieto => {
       val oikeuttaMaksuttomuuteenPidennetty = sortJaksonAlkupäivänMukaan(lisätiedot.oikeuttaMaksuttomuuteenPidennetty.toList.flatten)
       val maksuttomuus = sortJaksonAlkupäivänMukaan(lisätiedot.maksuttomuus.toList.flatten)

       for {
         validMaksuttomuus <- validateAndFillMaksuttomuusJaksot(maksuttomuus, opiskeluoikeus)
         validMaksuttomuuttaPidennetty <- validateMaksuttomuuttaPidennetty(oikeuttaMaksuttomuuteenPidennetty, validMaksuttomuus, opiskeluoikeus)
       } yield (
         opiskeluoikeus
           .withLisätiedot(Some(lisätiedot
             .withMaksuttomus(toOptional(validMaksuttomuus))
             .withOikeuttaMaksuttomuuteenPidennetty(toOptional(validMaksuttomuuttaPidennetty))
           ))
         )
     }
   }.getOrElse(Right(opiskeluoikeus))
  }

  private def sortJaksonAlkupäivänMukaan[A <: Alkupäivällinen](jaksot: List[A]): List[A] = jaksot.sortBy(_.alku)(DateOrdering.localDateOrdering)

  private def validateAndFillMaksuttomuusJaksot(jaksot: List[Maksuttomuus], opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    val voimassaolonUlkopuolella = jaksot.map(_.alku).filterNot(d => between(opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä)(d))
    val samojaAlkamispäiviä = jaksot.map(_.alku).groupBy(x => x).filter(_._2.length > 1).values.flatten.toSeq

    val validationResult = HttpStatus.fold(
      validate(voimassaolonUlkopuolella)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuusjaksoja, jonka alkupäivä ${x.map(_.toString).mkString(", ")} ei ole opiskeluoikeuden voimassaolon (${voimassaolo(opiskeluoikeus)}) sisällä")),
      validate(samojaAlkamispäiviä)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuusjaksoja, joilla on sama alkupäivä ${x.map(_.toString).mkString(", ")}"))
    )

    if (validationResult.isOk) Right(fillPäättymispäivät(jaksot)) else Left(validationResult)
  }

  private def validateMaksuttomuuttaPidennetty(jaksot: List[OikeuttaMaksuttomuuteenPidennetty], maksuttomuus: List[Maksuttomuus], opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) = {
    def alkuBetweenOpiskeluoikeudenAlkamisPäättymis(jakso: OikeuttaMaksuttomuuteenPidennetty) = {
      val voimassaolonSisällä = between(opiskeluoikeus.alkamispäivä, opiskeluoikeus.päättymispäivä)_
      voimassaolonSisällä(jakso.alku)
    }

    val voimassaolonUlkopuolella = jaksot.filterNot(alkuBetweenOpiskeluoikeudenAlkamisPäättymis)
    val jaksonAlkuEnnenLoppua = jaksot.filterNot(jakso => !jakso.alku.isAfter(jakso.loppu))
    val päällekkäisiäJaksoja = jaksot.zip(jaksot.drop(1)).filter { case (a,b) => a.overlaps(b) }

    val maksuttomatMaksuttomuusJaksot = maksuttomuus.filter(_.maksuton)
    val pidennysMaksuttomuudenUlkopuolella = jaksot.filterNot(pidennys => maksuttomatMaksuttomuusJaksot.exists(maksuton => maksuton.containsPidennysJakso(pidennys)))

    val validationResult = HttpStatus.fold(
      validate(voimassaolonUlkopuolella)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka alku- ja/tai loppupäivä ei ole opiskeluoikeuden voimassaolon (${voimassaolo(opiskeluoikeus)}) sisällä ${x.map(_.toString).mkString(", ")}")),
      validate(jaksonAlkuEnnenLoppua)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyvä jakso, jonka loppupäivä on aikaisemmin kuin alkupäivä. ${x.map(y => s"${y.alku} (alku) - ${y.loppu} (loppu)").mkString(", ")}")),
      validate(päällekkäisiäJaksoja)(x => KoskiErrorCategory.badRequest.validation(s"Opiskeluoikeudella on koulutuksen maksuttomuuden pidennykseen liittyviä jaksoja, jotka ovat keskenään päällekkäisiä ${x.map(_.toString).mkString(", ")}")),
      validate(pidennysMaksuttomuudenUlkopuolella)(x => KoskiErrorCategory.badRequest.validation(s"Maksuttomuutta voidaan pidetäntää vain aikavälillä jolloin koulutus on maksutontonta")),
    )

    if (validationResult.isOk) Right(jaksot) else Left(validationResult)
  }

  private def validateLiianVarhaisetMaksuttomuudenPidennykset(
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    oppijanHenkilötiedot: Option[LaajatOppijaHenkilöTiedot],
    rajapäivät: ValpasRajapäivätService,
   ): HttpStatus = {
    val pidennykset =
      opiskeluoikeus
        .lisätiedot
        .flatMap {
          case m: MaksuttomuusTieto => Some(m)
          case _ => None
        }
        .flatMap(_.oikeuttaMaksuttomuuteenPidennetty)
        .getOrElse(List.empty)

    val maksuttomuusPäättyyIänPerusteella = oppijanHenkilötiedot
      .flatMap(_.syntymäaika)
      .map(rajapäivät.maksuttomuusVoimassaAstiIänPerusteella)

    // Maksuttomuuden pidennysjaksot ennen maksuttomuuskauden varsinaista loppua, jotka sisältävät päiviä ennen 1.8.2022
    // ovat sallittuja ja päivät ennen 1.8.2022 lisätään maksuttomuuskauden loppuun
    val virheelliset =
      maksuttomuusPäättyyIänPerusteella
        .toList
        .flatMap(rajapäivä => pidennykset.filter(p => p.alku.isEqualOrAfter(date(2022, 8, 1)) && p.alku.isEqualOrBefore(rajapäivä)))

    if (virheelliset.isEmpty) {
      HttpStatus.ok
    } else {
      val fmt = FinnishDateFormat.finnishDateFormat
      val aikajaksot = virheelliset
        .map(a => s"${a.alku.format(fmt)}–${a.loppu.format(fmt)}")
        .mkString(", ")
      val raja = maksuttomuusPäättyyIänPerusteella
        .map(_.plusDays(1))
        .map(_.format(fmt))
        .getOrElse("???")
      KoskiErrorCategory.badRequest.validation(s"Maksuttomuuden pidennyksen aikajakso ($aikajaksot) voi alkaa aikaisintaan ${raja}")
    }
  }

  private def fillPäättymispäivät(maksuttomuus: List[Maksuttomuus]) = {
    val jaksot = maksuttomuus.map(_.copy(loppu = None))
    val last = jaksot.lastOption.toList
    val filled = jaksot.zip(jaksot.drop(1)).map { case (a, b) => a.copy(loppu = Some(b.alku.minusDays(1))) }
    filled ::: last
  }

  private def validate[A](virheelliset: Seq[A])(virheviesti: Seq[A] => HttpStatus) =
    if (virheelliset.length > 0) virheviesti(virheelliset) else HttpStatus.ok

  private def between(start: Option[LocalDate], end: Option[LocalDate])(date: LocalDate) =
    start.map(alku => !date.isBefore(alku)).getOrElse(false) && end.map(loppu => !date.isAfter(loppu)).getOrElse(true)

  private def voimassaolo(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus) =
    s"${opiskeluoikeus.alkamispäivä.map(_.toString).getOrElse("")} - ${opiskeluoikeus.päättymispäivä.map(_.toString).getOrElse("")}"

  private def toOptional[A](xs: List[A]): Option[List[A]] = if (xs.isEmpty) None else Some(xs)

  private def validationTexts(ts: (Boolean, String)*): Seq[String] = ts.filter(_._1).map(_._2)
}
