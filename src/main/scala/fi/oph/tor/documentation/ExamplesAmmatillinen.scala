package fi.oph.tor.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import ExampleData._
import fi.oph.tor.localization.LocalizedStringImplicits._
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.schema._

object AmmatillinenExampleData {
  val exampleHenkilö = MockOppijat.ammattilainen.vainHenkilötiedot

  def tutkintoSuoritus(tutkintoKoulutus: AmmatillinenTutkintoKoulutus,
                               tutkintonimike: Option[List[Koodistokoodiviite]] = None,
                               osaamisala: Option[List[Koodistokoodiviite]] = None,
                               suoritustapa: Option[Suoritustapa] = None,
                               järjestämismuoto: Option[Järjestämismuoto] = None,
                               paikallinenId: Option[String] = None,
                               suorituskieli: Option[Koodistokoodiviite] = None,
                               tila: Koodistokoodiviite,
                               alkamisPäivä: Option[LocalDate] = None,
                               toimipiste: OrganisaatioWithOid,
                               arviointi: Option[List[AmmatillinenArviointi]] = None,
                               vahvistus: Option[Vahvistus] = None,
                               osasuoritukset: Option[List[AmmatillisenTutkinnonosanSuoritus]] = None): AmmatillisenTutkinnonSuoritus =

    AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = tutkintoKoulutus,
      tutkintonimike,
      osaamisala = osaamisala,
      suoritustapa = suoritustapa,
      järjestämismuoto = järjestämismuoto,
      paikallinenId,
      suorituskieli,
      tila = tila,
      alkamispäivä = alkamisPäivä,
      toimipiste = toimipiste,
      arviointi = arviointi,
      vahvistus = vahvistus,
      osasuoritukset = osasuoritukset)

  lazy val autoalanPerustutkinto: AmmatillisenTutkinnonSuoritus = tutkintoSuoritus(
    tutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus"), Some("39/011/2014")),
    tutkintonimike = None,
    osaamisala = None,
    suoritustapa = None,
    järjestämismuoto = None,
    paikallinenId = Some("suoritus-12345"),
    suorituskieli = None,
    tila = tilaKesken,
    alkamisPäivä = Some(date(2016, 9, 1)),
    toimipiste = toimipiste,
    arviointi = None,
    vahvistus = None,
    osasuoritukset = None
  )

  lazy val h2: Koodistokoodiviite = Koodistokoodiviite("2", Some("H2"), "arviointiasteikkoammatillinent1k3", None)
  lazy val k3: Koodistokoodiviite = Koodistokoodiviite("3", Some("K3"), "arviointiasteikkoammatillinent1k3", None)
  lazy val näytönArviointi = NäytönArviointi(List(
    NäytönArviointikohde(Koodistokoodiviite("1", Some("Työprosessin hallinta"), "ammatillisennaytonarviointikohde", None), k3),
    NäytönArviointikohde(Koodistokoodiviite("2", Some("Työmenetelmien, -välineiden ja materiaalin hallinta"), "ammatillisennaytonarviointikohde", None), h2),
    NäytönArviointikohde(Koodistokoodiviite("3", Some("Työn perustana olevan tiedon hallinta"), "ammatillisennaytonarviointikohde", None), h2),
    NäytönArviointikohde(Koodistokoodiviite("4", Some("Elinikäisen oppimisen avaintaidot"), "ammatillisennaytonarviointikohde", None), k3)),
    Koodistokoodiviite("1", Some("Opettaja"), "ammatillisennaytonarvioinnistapaattaneet", None),
    Koodistokoodiviite("1", Some("Opiskelija ja opettaja"), "ammatillisennaytonarviointikeskusteluunosallistuneet", None)
  )

  def näyttö(kuvaus: String, paikka: String, arviointi: Option[NäytönArviointi] = None) = Näyttö(
    kuvaus, NäytönSuorituspaikka(Koodistokoodiviite("1", Some("työpaikka"), "ammatillisennaytonsuorituspaikka", Some(1)), paikka), arviointi)

  lazy val suoritustapaNäyttö = Suoritustapa(Koodistokoodiviite("naytto", Some("Näyttö"), None, "suoritustapa", Some(1)))
  lazy val suoritustapaOps = Suoritustapa(Koodistokoodiviite("ops", Some("Opetussuunnitelman mukainen"), "suoritustapa", Some(1)))
  lazy val järjestämismuotoOppisopimus = Koodistokoodiviite("20", Some("Oppisopimusmuotoinen"), "jarjestamismuoto", Some(1))
  lazy val järjestämismuotoOppilaitos = Koodistokoodiviite("10", Some("Oppilaitosmuotoinen"), "jarjestamismuoto", Some(1))
  lazy val stadinAmmattiopisto: Oppilaitos = Oppilaitos("1.2.246.562.10.52251087186", Some(Koodistokoodiviite("10105", None, "oppilaitosnumero", None)), Some("Stadin ammattiopisto"))
  lazy val toimipiste: OidOrganisaatio = OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))
  lazy val tutkintotoimikunta: Organisaatio = Tutkintotoimikunta("Autokorjaamoalan tutkintotoimikunta", 8406)
  lazy val osaamuspistettä = Koodistokoodiviite("6", Some("osaamispistettä"), "opintojenlaajuusyksikko", Some(1))
  lazy val lähdeWinnova = Koodistokoodiviite("winnova", Some("Winnova"), "lahdejarjestelma", Some(1))
  lazy val hyväksiluku = Hyväksiluku(
    OpsTutkinnonosa(Koodistokoodiviite("100238", Some("Asennushitsaus"), "tutkinnonosat", Some(1)), true, None),
    Some("Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta"))
  lazy val arviointiHyväksytty: Some[List[AmmatillinenArviointi]] = Some(List(AmmatillinenArviointi(
    arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)), Some(date(2013, 3, 20)),
    arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen"))))))

  def vahvistus(date: LocalDate) = Some(Vahvistus(date, helsinki, stadinAmmattiopisto, List(OrganisaatioHenkilö("Keijo Perttilä", "rehtori", stadinAmmattiopisto))))
  lazy val paikallisenOsanSuoritus = AmmatillisenTutkinnonosanSuoritus(
    koulutusmoduuli = PaikallinenTutkinnonosa(Paikallinenkoodi("123456789", "Pintavauriotyöt", "kallion_oma_koodisto"), "Opetellaan korjaamaan pinnallisia vaurioita", false, None),
    hyväksiluku = None,
    näyttö = Some(näyttö("Pintavaurioiden korjausta", "Autokorjaamo Oy, Riihimäki")),
    lisätiedot = None,
    paikallinenId = Some("suoritus-12345-2"),
    suorituskieli = None,
    tila = tilaValmis,
    alkamispäivä = None,
    toimipiste = toimipiste,
    arviointi = arviointiHyväksytty,
    vahvistus = vahvistus(date(2013, 5, 31))
  )

  lazy val arviointiKiitettävä = Some(
    List(
      AmmatillinenArviointi(
        arvosana = k3,
        Some(date(2014, 10, 20))
      )
    )
  )

  def opiskeluoikeus(oppilaitos: Oppilaitos = Oppilaitos("1.2.246.562.10.52251087186"),
                     tutkinto: AmmatillisenTutkinnonSuoritus = autoalanPerustutkinto,
                     osat: Option[List[AmmatillisenTutkinnonosanSuoritus]] = None) = {
    AmmatillinenOpiskeluoikeus(
      None,
      None,
      None,
      Some(date(2016, 9, 1)),
      Some(date(2020, 5, 1)),
      None,
      oppilaitos, None,
      List(tutkinto.copy(osasuoritukset = osat)),
      hojks = None,
      None,
      None,
      None
    )
  }

  def oppija( henkilö: Henkilö = exampleHenkilö,
              opiskeluOikeus: Opiskeluoikeus = opiskeluoikeus()) = {
    TorOppija(
      henkilö,
      List(opiskeluOikeus)
    )
  }
}
object ExamplesAmmatillinen {
  import AmmatillinenExampleData._

