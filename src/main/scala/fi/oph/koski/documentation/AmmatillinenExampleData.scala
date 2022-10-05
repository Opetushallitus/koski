package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat}
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

object AmmatillinenExampleData {
  val exampleHenkilö = asUusiOppija(KoskiSpecificMockOppijat.ammattilainen)

  val autoalanPerustutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", None, "koulutus", Some(11)), Some("39/011/2014"))
  val ajoneuvoalanPerustutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", None, "koulutus", Some(12)), Some("OPH-5410-2021"))
  val valmaKoulutus: ValmaKoulutus = ValmaKoulutus(Koodistokoodiviite("999901", "koulutus"), Some("OPH-2658-2017"))
  val parturikampaaja: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("381303", "koulutus"), Some("43/011/2014"))
  val puutarhuri: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("361201", "koulutus"), Some("75/011/2014"))
  val autoalanTyönjohto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357305", "koulutus"), Some("40/011/2001"))
  val puuteollisuudenPerustutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351741", "koulutus"), Some("OPH-2455-2017"))
  val sosiaaliJaTerveysalanPerustutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("371101", "koulutus"), Some("79/011/2014"))
  val tietoJaViestintäTekniikanPerustutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("341101", "koulutus"), Some("OPH-1117-2019"))
  val virheellinenPuuteollisuudenPerustutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351741", "koulutus"), Some("OPH-992455-2017"))

  val tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus = TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus(
    PaikallinenKoodi("KISI", "Kiinteistösihteerin koulutus ja tutkinto (KISI)"),
    None,
    LocalizedString.finnish("Koulutus antaa opiskelijalle valmiudet hoitaa isännöinti- ja kiinteistöpalvelualan yritysten sihteeri- ja asiakaspalvelutehtäviä.")
  )
  val muuAmmatillinenKoulutus: PaikallinenMuuAmmatillinenKoulutus = PaikallinenMuuAmmatillinenKoulutus(
    PaikallinenKoodi("KISI", "Kiinteistösihteerin koulutus ja tutkinto (KISI)"),
    None,
    LocalizedString.finnish("Koulutus antaa opiskelijalle valmiudet hoitaa isännöinti- ja kiinteistöpalvelualan yritysten sihteeri- ja asiakaspalvelutehtäviä.")
  )
  val ammatilliseenTehtäväänValmistavaKoulutus: AmmatilliseenTehtäväänValmistavaKoulutus = AmmatilliseenTehtäväänValmistavaKoulutus(
    Koodistokoodiviite("1", "ammatilliseentehtavaanvalmistavakoulutus"),
    None,
    Some(LocalizedString.finnish("Liikennelentäjät lentävät monentyyppisiä lentokoneita kuljettaen matkustajia, rahtia ja postia."))
  )

  def autoalanPerustutkinnonSuoritus(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = ammatillinenTutkintoSuoritus(autoalanPerustutkinto, toimipiste)
  def ajoneuvoalanPerustutkinnonSuoritus(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = ammatillinenTutkintoSuoritus(ajoneuvoalanPerustutkinto, toimipiste).copy(suoritustapa = suoritustapaReformi)
  def autoalanPerustutkinnonSuoritusValma(toimipiste: OrganisaatioWithOid = stadinToimipiste): ValmaKoulutuksenSuoritus = valmaSuoritus(valmaKoulutus, toimipiste)
  def autoalanErikoisammattitutkinnonSuoritus(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = ammatillinenTutkintoSuoritus(autoalanTyönjohto, toimipiste)
  def puuteollisuudenPerustutkinnonSuoritus(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = ammatillinenTutkintoSuoritus(puuteollisuudenPerustutkinto, toimipiste)
  def tietoJaViestintäTekniikanPerustutkinnonSuoritus(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = ammatillinenTutkintoSuoritus(tietoJaViestintäTekniikanPerustutkinto, toimipiste)
  def virheellinenPuuteollisuudenPerustutkinnonSuoritus(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = ammatillinenTutkintoSuoritus(virheellinenPuuteollisuudenPerustutkinto, toimipiste)

  def kiinteistösihteerinTutkinnonOsaaPienempiMuuAmmatillinenKokonaisuus(toimipiste: OrganisaatioWithOid = stadinToimipiste): TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =
    tutkinnonOsaaPienempienKokonaisuuksienSuoritus(
      tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus,
      toimipiste
    )
  def kiinteistösihteerinMuuAmmatillinenKoulutus(toimipiste: OrganisaatioWithOid = stadinToimipiste): MuunAmmatillisenKoulutuksenSuoritus =
    muunAmmatillisenKoulutuksenSuoritus(
      muuAmmatillinenKoulutus,
      toimipiste
    )
  def ansioJaLiikenneLentäjänMuuAmmatillinenKoulutus(toimipiste: OrganisaatioWithOid = stadinToimipiste): MuunAmmatillisenKoulutuksenSuoritus =
    muunAmmatillisenKoulutuksenSuoritus(
      ammatilliseenTehtäväänValmistavaKoulutus,
      toimipiste
    )

  def ammatillinenTutkintoSuoritus(koulutusmoduuli: AmmatillinenTutkintoKoulutus, toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = AmmatillisenTutkinnonSuoritus(
    koulutusmoduuli = koulutusmoduuli,
    alkamispäivä = Some(date(2016, 9, 1)),
    toimipiste = toimipiste,
    suorituskieli = suomenKieli,
    suoritustapa = suoritustapaOps
  )

  def valmaSuoritus(koulutusmoduuli: ValmaKoulutus, toimipiste: OrganisaatioWithOid = stadinToimipiste): ValmaKoulutuksenSuoritus = ValmaKoulutuksenSuoritus(
    koulutusmoduuli = valmaKoulutus,
    toimipiste = toimipiste,
    suorituskieli = suomenKieli,
    osasuoritukset = None
  )

  def tutkinnonOsaaPienempienKokonaisuuksienSuoritus(koulutusmoduuli: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus, toimipiste: OrganisaatioWithOid = stadinToimipiste): TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =
    TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(
      koulutusmoduuli = koulutusmoduuli,
      alkamispäivä = Some(date(2018, 1, 1)),
      toimipiste = toimipiste,
      suorituskieli = suomenKieli,
      osasuoritukset = None
    )

  def muunAmmatillisenKoulutuksenSuoritus(koulutusmoduuli: MuuAmmatillinenKoulutus, toimipiste: OrganisaatioWithOid = stadinToimipiste): MuunAmmatillisenKoulutuksenSuoritus =
    MuunAmmatillisenKoulutuksenSuoritus(
      koulutusmoduuli = koulutusmoduuli,
      alkamispäivä = Some(date(2018, 1, 1)),
      toimipiste = toimipiste,
      suorituskieli = suomenKieli,
      täydentääTutkintoa = None,
      vahvistus = None,
      osasuoritukset = None
    )

  lazy val hylätty: Koodistokoodiviite = Koodistokoodiviite("0", Some("H"), "arviointiasteikkoammatillinent1k3", None)
  lazy val h2: Koodistokoodiviite = Koodistokoodiviite("2", Some("H2"), "arviointiasteikkoammatillinent1k3", None)
  lazy val k3: Koodistokoodiviite = Koodistokoodiviite("3", Some("K3"), "arviointiasteikkoammatillinent1k3", None)

  lazy val näytönArvioitsijat = Some(List(NäytönArvioitsija("Jaana Arstila", Some(true)), NäytönArvioitsija("Pekka Saurmann", Some(true)), NäytönArvioitsija("Juhani Mykkänen", Some(false))))

  lazy val arviointikohteet1k3 = Some(List(
    NäytönArviointikohde(Koodistokoodiviite("1", Some("Työprosessin hallinta"), "ammatillisennaytonarviointikohde", None), k3),
    NäytönArviointikohde(Koodistokoodiviite("2", Some("Työmenetelmien, -välineiden ja materiaalin hallinta"), "ammatillisennaytonarviointikohde", None), h2),
    NäytönArviointikohde(Koodistokoodiviite("3", Some("Työn perustana olevan tiedon hallinta"), "ammatillisennaytonarviointikohde", None), h2),
    NäytönArviointikohde(Koodistokoodiviite("4", Some("Elinikäisen oppimisen avaintaidot"), "ammatillisennaytonarviointikohde", None), k3)
  ))

  lazy val arviointikohteet15 = Some(List(
    NäytönArviointikohde(Koodistokoodiviite("1", Some("Työprosessin hallinta"), "ammatillisennaytonarviointikohde", None), arvosanaViisi),
    NäytönArviointikohde(Koodistokoodiviite("2", Some("Työmenetelmien, -välineiden ja materiaalin hallinta"), "ammatillisennaytonarviointikohde", None), arvosanaViisi),
    NäytönArviointikohde(Koodistokoodiviite("3", Some("Työn perustana olevan tiedon hallinta"), "ammatillisennaytonarviointikohde", None), hyväksytty),
    NäytönArviointikohde(Koodistokoodiviite("4", Some("Elinikäisen oppimisen avaintaidot"), "ammatillisennaytonarviointikohde", None), arvosanaViisi)
  ))

  lazy val näytönArviointi = NäytönArviointi(
    arvosana = arviointiKiitettävä.arvosana,
    päivä = arviointiKiitettävä.päivä,
    arvioitsijat = näytönArvioitsijat,
    arviointikohteet = arviointikohteet1k3,
    arvioinnistaPäättäneet = Some(List(Koodistokoodiviite("1", Some("Opettaja"), "ammatillisennaytonarvioinnistapaattaneet", None))),
    arviointikeskusteluunOsallistuneet =
      Some(List(
        Koodistokoodiviite("1", Some("Opettaja"), "ammatillisennaytonarviointikeskusteluunosallistuneet", None),
        Koodistokoodiviite("4", Some("Opiskelija"), "ammatillisennaytonarviointikeskusteluunosallistuneet", None)
      ))
  )

  def näyttö(päivä: LocalDate, kuvaus: String, paikka: String, arviointi: Option[NäytönArviointi] = None) = Näyttö(
    Some(kuvaus),
    Some(NäytönSuorituspaikka(Koodistokoodiviite("1", Some("työpaikka"), "ammatillisennaytonsuorituspaikka", Some(1)), paikka)),
    Some(NäytönSuoritusaika(päivä, päivä)),
    false,
    arviointi
  )

  lazy val suoritustapaNäyttö = Koodistokoodiviite("naytto", Some("Näyttö"), None, "ammatillisentutkinnonsuoritustapa", Some(1))
  lazy val suoritustapaOps = Koodistokoodiviite("ops", Some("Ammatillinen perustutkinto"), "ammatillisentutkinnonsuoritustapa", Some(1))
  lazy val suoritustapaReformi = Koodistokoodiviite("reformi", Some("Ammatillinen perustutkinto"), "ammatillisentutkinnonsuoritustapa", Some(1))
  lazy val järjestämismuotoOppisopimus = OppisopimuksellinenJärjestämismuoto(Koodistokoodiviite("20", Some("Oppisopimusmuotoinen"), "jarjestamismuoto", Some(1)), Oppisopimus(Yritys("Autokorjaamo Oy", "1234567-8")))
  lazy val järjestämismuotoOppilaitos = JärjestämismuotoIlmanLisätietoja(Koodistokoodiviite("10", Some("Oppilaitosmuotoinen"), "jarjestamismuoto", Some(1)))
  lazy val osaamisenHankkimistapaOppilaitos = OsaamisenHankkimistapaIlmanLisätietoja(Koodistokoodiviite("oppilaitosmuotoinenkoulutus", Some("Oppilaitosmuotoinen"), "osaamisenhankkimistapa", Some(1)))
  lazy val deprekoituOsaamisenHankkimistapaOppilaitos = OsaamisenHankkimistapaIlmanLisätietoja(Koodistokoodiviite("oppisopimus", Some("Oppilaitosmuotoinen"), "osaamisenhankkimistapa", Some(1)))
  lazy val osaamisenHankkimistapaOppisopimus = OppisopimuksellinenOsaamisenHankkimistapa(Koodistokoodiviite("oppisopimus", Some("Oppisopimus"), "osaamisenhankkimistapa", Some(1)), Oppisopimus(Yritys("Autokorjaamo Oy", "1234567-8"), oppisopimuksenPurkaminen = Some(OppisopimuksenPurkaminen(date(2013, 3, 20), purettuKoeajalla = true))))
  lazy val stadinAmmattiopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto, Some(Koodistokoodiviite("10105", None, "oppilaitosnumero", None)), Some("Stadin ammattiopisto"))
  lazy val kiipulasäätiö: Koulutustoimija = Koulutustoimija(MockOrganisaatiot.kiipulasäätiö, Some("Kiipulasäätiö sr"))
  lazy val kiipulanAmmattiopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.kiipulanAmmattiopisto)
  lazy val kiipulanAmmattiopistoNokianToimipaikka: Toimipiste = Toimipiste(MockOrganisaatiot.kiipulanAmmattiopistoNokianToimipaikka, Some("Kiipulan ammattiopiston Nokian toimipaikka"))
  lazy val länsirannikonKoulutusOy = Koulutustoimija(MockOrganisaatiot.länsirannikonKoulutusOy, Some("Länsirannikon koulutus Oy"), Some("2245018-4"), Some(jyväskylä))
  lazy val autokorjaamoOy: Yritys = Yritys("Autokorjaamo Oy", "1234567-8")

  lazy val stadinToimipiste: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.lehtikuusentienToimipiste, Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))
  lazy val stadinOppisopimuskeskus: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.stadinOppisopimuskeskus, Some("Stadin oppisopimuskeskus"))
  lazy val tutkintotoimikunta: Organisaatio = Tutkintotoimikunta("Autokorjaamoalan tutkintotoimikunta", "8406")
  lazy val lähdeWinnova = Koodistokoodiviite("winnova", Some("Winnova"), "lahdejarjestelma", Some(1))
  lazy val lähdePrimus = Koodistokoodiviite("primus", Some("Primus"), "lahdejarjestelma", Some(1))
  lazy val arvosanaViisi = Koodistokoodiviite("5", Some("5"), "arviointiasteikkoammatillinen15", Some(1))
  lazy val arviointiViisi = Some(List(arviointi(arvosanaViisi)))
  lazy val hyväksytty: Koodistokoodiviite = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1))
  lazy val suoritettu: Koodistokoodiviite = Koodistokoodiviite("Suoritettu", Some("Suoritettu"), "arviointiasteikkomuuammatillinenkoulutus", Some(1))
  lazy val tunnustettu: OsaamisenTunnustaminen = OsaamisenTunnustaminen(
    Some(MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100238", Some("Asennushitsaus"), "tutkinnonosat"), true, None),
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = None
    )),
    "Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta"
  )

  lazy val arviointiHyväksytty = AmmatillinenArviointi(
    arvosana = hyväksytty, date(2013, 3, 20),
    arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen"))))
  lazy val arviointiSuoritettu = MuunAmmatillisenKoulutuksenArviointi(
    arvosana = suoritettu, date(2013, 3, 20),
    arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen"))))

  lazy val paikallisenOsanSuoritus = MuunAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = PaikallinenTutkinnonOsa(PaikallinenKoodi("123456789", "Pintavauriotyöt"), "Opetellaan korjaamaan pinnallisia vaurioita", false, None),
    tunnustettu = None,
    näyttö = Some(näyttö(date(2013, 5, 20), "Pintavaurioiden korjausta", "Autokorjaamo Oy, Riihimäki")),
    lisätiedot = None,
    suorituskieli = None,
    alkamispäivä = None,
    toimipiste = Some(stadinToimipiste),
    arviointi = Some(List(arviointiHyväksytty)),
    vahvistus = vahvistusValinnaisellaTittelillä(date(2013, 5, 31), stadinAmmattiopisto),
    tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat
  )

  def winnovaLähdejärjestelmäId(ooId: String) = LähdejärjestelmäId(Some(ooId), lähdeWinnova)
  def primusLähdejärjestelmäId(ooId: String) = LähdejärjestelmäId(Some(ooId), lähdePrimus)

  def autonLisävarustetyöt(pakollinen: Boolean, kuvaus: String = "Tuunaus") = MuuValtakunnallinenTutkinnonOsa(
    Koodistokoodiviite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat"),
    pakollinen,
    Some(LaajuusOsaamispisteissä(15)),
    Some(kuvaus)
  )

  def arviointi(arvosana: Koodistokoodiviite) = AmmatillinenArviointi(
    arvosana = arvosana,
    date(2014, 10, 20)
  )


  lazy val arviointiKiitettävä = arviointi(k3)
  lazy val arviointiHylätty = arviointi(hylätty)

  lazy val ammatillisetTutkinnonOsat = Some(Koodistokoodiviite("1", "ammatillisentutkinnonosanryhma"))
  lazy val yhteisetTutkinnonOsat = Some(Koodistokoodiviite("2", "ammatillisentutkinnonosanryhma"))
  lazy val vapaavalintaisetTutkinnonOsat = Some(Koodistokoodiviite("3", "ammatillisentutkinnonosanryhma"))
  lazy val yksilöllisestiLaajentavatTutkinnonOsat = Some(Koodistokoodiviite("4", "ammatillisentutkinnonosanryhma"))

  def opiskeluoikeus(oppilaitos: Oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto),
                     tutkinto: AmmatillisenTutkinnonSuoritus = autoalanPerustutkinnonSuoritus(stadinToimipiste),
                     osat: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None): AmmatillinenOpiskeluoikeus = {
    AmmatillinenOpiskeluoikeus(
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(date(2016, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)))),
      oppilaitos = Some(oppilaitos),
      suoritukset = List(tutkinto.copy(osasuoritukset = osat))
    )
  }

  def oppija(henkilö: UusiHenkilö = exampleHenkilö, opiskeluoikeus: Opiskeluoikeus = this.opiskeluoikeus()) = {
    Oppija(
      henkilö,
      List(opiskeluoikeus)
    )
  }

  def tutkinnonOsanSuoritus(koodi: String, nimi: String, ryhmä: Option[Koodistokoodiviite], arvosana: Koodistokoodiviite, laajuus: Float): MuunAmmatillisenTutkinnonOsanSuoritus = {
    tutkinnonOsanSuoritus(koodi, nimi, ryhmä, arvosana, Some(laajuus))
  }

  def yhteisenTutkinnonOsanSuoritus(koodi: String, nimi: String, arvosana: Koodistokoodiviite, laajuus: Float): YhteisenAmmatillisenTutkinnonOsanSuoritus = {
    val osa = YhteinenTutkinnonOsa(Koodistokoodiviite(koodi, Some(nimi), "tutkinnonosat"), true, Some(LaajuusOsaamispisteissä(laajuus)))
    YhteisenAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = osa,
      tutkinnonOsanRyhmä = yhteisetTutkinnonOsat,
      näyttö = None,
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(arvosana = arvosana, date(2014, 10, 20)))),
      vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 5, 31), stadinAmmattiopisto)
    )
  }

  def tutkinnonOsanSuoritus(koodi: String, nimi: String, ryhmä: Option[Koodistokoodiviite], laajuus: Option[Float]): AmmatillisenTutkinnonOsanSuoritus = {
    val osa = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite(koodi, Some(nimi), "tutkinnonosat"), true, laajuus.map(l =>LaajuusOsaamispisteissä(l)))
    MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = osa,
      tutkinnonOsanRyhmä = ryhmä,
      näyttö = None,
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste)
    )
  }

  def tutkinnonOsanSuoritus(koodi: String, nimi: String, ryhmä: Option[Koodistokoodiviite], arvosana: Koodistokoodiviite, laajuus: Option[Float] = None, pakollinen: Boolean = true): MuunAmmatillisenTutkinnonOsanSuoritus = {
    val osa = MuuValtakunnallinenTutkinnonOsa(tunniste = Koodistokoodiviite(koodi, Some(nimi), "tutkinnonosat"), pakollinen, laajuus = laajuus.map(l => LaajuusOsaamispisteissä(l)))
    tutkinnonOsanSuoritus(arvosana, osa, ryhmä)
  }

  def paikallisenTutkinnonOsanSuoritus(koodi: String, nimi: String, ryhmä: Option[Koodistokoodiviite], arvosana: Koodistokoodiviite, laajuus: Float): AmmatillisenTutkinnonOsanSuoritus = {
    val osa: PaikallinenTutkinnonOsa = PaikallinenTutkinnonOsa(PaikallinenKoodi(koodi, nimi), nimi, false, Some(LaajuusOsaamispisteissä(laajuus)))
    tutkinnonOsanSuoritus(arvosana, osa, ryhmä)
  }

  def tutkinnonOsanSuoritus(arvosana: Koodistokoodiviite, osa: MuuKuinYhteinenTutkinnonOsa, ryhmä: Option[Koodistokoodiviite]): MuunAmmatillisenTutkinnonOsanSuoritus = {
    MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = osa,
      tutkinnonOsanRyhmä = ryhmä,
      näyttö = None,
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(arvosana = arvosana, date(2014, 10, 20)))),
      vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 5, 31), stadinAmmattiopisto)
    )
  }

  def osittaisenTutkinnonTutkinnonOsanSuoritus(arvosana: Koodistokoodiviite, ryhmä: Option[Koodistokoodiviite], koodi: String, nimi: String, laajuus: Int): MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = {
    val osa = MuuValtakunnallinenTutkinnonOsa(tunniste = Koodistokoodiviite(koodi, Some(nimi), "tutkinnonosat"), true, Some(LaajuusOsaamispisteissä(laajuus)))
    MuunOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(
      koulutusmoduuli = osa,
      tutkinnonOsanRyhmä = ryhmä,
      näyttö = None,
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(arvosana = arvosana, date(2014, 10, 20)))),
      vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 5, 31), stadinAmmattiopisto)
    )
  }

  def yhteisenOsittaisenTutkinnonTutkinnonOsansuoritus(arvosana: Koodistokoodiviite, ryhmä: Option[Koodistokoodiviite], koodi: String, nimi: String, laajuus: Int): YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus = {
    val osa = YhteinenTutkinnonOsa(tunniste = Koodistokoodiviite(koodi, Some(nimi), "tutkinnonosat"), true, Some(LaajuusOsaamispisteissä(laajuus)))
    YhteisenOsittaisenAmmatillisenTutkinnonTutkinnonosanSuoritus(
      koulutusmoduuli = osa,
      tutkinnonOsanRyhmä = ryhmä,
      näyttö = None,
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(arvosana = arvosana, date(2014, 10, 20)))),
      vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 5, 31), stadinAmmattiopisto)
    )
  }

  val opiskeluoikeudenLisätiedot = AmmatillisenOpiskeluoikeudenLisätiedot(
    hojks = Some(Hojks(
      opetusryhmä = Koodistokoodiviite("1", Some("Yleinen opetusryhmä"), "opetusryhma")
    )),
    oikeusMaksuttomaanAsuntolapaikkaan = None,
    ulkomaanjaksot = Some(List(Ulkomaanjakso(date(2012, 9, 1), Some(date(2013, 9, 1)), ruotsi, "Harjoittelua ulkomailla"))),
    vaikeastiVammainen = Some(List(Aikajakso(date(2012, 9, 1), Some(date(2013, 9, 1))))),
    vammainenJaAvustaja = Some(List(Aikajakso(date(2012, 9, 1), Some(date(2013, 9, 1))))),
    majoitus = Some(List(Aikajakso(date(2012, 9, 1), Some(date(2013, 9, 1))))),
    sisäoppilaitosmainenMajoitus = Some(List(Aikajakso(date(2012, 9, 1), Some(date(2013, 9, 1))))),
    vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = Some(List(Aikajakso(date(2012, 9, 1), Some(date(2013, 9, 1))))),
    henkilöstökoulutus = true,
    vankilaopetuksessa = Some(List(Aikajakso(date(2013, 9, 2), None))),
    osaAikaisuusjaksot = Some(List(OsaAikaisuusJakso(date(2012, 9, 1), None, 80))),
    opiskeluvalmiuksiaTukevatOpinnot = Some(List(OpiskeluvalmiuksiaTukevienOpintojenJakso(date(2013, 10, 1), date(2013, 10, 31), "Opiskeluvalmiuksia tukevia opintoja")))
  )

  val opiskeluoikeudenOrganisaatioHistoria = List(
    OpiskeluoikeudenOrganisaatiohistoria(
      muutospäivä = date(2002, 2, 2),
      Some(Oppilaitos(
        oid = MockOrganisaatiot.ressunLukio,
        nimi = Some(Finnish(fi = "Ressun lukio"))
      )),
      Some(Koulutustoimija(
        oid = MockOrganisaatiot.helsinginKaupunki,
        nimi = Some(Finnish(fi = "Helsingin kaupunki"))
      ))
    ),
    OpiskeluoikeudenOrganisaatiohistoria(
      muutospäivä = date(2005, 5, 5),
      Some(Oppilaitos(
        oid = MockOrganisaatiot.stadinAmmattiopisto,
        nimi = Some(Finnish(fi = "Stadin ammatti- ja aikuisopisto"))
      )),
      Some(Koulutustoimija(
        oid = MockOrganisaatiot.helsinginKaupunki,
        nimi = Some(Finnish(fi = "Helsingin kaupunki"))
      ))
    )
  )

  def perustutkintoOpiskeluoikeusValmis(oppilaitos: Oppilaitos = stadinAmmattiopisto, toimipiste: OrganisaatioWithOid = stadinToimipiste) = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    oppilaitos = Some(oppilaitos),
    suoritukset = List(ympäristöalanPerustutkintoValmis(toimipiste)),
    lisätiedot = None,
    tila = AmmatillinenOpiskeluoikeudenTila(
      List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
        AmmatillinenOpiskeluoikeusjakso(date(2016, 5, 31), opiskeluoikeusValmistunut, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
      )
    )
  )

  def perustutkintoOpiskeluoikeusValmisVahvistettuKoulutustoimijalla(oppilaitos: Oppilaitos = stadinAmmattiopisto, toimipiste: OrganisaatioWithOid = stadinToimipiste) =
    perustutkintoOpiskeluoikeusValmis(oppilaitos, toimipiste).copy(
      suoritukset = List(ympäristöalanPerustutkintoValmis(toimipiste).copy(
        vahvistus = vahvistus(date(2016, 5, 31), länsirannikonKoulutusOy, Some(helsinki)),
        keskiarvo = Some(4.0)
      )),
    )

  def perustutkintoOpiskeluoikeusValmisVahvistettuYrityksessä(oppilaitos: Oppilaitos = stadinAmmattiopisto, toimipiste: OrganisaatioWithOid = stadinToimipiste) =
    perustutkintoOpiskeluoikeusValmis(oppilaitos, toimipiste).copy(
      suoritukset = List(ympäristöalanPerustutkintoValmis(toimipiste).copy(
        vahvistus = vahvistus(date(2016, 5, 31), autokorjaamoOy, Some(helsinki)),
        keskiarvo = Some(4.0)
      ))
    )

  def perustutkintoOpiskeluoikeusKesken(oppilaitos: Oppilaitos = stadinAmmattiopisto, toimipiste: OrganisaatioWithOid = stadinToimipiste) = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    oppilaitos = Some(oppilaitos),
    suoritukset = List(ympäristöalanPerustutkintoKesken(toimipiste).copy(keskiarvo = None, vahvistus = None)),
    tila = AmmatillinenOpiskeluoikeudenTila(
      List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
      )
    ),
    lisätiedot = Some(opiskeluoikeudenLisätiedot)
  )

  def puuteollisuusOpiskeluoikeusKesken(oppilaitos: Oppilaitos = stadinAmmattiopisto, toimipiste: OrganisaatioWithOid = stadinToimipiste) = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    oppilaitos = Some(oppilaitos),
    suoritukset = List(puuteollisuudenPerustutkinnonSuoritus(toimipiste)),
    tila = AmmatillinenOpiskeluoikeudenTila(
      List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
      )
    ),
    lisätiedot = Some(opiskeluoikeudenLisätiedot)
  )

  def sosiaaliJaTerveysalaOpiskeluoikeus(oppilaitos: Oppilaitos = stadinAmmattiopisto, toimipiste: OrganisaatioWithOid = stadinToimipiste) = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(date(2016, 8, 1), opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(
      AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto,
        suoritustapa = suoritustapaNäyttö,
        järjestämismuodot = Some(List(
          Järjestämismuotojakso(date(2014, 8, 1), None, järjestämismuotoOppilaitos),
          Järjestämismuotojakso(date(2015, 5, 31), None, järjestämismuotoOppisopimus),
          Järjestämismuotojakso(date(2016, 3, 31), None, järjestämismuotoOppilaitos)
        )),
        suorituskieli = suomenKieli,
        alkamispäivä = None,
        toimipiste = stadinToimipiste,
        vahvistus = vahvistus(date(2016, 5, 31), stadinAmmattiopisto, Some(helsinki)),
        osasuoritukset = Some(List(
          tutkinnonOsanSuoritus("100832", "Kasvun tukeminen ja ohjaus", ammatillisetTutkinnonOsat, hyväksytty),
          tutkinnonOsanSuoritus("100833", "Hoito ja huolenpito", ammatillisetTutkinnonOsat, hyväksytty),
          tutkinnonOsanSuoritus("100834", "Kuntoutumisen tukeminen", ammatillisetTutkinnonOsat, hyväksytty),
          tutkinnonOsanSuoritus("100840", "Lasten ja nuorten hoito ja kasvatus", ammatillisetTutkinnonOsat, hyväksytty)
        ))
      )
    )
  )

  def sosiaaliJaTerveysalaOpiskeluoikeusKesken(oppilaitos: Oppilaitos = stadinAmmattiopisto, toimipiste: OrganisaatioWithOid = stadinToimipiste) = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    oppilaitos = Some(oppilaitos),
    suoritukset = List(sosiaaliJaTerveysalanPerustutkinnonSuoritusKesken(toimipiste)),
    tila = AmmatillinenOpiskeluoikeudenTila(
      List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
      )
    ),
    lisätiedot = Some(opiskeluoikeudenLisätiedot)
  )

  val lisätietoMuutosArviointiasteikossa = AmmatillisenTutkinnonOsanLisätieto(Koodistokoodiviite("muutosarviointiasteikossa", "ammatillisentutkinnonosanlisatieto"),
    "Tutkinnon osa on koulutuksen järjestäjän päätöksellä arvioitu asteikolla hyväksytty/hylätty.")

  val lisätietoOsaamistavoitteet = AmmatillisenTutkinnonOsanLisätieto(Koodistokoodiviite("mukautettu", "ammatillisentutkinnonosanlisatieto"),
    "Tutkinnon osan ammattitaitovaatimuksia tai osaamistavoitteita ja osaamisen arviointia on mukautettu ammatillisesta peruskoulutuksesta annetun lain (630/1998, muutos 246/2015) 19 a tai 21 §:n perusteella"
  )

  val työssäoppiminenSorttiAsemalla = Some(List(
    työssäoppimisjakso
  ))

  lazy val työssäoppimisjakso = Työssäoppimisjakso(date(2014, 1, 1), Some(date(2014, 3, 15)), Some("Sortti-asema"), jyväskylä, suomi, Some(LocalizedString.finnish("Toimi harjoittelijana Sortti-asemalla")), LaajuusOsaamispisteissä(5))
  lazy val koulutussopimusjakso = Koulutussopimusjakso(date(2014, 1, 1), Some(date(2014, 3, 15)), Some("Sortti-asema"), Some("1572860-0"), jyväskylä, suomi, Some(LocalizedString.finnish("Toimi harjoittelijana Sortti-asemalla")))

  def ympäristöalanPerustutkintoValmis(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = {
    AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = AmmatillinenTutkintoKoulutus(
        Koodistokoodiviite("361902", Some("Luonto- ja ympäristöalan perustutkinto"), "koulutus", None),
        Some("62/011/2014")
      ),
      työssäoppimisjaksot = työssäoppiminenSorttiAsemalla,
      tutkintonimike = Some(List(Koodistokoodiviite("10083", Some("Ympäristönhoitaja"), "tutkintonimikkeet", None))),
      osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1590", Some("Ympäristöalan osaamisala"), "osaamisala", None)))),
      suoritustapa = suoritustapaOps,
      järjestämismuodot = Some(List(Järjestämismuotojakso(date(2013, 9, 1), None, järjestämismuotoOppilaitos))),
      suorituskieli = suomenKieli,
      alkamispäivä = None,
      toimipiste = toimipiste,
      vahvistus = vahvistus(date(2016, 5, 31), stadinAmmattiopisto, Some(helsinki)),
      ryhmä = Some("YMP14SN"),
      keskiarvo = Some(4.0),
      osasuoritukset = Some(List(
        tutkinnonOsanSuoritus("100431", "Kestävällä tavalla toimiminen", ammatillisetTutkinnonOsat, k3, 40).copy(arviointi = Some(List(arviointi(k3).copy(päivä = date(2015, 1, 1))))),
        tutkinnonOsanSuoritus("100432", "Ympäristön hoitaminen", ammatillisetTutkinnonOsat, k3, 35).copy(näyttö = Some(
          näyttö(date(2016, 2, 1), "Muksulan päiväkodin ympäristövaikutusten arvioiminen ja ympäristön kunnostustöiden\ntekeminen sekä mittauksien tekeminen ja näytteiden ottaminen", "Muksulan päiväkoti, Kaarinan kunta", Some(näytönArviointi)))
        ),
        tutkinnonOsanSuoritus("100439", "Uusiutuvien energialähteiden hyödyntäminen", ammatillisetTutkinnonOsat, k3, 15),
        tutkinnonOsanSuoritus("100442", "Ulkoilureittien rakentaminen ja hoitaminen", ammatillisetTutkinnonOsat, k3, 15),
        tutkinnonOsanSuoritus("100443", "Kulttuuriympäristöjen kunnostaminen ja hoitaminen", ammatillisetTutkinnonOsat, k3, 15).copy(näyttö = Some(
          näyttö(date(2016, 3, 1), "Sastamalan kunnan kulttuuriympäristöohjelmaan liittyvän Wanhan myllyn lähiympäristön\nkasvillisuuden kartoittamisen sekä ennallistamisen suunnittelu ja toteutus", "Sastamalan kunta", Some(näytönArviointi)))
        ),
        tutkinnonOsanSuoritus("100447", "Vesistöjen kunnostaminen ja hoitaminen", ammatillisetTutkinnonOsat, hyväksytty, 15).copy(
          näyttö = Some(näyttö(date(2016, 4, 1), "Uimarin järven tilan arviointi ja kunnostus", "Vesipojat Oy", Some(näytönArviointi))),
          lisätiedot = Some(List(lisätietoMuutosArviointiasteikossa)),
          osasuoritukset = Some(List(
            AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
              AmmatillisenTutkinnonOsaaPienempiKokonaisuus(PaikallinenKoodi("htm", "Hoitotarpeen määrittäminen"), "Hoitotarpeen määrittäminen"),
              arviointi = Some(List(arviointiHyväksytty))
            )
          ))
        ),
        yhteisenTutkinnonOsanSuoritus("101053", "Viestintä- ja vuorovaikutusosaaminen", k3, 11).copy(
          osasuoritukset = Some(List(
            YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(5))), arviointi = Some(List(arviointiKiitettävä.copy(kuvaus = Some(LocalizedString.finnish("Testikuvaus")))))),
            YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = false, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(3))), arviointi = Some(List(arviointiKiitettävä))),
            YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(Koodistokoodiviite("TK1", "ammatillisenoppiaineet"), Koodistokoodiviite("SV", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(1))), arviointi = Some(List(arviointiKiitettävä))),
            YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(Koodistokoodiviite("VK", "ammatillisenoppiaineet"), Koodistokoodiviite("EN", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(2))), arviointi = Some(List(arviointiKiitettävä)))
          ))
        ),
        yhteisenTutkinnonOsanSuoritus("101054", "Matemaattis-luonnontieteellinen osaaminen", k3, 9).copy(
          lisätiedot = Some(List(lisätietoOsaamistavoitteet)),
          osasuoritukset = Some(List(
            YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = PaikallinenAmmatillisenTutkinnonOsanOsaAlue(PaikallinenKoodi("MA", "Matematiikka"), "Matematiikan opinnot", pakollinen = true, Some(LaajuusOsaamispisteissä(3))), arviointi = Some(List(arviointiKiitettävä))),
            YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("FK", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(3))), arviointi = Some(List(arviointiKiitettävä))),
            YhteisenTutkinnonOsanOsaAlueenSuoritus(
              koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("TVT", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(3))),
              arviointi = Some(List(arviointiKiitettävä.copy(päivä = date(2015, 1, 1)))),
              alkamispäivä = Some(date(2014, 1, 1)),
              tunnustettu = Some(tunnustettu),
              lisätiedot = Some(List(lisätietoOsaamistavoitteet))
            )
          ))
        ),
        yhteisenTutkinnonOsanSuoritus("101055", "Yhteiskunnassa ja työelämässä tarvittava osaaminen", k3, 8).copy(
          osasuoritukset = Some(List(
            YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = PaikallinenAmmatillisenTutkinnonOsanOsaAlue(PaikallinenKoodi("YTT", "Yhteiskuntatieto"), "Yhteiskuntaopin opinnot", pakollinen = true, Some(LaajuusOsaamispisteissä(8))), arviointi = Some(List(arviointiKiitettävä))),
          ))
        ),
        yhteisenTutkinnonOsanSuoritus("101056", "Sosiaalinen ja kulttuurinen osaaminen", k3, 7).copy(
          osasuoritukset = Some(List(
            YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = PaikallinenAmmatillisenTutkinnonOsanOsaAlue(PaikallinenKoodi("SKO", "Sosiaalitaito"), "Vuorotaitovaikutuksen kurssi", pakollinen = true, Some(LaajuusOsaamispisteissä(7))), arviointi = Some(List(arviointiKiitettävä))),
          ))
        ),

        paikallisenTutkinnonOsanSuoritus("enkku3", "Matkailuenglanti", yksilöllisestiLaajentavatTutkinnonOsat, k3, 5),
        paikallisenTutkinnonOsanSuoritus("soskultos1", "Sosiaalinen ja kulttuurinen osaaminen", vapaavalintaisetTutkinnonOsat, k3, 5)
      ).map(_.toimipisteellä(toimipiste)))
    )
  }

  def ympäristöalanPerustutkintoKesken(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = {
    AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = AmmatillinenTutkintoKoulutus(
        Koodistokoodiviite("361902", Some("Luonto- ja ympäristöalan perustutkinto"), "koulutus", None),
        Some("62/011/2014")
      ),
      tutkintonimike = Some(List(Koodistokoodiviite("10083", Some("Ympäristönhoitaja"), "tutkintonimikkeet", None))),
      osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1590", Some("Ympäristöalan osaamisala"), "osaamisala", None)))),
      suoritustapa = suoritustapaOps,
      järjestämismuodot = Some(List(Järjestämismuotojakso(date(2012, 9, 1), None, järjestämismuotoOppilaitos))),
      suorituskieli = suomenKieli,
      alkamispäivä = None,
      toimipiste = toimipiste,
      työssäoppimisjaksot = työssäoppiminenSorttiAsemalla,
      osasuoritukset = Some(List(
        tutkinnonOsanSuoritus("100431", "Kestävällä tavalla toimiminen", ammatillisetTutkinnonOsat, k3, 40),
        tutkinnonOsanSuoritus("100432", "Ympäristön hoitaminen", ammatillisetTutkinnonOsat, k3, 35),
        tutkinnonOsanSuoritus("100439", "Uusiutuvien energialähteiden hyödyntäminen", ammatillisetTutkinnonOsat, k3, 15),
        tutkinnonOsanSuoritus("100442", "Ulkoilureittien rakentaminen ja hoitaminen", ammatillisetTutkinnonOsat, None),
        tutkinnonOsanSuoritus("100443", "Kulttuuriympäristöjen kunnostaminen ja hoitaminen", ammatillisetTutkinnonOsat, None),
        paikallisenTutkinnonOsanSuoritus("enkku3", "Matkailuenglanti", vapaavalintaisetTutkinnonOsat, k3, 5)
      ).map(_.toimipisteellä(toimipiste)))
    )
  }

  def sosiaaliJaTerveysalanPerustutkinnonSuoritusKesken(toimipiste: OrganisaatioWithOid = stadinToimipiste): AmmatillisenTutkinnonSuoritus = {
    AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto,
      tutkintonimike = Some(List(Koodistokoodiviite("10008", Some("Lähihoitaja"), "tutkintonimikkeet", None))),
      osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1509", Some("Lasten ja nuorten hoidon ja kasvatuksen osaamisala"), "osaamisala", None)))),
      suoritustapa = suoritustapaOps,
      järjestämismuodot = Some(List(Järjestämismuotojakso(date(2012, 9, 1), None, järjestämismuotoOppilaitos))),
      suorituskieli = suomenKieli,
      alkamispäivä = None,
      toimipiste = toimipiste,
      työssäoppimisjaksot = työssäoppiminenSorttiAsemalla,
      osasuoritukset = Some(List(
        tutkinnonOsanSuoritus("100832", "Kasvun tukeminen ja ohjaus", ammatillisetTutkinnonOsat, hyväksytty),
        tutkinnonOsanSuoritus("100833", "Hoito ja huolenpito", ammatillisetTutkinnonOsat, None),
        tutkinnonOsanSuoritus("100834", "Kuntoutumisen tukeminen", ammatillisetTutkinnonOsat, None)
      ).map(_.toimipisteellä(toimipiste)))
    )
  }

  def ammatillisenTutkinnonOsittainenSuoritus = AmmatillisenTutkinnonOsittainenSuoritus(
    koulutusmoduuli = AmmatillinenTutkintoKoulutus(
      Koodistokoodiviite("361902", Some("Luonto- ja ympäristöalan perustutkinto"), "koulutus", None),
      Some("62/011/2014")
    ),
    tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
    toinenTutkintonimike = true,
    osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None)))),
    toinenOsaamisala = false,
    suoritustapa = suoritustapaOps,
    järjestämismuodot = Some(List(Järjestämismuotojakso(date(2012, 9, 1), None, järjestämismuotoOppilaitos))),
    suorituskieli = suomenKieli,
    keskiarvo = Some(4.0),
    vahvistus = vahvistus(),
    alkamispäivä = None,
    toimipiste = stadinToimipiste,
    osasuoritukset = Some(List(
      osittaisenTutkinnonTutkinnonOsanSuoritus(k3, ammatillisetTutkinnonOsat, "100432", "Ympäristön hoitaminen", 35)
    )),
    todistuksellaNäkyvätLisätiedot = Some("Suorittaa toista osaamisalaa")
  )

  def ammatillisenTutkinnonOsittainenAutoalanSuoritus = AmmatillisenTutkinnonOsittainenSuoritus(
    koulutusmoduuli = AmmatillinenTutkintoKoulutus(
      Koodistokoodiviite("361902", Some("Autoalan perustutkinto"), "koulutus", None),
      Some("62/011/2014")
    ),
    tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
    toinenTutkintonimike = true,
    osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None)))),
    toinenOsaamisala = false,
    suoritustapa = suoritustapaReformi,
    järjestämismuodot = Some(List(Järjestämismuotojakso(date(2012, 9, 1), None, järjestämismuotoOppilaitos))),
    suorituskieli = suomenKieli,
    keskiarvo = Some(4.0),
    vahvistus = vahvistus(date(2016, 5, 31), stadinAmmattiopisto, Some(helsinki)),
    alkamispäivä = None,
    toimipiste = stadinToimipiste,
    osasuoritukset = Some(List(
      osittaisenTutkinnonTutkinnonOsanSuoritus(h2, ammatillisetTutkinnonOsat, "100001", "Audiovisuaalisen tuotannon toteuttaminen", 20).copy(
        tunnustettu = Some(tunnustettu),
        arviointi = Some(List(arviointi(h2).copy(päivä = date(2015, 1, 1))))
      ),
      osittaisenTutkinnonTutkinnonOsanSuoritus(h2, ammatillisetTutkinnonOsat, "100003", "Paikallinen kurssi", 3).copy(vahvistus =  None),
      osittaisenTutkinnonTutkinnonOsanSuoritus(k3, ammatillisetTutkinnonOsat, "100002", "Televisiotuotanto", 25).copy(
        tunnustettu = Some(tunnustettu.copy(rahoituksenPiirissä = true)),
        osasuoritukset = Some(List(
          AmmatillisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
            AmmatillisenTutkinnonOsaaPienempiKokonaisuus(PaikallinenKoodi("htm", "Hoitotarpeen määrittäminen"), "Hoitotarpeen määrittäminen"),
            arviointi = Some(List(arviointiHyväksytty))
          )
        ))
      ),
      osittaisenTutkinnonTutkinnonOsanSuoritus(k3, ammatillisetTutkinnonOsat, "100432", "Ympäristön hoitaminen", 30).copy(näyttö = Some(
        näyttö(date(2016, 2, 1), "Muksulan päiväkodin ympäristövaikutusten arvioiminen ja ympäristön kunnostustöiden\ntekeminen sekä mittauksien tekeminen ja näytteiden ottaminen", "Muksulan päiväkoti, Kaarinan kunta", Some(näytönArviointi)))
      ),
      yhteisenOsittaisenTutkinnonTutkinnonOsansuoritus(h2, yhteisetTutkinnonOsat, "101053", "Viestintä- ja vuorovaikutusosaaminen", 14).copy(
        osasuoritukset = Some(List(
          YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(5))), arviointi = Some(List(arviointiKiitettävä))),
          YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = false, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(3))), arviointi = Some(List(arviointiKiitettävä))),
          YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(Koodistokoodiviite("TK1", "ammatillisenoppiaineet"), Koodistokoodiviite("SV", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(1))), arviointi = Some(List(arviointiKiitettävä))),
          YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(Koodistokoodiviite("VK", "ammatillisenoppiaineet"), Koodistokoodiviite("EN", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(2))), arviointi = Some(List(arviointiKiitettävä))),
          YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("PS", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(1))), arviointi = Some(List(arviointiHylätty))),
          YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(Koodistokoodiviite("VVTK", "ammatillisenoppiaineet"), Koodistokoodiviite("EN", "kielivalikoima"), pakollinen = true, Some(LaajuusOsaamispisteissä(2))), arviointi = Some(List(arviointiKiitettävä)))
        ))),
      yhteisenOsittaisenTutkinnonTutkinnonOsansuoritus(k3, yhteisetTutkinnonOsat, "101054", "Matemaattis-luonnontieteellinen osaaminen", 9).copy(
        lisätiedot = Some(List(lisätietoOsaamistavoitteet)),
        tunnustettu = Some(tunnustettu.copy(rahoituksenPiirissä = true)),
        osasuoritukset = Some(List(
          YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = PaikallinenAmmatillisenTutkinnonOsanOsaAlue(PaikallinenKoodi("MA", "Matematiikka"), "Matematiikan opinnot", pakollinen = true, Some(LaajuusOsaamispisteissä(3))), arviointi = Some(List(arviointiKiitettävä))),
          YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("FK", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(3))), arviointi = Some(List(arviointiKiitettävä))).copy(
            tunnustettu = Some(tunnustettu)
          ),
          YhteisenTutkinnonOsanOsaAlueenSuoritus(
            koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("TVT", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(3))),
            arviointi = Some(List(arviointiKiitettävä)),
            alkamispäivä = Some(date(2014, 1, 1)),
            lisätiedot = Some(List(lisätietoOsaamistavoitteet))
          )
        ))
      ),
      OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(
        koulutusmoduuli = KorkeakouluopinnotTutkinnonOsa(),
        osasuoritukset = Some(List(AmmatillinenReforminMukainenPerustutkintoExample.saksa.copy(arviointi = Some(List(arviointiKiitettävä)))))
      ),
      OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(koulutusmoduuli = JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa(), osasuoritukset = Some(List(
        LukioOpintojenSuoritus(
          koulutusmoduuli = PaikallinenLukionOpinto(
            tunniste = PaikallinenKoodi("MAA", "Maantieto"),
            kuvaus = "Lukion maantiedon oppimäärä",
            perusteenDiaarinumero = "33/011/2003"
          ),
          arviointi = Some(List(arviointiKiitettävä)),
          tyyppi = Koodistokoodiviite(koodiarvo = "ammatillinenlukionopintoja", koodistoUri = "suorituksentyyppi")
        ),
        LukioOpintojenSuoritus(
          koulutusmoduuli = PaikallinenLukionOpinto(
            tunniste = PaikallinenKoodi("EN", "Englanti"),
            kuvaus = "Englannin kurssi",
            laajuus = Some(LaajuusOsaamispisteissä(3)),
            perusteenDiaarinumero = "33/011/2003"
          ),
          arviointi = Some(List(arviointiKiitettävä)),
          tyyppi = Koodistokoodiviite(koodiarvo = "ammatillinenlukionopintoja", koodistoUri = "suorituksentyyppi")
        ),
        YhteisenTutkinnonOsanOsaAlueenSuoritus(
          koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("TVT", "ammatillisenoppiaineet"),
            pakollinen = true, Some(LaajuusOsaamispisteissä(3))
          ),
          arviointi = Some(List(arviointiKiitettävä))
        ),
        MuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
          PaikallinenOpintovalmiuksiaTukevaOpinto(PaikallinenKoodi("htm", "Hoitotarpeen määrittäminen"), "Hoitotarpeen määrittäminen"),
          arviointi = Some(List(arviointiKiitettävä))
        )
      )))
    )),
    todistuksellaNäkyvätLisätiedot = Some("Suorittaa toista osaamisalaa")
  )

  def muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(tunniste: PaikallinenKoodi, kuvaus: String, osasuoritukset: Option[List[MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus]] = None, laajuus: Option[LaajuusKaikkiYksiköt] = None) =
    MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
      MuunAmmatillisenKoulutuksenOsasuoritus(
        tunniste,
        laajuus,
        LocalizedString.finnish(kuvaus)
      ),
      alkamispäivä = None,
      arviointi = Some(List(
        MuunAmmatillisenKoulutuksenArviointi(
          arvosana = Koodistokoodiviite("Suoritettu", "arviointiasteikkomuuammatillinenkoulutus"),
          date(2018, 5, 31),
          arvioitsijat = Some(List(Arvioitsija("Aarne Arvioija")))
        )
      )),
      suorituskieli = None,
      näyttö = None,
      osasuoritukset = osasuoritukset
    )

  def ammatillisenTutkinnonSuoritusKorotetuillaOsasuorituksilla() =
    AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto,
      suoritustapa = suoritustapaOps,
      järjestämismuodot = Some(List(
        Järjestämismuotojakso(date(2015, 1, 1), None, järjestämismuotoOppilaitos),
      )),
      suorituskieli = suomenKieli,
      alkamispäivä = None,
      toimipiste = stadinToimipiste,
      vahvistus = vahvistus(date(2016, 1, 1), stadinAmmattiopisto, Some(helsinki)),
      keskiarvo = Some(4.0),
      osasuoritukset = Some(List(
        tutkinnonOsanSuoritus("100832", "Kasvun tukeminen ja ohjaus", ammatillisetTutkinnonOsat, hylätty, 2).copy(
          arviointi = Some(List(
            AmmatillinenArviointi(arvosana = hylätty, date(2015, 1, 1)),
            AmmatillinenArviointi(arvosana = hyväksytty, date(2016, 1, 1))
          )),
          vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 1, 1), stadinAmmattiopisto),
          tunnustettu = Some(tunnustettu.copy(
            rahoituksenPiirissä = true
          )),
          näyttö = Some(
            näyttö(date(2016, 1, 1), "Muksulan päiväkodin ympäristövaikutusten arvioiminen ja ympäristön kunnostustöiden\ntekeminen sekä mittauksien tekeminen ja näytteiden ottaminen", "Muksulan päiväkoti, Kaarinan kunta", Some(näytönArviointi))
          )
        ),
        tutkinnonOsanSuoritus("100833", "Hoito ja huolenpito", ammatillisetTutkinnonOsat, hylätty, laajuus = Some(2.0f), pakollinen = false).copy(
          arviointi = Some(List(
            AmmatillinenArviointi(arvosana = hylätty, date(2015, 1, 1)),
            AmmatillinenArviointi(arvosana = hyväksytty, date(2016, 1, 1))
          )),
          vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 1, 1), stadinAmmattiopisto),
        ),
        tutkinnonOsanSuoritus("100834", "Kuntoutumisen tukeminen", vapaavalintaisetTutkinnonOsat, hylätty, 2).copy(
          arviointi = Some(List(
            AmmatillinenArviointi(arvosana = hylätty, date(2015, 1, 1)),
            AmmatillinenArviointi(arvosana = hyväksytty, date(2016, 1, 1))
          )),
          vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 1, 1), stadinAmmattiopisto)
        ),
        tutkinnonOsanSuoritus("100835", "Asiakaspalvelu ja tietohallinta", yksilöllisestiLaajentavatTutkinnonOsat, hylätty, 2).copy(
          arviointi = Some(List(
            AmmatillinenArviointi(arvosana = hylätty, date(2015, 1, 1)),
            AmmatillinenArviointi(arvosana = hyväksytty, date(2016, 1, 1))
          )),
          vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 1, 1), stadinAmmattiopisto)
        ),
        yhteisenTutkinnonOsanSuoritus("101053", "Viestintä- ja vuorovaikutusosaaminen", k3, 10).copy(
          osasuoritukset = Some(List(
            YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"),
              pakollinen = true,
              kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"),
              laajuus = Some(LaajuusOsaamispisteissä(5))),
              arviointi = Some(List(
                AmmatillinenArviointi(arvosana = hylätty, date(2015, 1, 1)),
                AmmatillinenArviointi(arvosana = hyväksytty, date(2016, 1, 1))
              ))),
            YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"),
              pakollinen = false,
              kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"),
              laajuus = Some(LaajuusOsaamispisteissä(5))),
              arviointi = Some(List(
                AmmatillinenArviointi(arvosana = hylätty, date(2015, 1, 1)),
                AmmatillinenArviointi(arvosana = hyväksytty, date(2016, 1, 1))
              )),
              tunnustettu = Some(tunnustettu.copy(
                rahoituksenPiirissä = true
              ))),
          )),
          vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 1, 1), stadinAmmattiopisto),
          tunnustettu = Some(tunnustettu.copy(
            rahoituksenPiirissä = true
          ))
        ))
      )
    )

  def ammatillisenTutkinnonOsittainenSuoritusKorotetuillaOsasuorituksilla() =
    AmmatillisenTutkinnonOsittainenSuoritus(
      koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto,
      suoritustapa = suoritustapaOps,
      järjestämismuodot = Some(List(
        Järjestämismuotojakso(date(2015, 1, 1), None, järjestämismuotoOppilaitos),
      )),
      suorituskieli = suomenKieli,
      alkamispäivä = None,
      toimipiste = stadinToimipiste,
      vahvistus = vahvistus(date(2016, 1, 1), stadinAmmattiopisto, Some(helsinki)),
      keskiarvo = Some(4.0),
      osasuoritukset = Some(List(
        osittaisenTutkinnonTutkinnonOsanSuoritus(h2, ammatillisetTutkinnonOsat, "100001", "Audiovisuaalisen tuotannon toteuttaminen", 2).copy(
          arviointi = Some(List(
            AmmatillinenArviointi(arvosana = hylätty, date(2015, 1, 1)),
            AmmatillinenArviointi(arvosana = hyväksytty, date(2016, 1, 1))
          )),
          vahvistus = vahvistusValinnaisellaTittelillä(date(2016, 1, 1), stadinAmmattiopisto),
          tunnustettu = Some(tunnustettu.copy(
            rahoituksenPiirissä = true
          )),
          näyttö = Some(
            näyttö(date(2016, 1, 1), "Muksulan päiväkodin ympäristövaikutusten arvioiminen ja ympäristön kunnostustöiden\ntekeminen sekä mittauksien tekeminen ja näytteiden ottaminen", "Muksulan päiväkoti, Kaarinan kunta", Some(näytönArviointi))
          )),
        )
      )
    )

  def ammatillinenOpiskeluoikeusNäyttötutkinnonJaNäyttöönValmistavanSuorituksilla(
    vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
    tutkinnonOsasuoritukset: Option[List[AmmatillisenTutkinnonOsanSuoritus]] = None) = {
    val näyttötutkinnonSuoritus = AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = sosiaaliJaTerveysalanPerustutkinto,
      suoritustapa = suoritustapaNäyttö,
      suorituskieli = suomenKieli,
      toimipiste = stadinToimipiste,
      vahvistus = vahvistus,
      osasuoritukset = tutkinnonOsasuoritukset
    )
    val näyttötutkintoonValmistavaSuoritus = AmmattitutkintoExample.näyttötutkintoonValmistavanKoulutuksenSuoritus.copy(alkamispäivä = Some(date(2015, 1, 1)), vahvistus = vahvistus)

    AmmatillinenExampleData.sosiaaliJaTerveysalaOpiskeluoikeus().copy(
      tila = AmmatillinenOpiskeluoikeudenTila(List(
        AmmatillinenOpiskeluoikeusjakso(date(2015, 1, 1), ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      )),
      suoritukset = List(näyttötutkinnonSuoritus, näyttötutkintoonValmistavaSuoritus)
    )
  }
}