  lazy val uusi = oppija()

  lazy val oppisopimus = oppija(
    opiskeluOikeus = opiskeluoikeus(
      oppilaitos = stadinAmmattiopisto,
      tutkinto = tutkintoSuoritus(
        tutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
        tutkintonimike = None,
        osaamisala = None,
        suoritustapa = Some(suoritustapaNäyttö),
        järjestämismuoto = Some(OppisopimuksellinenJärjestämismuoto(järjestämismuotoOppisopimus, Oppisopimus(Yritys("Autokorjaamo Oy", "1234567-8")))),
        paikallinenId = Some("suoritus-12345"),
        suorituskieli = None,
        tila = tilaKesken,
        alkamisPäivä = Some(date(2016, 9, 1)),
        toimipiste = toimipiste,
        arviointi = None,
        vahvistus = None,
        osasuoritukset = None
      )
    ).copy(opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(List(
      Opiskeluoikeusjakso(date(2016, 9, 1), None, opiskeluoikeusAktiivinen, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
    ))))
  )

  lazy val paikallinen = oppija(opiskeluOikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinto.copy(suoritustapa = Some(suoritustapaNäyttö)),
    osat = Some(List(paikallisenOsanSuoritus))
  ))

  lazy val mukautettu = oppija(opiskeluOikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinto.copy(suoritustapa = Some(suoritustapaOps)),
    osat = Some(List(
      AmmatillisenTutkinnonosanSuoritus(
        koulutusmoduuli = OpsTutkinnonosa(Koodistokoodiviite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(Laajuus(11, osaamuspistettä))),
        lisätiedot = Some(List(AmmatillisenTutkinnonOsanLisätieto(
          Koodistokoodiviite("mukautettu", "ammatillisentutkinnonosanlisatieto"),
          "Tutkinnon osan ammattitaitovaatimuksia ja osaamisen arviointi on mukautettu (ja/tai niistä on poikettu) ammatillisesta peruskoulutuksesta annetun lain\n(630/1998, muutos 246/2015) 19 a (ja/tai 21) §:n perusteella"))),
        paikallinenId = Some("suoritus-12345-1"),
        suorituskieli = None,
        tila = tilaValmis,
        alkamispäivä = None,
        toimipiste = toimipiste,
        arviointi = arviointiKiitettävä,
        vahvistus = vahvistus(date(2014, 11, 8))
      )
    ))
  ))

  lazy val tutkinnonOsaToisestaTutkinnosta = oppija(opiskeluOikeus = opiskeluoikeus(
    tutkinto = tutkintoSuoritus(
      tutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
      suoritustapa = Some(suoritustapaNäyttö),
      järjestämismuoto = None,
      tila = tilaKesken,
      toimipiste = toimipiste
    ),

    osat = Some(List(
      AmmatillisenTutkinnonosanSuoritus(
        koulutusmoduuli = OpsTutkinnonosa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None, None, None),
        tutkinto = Some(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357305", "koulutus"), Some("40/011/2001"))),
        paikallinenId = Some("suoritus-12345-1"),
        suorituskieli = None,
        tila = tilaValmis,
        alkamispäivä = None,
        toimipiste = toimipiste,
        arviointi = arviointiKiitettävä,
        vahvistus = vahvistus(date(2014, 11, 8))
      )
    ))
  ))

  lazy val ops = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      AmmatillinenOpiskeluoikeus(
        None,
        None,
        None,
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        stadinAmmattiopisto, None,
        List(tutkintoSuoritus(
          tutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
          tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
          suoritustapa = Some(suoritustapaOps),
          järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos)),
          paikallinenId = Some("suoritus-12345"),
          tila = tilaKesken,
          toimipiste = toimipiste,
          suorituskieli = suomenKieli,

          osasuoritukset = Some(List(
            AmmatillisenTutkinnonosanSuoritus(
              koulutusmoduuli = OpsTutkinnonosa(Koodistokoodiviite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(Laajuus(11, osaamuspistettä))),
              hyväksiluku = None,
              näyttö = None,
              lisätiedot = None,
              tutkinto = None,
              paikallinenId = Some("suoritus-12345-1"),
              suorituskieli = None,
              tila = tilaValmis,
              alkamispäivä = None,
              toimipiste = toimipiste,
              arviointi = Some(
                List(
                  AmmatillinenArviointi(
                    arvosana = h2,
                    Some(date(2014, 5, 20))
                  ),
                  AmmatillinenArviointi(
                    arvosana = k3,
                    Some(date(2014, 10, 20))
                  )
                )
              ),
              vahvistus = vahvistus(date(2014, 11, 8))
            )
          ))
        )),
        hojks = Some(Hojks(hojksTehty = true, opetusryhmä = Some(Koodistokoodiviite("1", Some("Yleinen opetusryhmä"), "opetusryhma", None)))),
        Some(Koodistokoodiviite("tutkinto", Some("Tutkinto"), "opintojentavoite", None)),
        Some(OpiskeluoikeudenTila(
          List(
            Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2012, 12, 31)), opiskeluoikeusAktiivinen, Some(Koodistokoodiviite("1", Some("Valtionosuusrahoitteinen koulutus"), "opintojenrahoitus", None))),
            Opiskeluoikeusjakso(date(2013, 1, 1), Some(date(2013, 12, 31)), opiskeluoikeusKeskeyttänyt, Some(Koodistokoodiviite("1", Some("Valtionosuusrahoitteinen koulutus"), "opintojenrahoitus", None))),
            Opiskeluoikeusjakso(date(2014, 1, 1), None, opiskeluoikeusAktiivinen, Some(Koodistokoodiviite("1", Some("Valtionosuusrahoitteinen koulutus"), "opintojenrahoitus", None)))
          )
        )),
        Some(Läsnäolotiedot(List(
          Läsnäolojakso(date(2012, 9, 1), Some(date(2012, 12, 31)), Koodistokoodiviite("lasna", Some("Läsnä"), "lasnaolotila", Some(1))),
          Läsnäolojakso(date(2013, 1, 1), Some(date(2013, 12, 31)), Koodistokoodiviite("poissa", Some("Poissa"), "lasnaolotila", Some(1))),
          Läsnäolojakso(date(2014, 1, 1), None, Koodistokoodiviite("lasna", Some("Läsnä"), "lasnaolotila", Some(1)))
        )))
  )))

  lazy val full = AmmatillinenFullExample.full

  lazy val examples = List(
    Example("ammatillinen - uusi", "Uusi oppija lisätään suorittamaan Autoalan perustutkintoa", uusi),
    Example("ammatillinen - oppisopimus", "Uusi oppija, suorittaa oppisopimuksella", oppisopimus),
    Example("ammatillinen - paikallinen", "Oppija on suorittanut paikallisen tutkinnon osan", paikallinen),
    Example("ammatillinen - mukautettu", "Tutkinnon osan arviointia on mukautettu", mukautettu),
    Example("ammatillinen - osatoisestatutkinnosta", "Oppija on suorittanut toiseen tutkintoon liittyvän tutkinnon osan", tutkinnonOsaToisestaTutkinnosta),
    Example("ammatillinen - full", "Isompi esimerkki. Suorittaa perustutkintoa näyttönä. Tähän lisätty lähes kaikki kaavaillut tietokentät.", full),
    Example("ammatillinen - ops", "Perustutkinto ops:n mukaan, läsnäolotiedoilla, hojks", ops),
    Example("ammatillinen - päättötodistus", "Ympäristönhoitajaksi valmistunut opiskelija", AmmatillinenTodistusExample.todistus)
  )
}

object AmmatillinenTodistusExample {
  import AmmatillinenExampleData._

  lazy val todistus = TorOppija(
    exampleHenkilö,
    List(
      AmmatillinenOpiskeluoikeus(
        None,
        None,
        None,
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 5, 31)),
        stadinAmmattiopisto, None,
        List(tutkintoSuoritus(
          tutkintoKoulutus = AmmatillinenTutkintoKoulutus(
            Koodistokoodiviite("361902", Some("Luonto- ja ympäristöalan perustutkinto"), "koulutus", None),
            Some("62/011/2014")
          ),
          tutkintonimike = Some(List(Koodistokoodiviite("10083", Some("Ympäristönhoitaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(Koodistokoodiviite("1590", Some("Ympäristöalan osaamisala"), "osaamisala", None))),
          suoritustapa = Some(suoritustapaOps),
          järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos)),
          paikallinenId= None,
          suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
          tila = tilaValmis,
          alkamisPäivä = None,
          toimipiste = toimipiste,
          arviointi = arviointiHyväksytty,
          vahvistus = vahvistus(date(2016, 5, 31)),
          osasuoritukset = Some(List(
            tutkinnonOsanSuoritus("100431", "Kestävällä tavalla toimiminen", k3, 40),
            tutkinnonOsanSuoritus("100432", "Ympäristön hoitaminen", k3, 35),
            tutkinnonOsanSuoritus("100439", "Uusiutuvien energialähteiden hyödyntäminen", k3, 15),
            tutkinnonOsanSuoritus("100442", "Ulkoilureittien rakentaminen ja hoitaminen", k3, 15),
            tutkinnonOsanSuoritus("100443", "Kulttuuriympäristöjen kunnostaminen ja hoitaminen", k3, 15),
            tutkinnonOsanSuoritus("100447", "Vesistöjen kunnostaminen ja hoitaminen", k3, 15),

            tutkinnonOsanSuoritus("101053", "Viestintä- ja vuorovaikutusosaaminen", k3, 11),
            tutkinnonOsanSuoritus("101054", "Matemaattis-luonnontieteellinen osaaminen", k3, 9),
            tutkinnonOsanSuoritus("101055", "Yhteiskunnassa ja työelämässä tarvittava osaaminen", k3, 8),
            tutkinnonOsanSuoritus("101056", "Sosiaalinen ja kulttuurinen osaaminen", k3, 7),

            paikallisenTutkinnonOsanSuoritus("enkku3", "Matkailuenglanti", k3, 5),
            paikallisenTutkinnonOsanSuoritus("soskultos1", "Sosiaalinen ja kulttuurinen osaaminen", k3, 5)
          ))
        )),
        hojks = None,
        Some(Koodistokoodiviite("tutkinto", Some("Tutkinto"), "opintojentavoite", None)),
        Some(OpiskeluoikeudenTila(
          List(
            Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 5, 31)), opiskeluoikeusAktiivinen, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
            Opiskeluoikeusjakso(date(2016, 6, 1), None, opiskeluoikeusPäättynyt, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
          )
        )),
        None

      )
    )
  )

  def tutkinnonOsanSuoritus(koodi: String, nimi: String, arvosana: Koodistokoodiviite, laajuus: Float): AmmatillisenTutkinnonosanSuoritus = {
    val osa: OpsTutkinnonosa = OpsTutkinnonosa(Koodistokoodiviite(koodi, Some(nimi), "tutkinnonosat", Some(1)), true, Some(Laajuus(laajuus, osaamuspistettä)))
    tutkonnonOsanSuoritus(arvosana, osa)
  }

  def paikallisenTutkinnonOsanSuoritus(koodi: String, nimi: String, arvosana: Koodistokoodiviite, laajuus: Float): AmmatillisenTutkinnonosanSuoritus = {
    val osa: PaikallinenTutkinnonosa = PaikallinenTutkinnonosa(Paikallinenkoodi(koodi, nimi, "paikallinen"), nimi, false, Some(Laajuus(laajuus, osaamuspistettä)))
    tutkonnonOsanSuoritus(arvosana, osa)
  }

  def tutkonnonOsanSuoritus(arvosana: Koodistokoodiviite, osa: AmmatillinenTutkinnonOsa): AmmatillisenTutkinnonosanSuoritus = {
    AmmatillisenTutkinnonosanSuoritus(
      koulutusmoduuli = osa,
      näyttö = None, paikallinenId = None, suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(AmmatillinenArviointi(arvosana = arvosana, Some(date(2014, 10, 20))))),
      vahvistus = vahvistus(date(2016, 5, 31))
    )
  }
}

object AmmatillinenFullExample {
  import AmmatillinenExampleData._

  lazy val full = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      AmmatillinenOpiskeluoikeus(
        None,
        None,
        Some(LähdejärjestelmäId("847823465", lähdeWinnova)),
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        stadinAmmattiopisto, None,
        List(tutkintoSuoritus(
          tutkintoKoulutus = AmmatillinenTutkintoKoulutus(
            Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", None),
            Some("39/011/2014")
          ),
          tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
          suoritustapa = Some(suoritustapaNäyttö),
          järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos)),
          paikallinenId= Some("suoritus-12345"),
          suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
          tila = tilaValmis,
          alkamisPäivä = None,
          toimipiste = toimipiste,
          arviointi = arviointiHyväksytty,
          vahvistus = Some(Vahvistus(date(2016, 1, 9), helsinki, stadinAmmattiopisto, List(
            OrganisaatioHenkilö("Mauri Bauer", "puheenjohtaja", tutkintotoimikunta),
            OrganisaatioHenkilö("Keijo Perttilä", "rehtori", stadinAmmattiopisto)))),
          osasuoritukset = Some(tutkinnonOsat)
        )),
        hojks = None,
        Some(Koodistokoodiviite("tutkinto", Some("Tutkinto"), "opintojentavoite", None)),
        Some(OpiskeluoikeudenTila(
          List(
            Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
            Opiskeluoikeusjakso(date(2016, 1, 10), None, opiskeluoikeusPäättynyt, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
          )
        )),
        None
      )
    )
  )

  private lazy val tutkinnonOsat = List(
    AmmatillisenTutkinnonosanSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        Koodistokoodiviite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat", Some(1)),
        true,
        laajuus = None
      ),
      näyttö = Some(näyttö("Huolto- ja korjaustyöt", "Autokorjaamo Oy, Riihimäki", Some(näytönArviointi))),
      paikallinenId = Some("suoritus-12345-1"),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2012, 10, 20)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2013, 1, 31))
    ),
    paikallisenOsanSuoritus,
    AmmatillisenTutkinnonosanSuoritus(
      koulutusmoduuli =  OpsTutkinnonosa(
        Koodistokoodiviite("100019", Some("Mittaus- ja korivauriotyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö("Mittaus- ja korivauriotöitä", "Autokorjaamo Oy, Riihimäki")),
      paikallinenId = Some("suoritus-12345-3"),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2013, 4, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2013, 5, 31))
    ),
    AmmatillisenTutkinnonosanSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        Koodistokoodiviite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö("Maalauksen esikäsittelytöitä", "Autokorjaamo Oy, Riihimäki")),
      paikallinenId = Some("suoritus-12345-4"),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2014, 10, 20)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2014, 11, 8))
    ),
    AmmatillisenTutkinnonosanSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        Koodistokoodiviite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö("Auton lisävarustetöitä", "Autokorjaamo Oy, Riihimäki")),
      paikallinenId = Some("suoritus-12345-5"),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2015, 4, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2015, 5, 1))
    ),
    AmmatillisenTutkinnonosanSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        Koodistokoodiviite("101050", Some("Yritystoiminnan suunnittelu"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      hyväksiluku = Some(hyväksiluku),
      paikallinenId = Some("suoritus-12345-6"),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2016, 2, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2016, 5, 1))
    )
  )
}
