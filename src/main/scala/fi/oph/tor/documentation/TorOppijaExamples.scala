package fi.oph.tor.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.tor.schema._

object TorOppijaExampleData {

  def tutkintoSuoritus(tutkintoKoulutus: TutkintoKoulutus,
                               tutkintonimike: Option[List[KoodistoKoodiViite]] = None,
                               osaamisala: Option[List[KoodistoKoodiViite]] = None,
                               suoritustapa: Option[Suoritustapa] = None,
                               järjestämismuoto: Option[Järjestämismuoto] = None,
                               paikallinenId: Option[String] = None,
                               suorituskieli: Option[KoodistoKoodiViite] = None,
                               tila: KoodistoKoodiViite,
                               alkamisPäivä: Option[LocalDate] = None,
                               toimipiste: OrganisaatioWithOid,
                               arviointi: Option[List[Arviointi]] = None,
                               vahvistus: Option[Vahvistus] = None,
                               osasuoritukset: Option[List[AmmatillinenTutkinnonosaSuoritus]] = None): AmmatillinenTutkintoSuoritus =

    AmmatillinenTutkintoSuoritus(
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

  lazy val autoalanPerustutkinto: AmmatillinenTutkintoSuoritus = tutkintoSuoritus(
    tutkintoKoulutus = TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
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

  lazy val h2: KoodistoKoodiViite = KoodistoKoodiViite("2", Some("H2"), "arviointiasteikkoammatillinent1k3", None)
  lazy val k3: KoodistoKoodiViite = KoodistoKoodiViite("3", Some("K3"), "arviointiasteikkoammatillinent1k3", None)
  lazy val näytönArviointi = NäytönArviointi(List(
    NäytönArviointikohde(KoodistoKoodiViite("1", Some("Työprosessin hallinta"), "ammatillisennaytonarviointikohde", None), k3),
    NäytönArviointikohde(KoodistoKoodiViite("2", Some("Työmenetelmien, -välineiden ja materiaalin hallinta"), "ammatillisennaytonarviointikohde", None), h2),
    NäytönArviointikohde(KoodistoKoodiViite("3", Some("Työn perustana olevan tiedon hallinta"), "ammatillisennaytonarviointikohde", None), h2),
    NäytönArviointikohde(KoodistoKoodiViite("4", Some("Elinikäisen oppimisen avaintaidot"), "ammatillisennaytonarviointikohde", None), k3)),
    KoodistoKoodiViite("1", Some("Opettaja"), "ammatillisennaytonarvioinnistapaattaneet", None),
    KoodistoKoodiViite("1", Some("Opiskelija ja opettaja"), "ammatillisennaytonarviointikeskusteluunosallistuneet", None)
  )

  def näyttö(kuvaus: String, paikka: String, arviointi: Option[NäytönArviointi] = None) = Näyttö(
    kuvaus, NäytönSuorituspaikka(KoodistoKoodiViite("1", Some("työpaikka"), "ammatillisennaytonsuorituspaikka", Some(1)), paikka), arviointi)

  lazy val suoritustapaNäyttö = Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", Some(1)))
  lazy val suoritustapaOps = Suoritustapa(KoodistoKoodiViite("ops", Some("Opetussuunnitelman mukainen"), "suoritustapa", Some(1)))
  lazy val järjestämismuotoOppisopimus = KoodistoKoodiViite("20", Some("Oppisopimusmuotoinen"), "jarjestamismuoto", Some(1))
  lazy val järjestämismuotoOppilaitos = KoodistoKoodiViite("10", Some("Oppilaitosmuotoinen"), "jarjestamismuoto", Some(1))
  lazy val stadinAmmattiopisto: Oppilaitos = Oppilaitos("1.2.246.562.10.52251087186", Some(KoodistoKoodiViite("10105", None, "oppilaitosnumero", None)), Some("Stadin ammattiopisto"))
  lazy val toimipiste: OidOrganisaatio = OidOrganisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))
  lazy val tutkintotoimikunta: Organisaatio = Tutkintotoimikunta("Autokorjaamoalan tutkintotoimikunta", 8406)
  lazy val opintojenLaajuusYksikkö = KoodistoKoodiViite("6", Some("osaamispistettä"), "opintojenlaajuusyksikko", Some(1))
  lazy val opiskeluoikeusAktiivinen = KoodistoKoodiViite("aktiivinen", Some("Aktiivinen"), "opiskeluoikeudentila", Some(1))
  lazy val opiskeluoikeusPäättynyt = KoodistoKoodiViite("paattynyt", Some("Päättynyt"), "opiskeluoikeudentila", Some(1))
  lazy val opiskeluoikeusKeskeyttänyt = KoodistoKoodiViite("keskeyttanyt", Some("Keskeyttänyt"), "opiskeluoikeudentila", Some(1))
  lazy val lähdeWinnova = KoodistoKoodiViite("winnova", Some("Winnova"), "lahdejarjestelma", Some(1))
  lazy val hyväksiluku = Hyväksiluku(
    OpsTutkinnonosa(KoodistoKoodiViite("100238", Some("Asennushitsaus"), "tutkinnonosat", Some(1)), true, None),
    Some("Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta"))
  lazy val tilaKesken = KoodistoKoodiViite("KESKEN", "suorituksentila")
  lazy val tilaValmis = KoodistoKoodiViite("VALMIS", "suorituksentila")
  lazy val arviointiHyväksytty: Some[List[Arviointi]] = Some(List(Arviointi(
    arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)), Some(date(2013, 3, 20)),
    arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen"))))))

  lazy val paikallisenOsanSuoritus = AmmatillinenPaikallinenTutkinnonosaSuoritus(
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
    vahvistus = Some(Vahvistus(Some(date(2013, 5, 31)), Some(stadinAmmattiopisto), None)),
    osasuoritukset = None
  )

  lazy val arviointiKiitettävä = Some(
    List(
      Arviointi(
        arvosana = k3,
        Some(date(2014, 10, 20))
      )
    )
  )

  def opiskeluoikeus(oppilaitos: Oppilaitos = Oppilaitos("1.2.246.562.10.52251087186"),
                     tutkinto: AmmatillinenTutkintoSuoritus = autoalanPerustutkinto,
                     osat: Option[List[AmmatillinenTutkinnonosaSuoritus]] = None) = {
    AmmatillinenOpiskeluOikeus(
      None,
      None,
      None,
      Some(date(2016, 9, 1)),
      Some(date(2020, 5, 1)),
      None,
      oppilaitos,
      List(tutkinto.copy(osasuoritukset = osat)),
      hojks = None,
      None,
      None,
      None
    )
  }

  def oppija( henkilö: Henkilö = Henkilö("010101-123N", "matti pekka", "matti", "virtanen"),
              opiskeluOikeus: OpiskeluOikeus = opiskeluoikeus()) = {
    TorOppija(
      Henkilö("010101-123N", "matti pekka", "matti", "virtanen"),
      List(opiskeluOikeus)
    )
  }
}
object TorOppijaExamples {
  import TorOppijaExampleData._

  lazy val uusi = oppija()

  lazy val oppisopimus = oppija(
    opiskeluOikeus = opiskeluoikeus(
      oppilaitos = stadinAmmattiopisto,
      tutkinto = tutkintoSuoritus(
        tutkintoKoulutus = TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
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
      Opiskeluoikeusjakso(date(2016, 9, 1), None, opiskeluoikeusAktiivinen, Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
    ))))
  )

  lazy val paikallinen = oppija(opiskeluOikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinto.copy(suoritustapa = Some(suoritustapaNäyttö)),
    osat = Some(List(paikallisenOsanSuoritus))
  ))

  lazy val mukautettu = oppija(opiskeluOikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinto.copy(suoritustapa = Some(suoritustapaOps)),
    osat = Some(List(
      AmmatillinenOpsTutkinnonosaSuoritus(
        koulutusmoduuli = OpsTutkinnonosa(KoodistoKoodiViite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(Laajuus(11, opintojenLaajuusYksikkö))),
        lisätiedot = Some(List(AmmatillisenTutkinnonOsanLisätieto(
          KoodistoKoodiViite("mukautettu", "ammatillisentutkinnonosanlisatieto"),
          "Tutkinnon osan ammattitaitovaatimuksia ja osaamisen arviointi on mukautettu (ja/tai niistä on poikettu) ammatillisesta peruskoulutuksesta annetun lain\n(630/1998, muutos 246/2015) 19 a (ja/tai 21) §:n perusteella"))),
        paikallinenId = Some("suoritus-12345-1"),
        suorituskieli = None,
        tila = tilaValmis,
        alkamispäivä = None,
        toimipiste = toimipiste,
        arviointi = arviointiKiitettävä,
        vahvistus = Some(Vahvistus(Some(date(2014, 11, 8)), Some(stadinAmmattiopisto), None))
      )
    ))
  ))

  lazy val tutkinnonOsaToisestaTutkinnosta = oppija(opiskeluOikeus = opiskeluoikeus(
    tutkinto = tutkintoSuoritus(
      tutkintoKoulutus = TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
      suoritustapa = Some(suoritustapaNäyttö),
      järjestämismuoto = None,
      tila = tilaKesken,
      toimipiste = toimipiste
    ),

    osat = Some(List(
      AmmatillinenOpsTutkinnonosaSuoritus(
        koulutusmoduuli = OpsTutkinnonosa(KoodistoKoodiViite("104052", "tutkinnonosat"), true, None, None, None),
        tutkinto = Some(TutkintoKoulutus(KoodistoKoodiViite("357305", "koulutus"), Some("40/011/2001"))),
        paikallinenId = Some("suoritus-12345-1"),
        suorituskieli = None,
        tila = tilaValmis,
        alkamispäivä = None,
        toimipiste = toimipiste,
        arviointi = arviointiKiitettävä,
        vahvistus = Some(Vahvistus(Some(date(2014, 11, 8)), Some(stadinAmmattiopisto), None))
      )
    ))
  ))

  lazy val ops = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      AmmatillinenOpiskeluOikeus(
        None,
        None,
        None,
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        stadinAmmattiopisto,
        List(tutkintoSuoritus(
          tutkintoKoulutus = TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
          tutkintonimike = Some(List(KoodistoKoodiViite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(KoodistoKoodiViite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
          suoritustapa = Some(suoritustapaOps),
          järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos)),
          paikallinenId = Some("suoritus-12345"),
          tila = tilaKesken,
          toimipiste = toimipiste,
          suorituskieli = Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", None)),

          osasuoritukset = Some(List(
            AmmatillinenOpsTutkinnonosaSuoritus(
              koulutusmoduuli = OpsTutkinnonosa(KoodistoKoodiViite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(Laajuus(11, opintojenLaajuusYksikkö))),
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
                  Arviointi(
                    arvosana = h2,
                    Some(date(2014, 5, 20))
                  ),
                  Arviointi(
                    arvosana = k3,
                    Some(date(2014, 10, 20))
                  )
                )
              ),
              vahvistus = Some(Vahvistus(Some(date(2014, 11, 8)), Some(stadinAmmattiopisto), None))
            )
          ))
        )),
        hojks = Some(Hojks(hojksTehty = true, opetusryhmä = Some(KoodistoKoodiViite("1", Some("Yleinen opetusryhmä"), "opetusryhma", None)))),
        Some(KoodistoKoodiViite("tutkinto", Some("Tutkinto"), "opintojentavoite", None)),
        Some(OpiskeluoikeudenTila(
          List(
            Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2012, 12, 31)), opiskeluoikeusAktiivinen, Some(KoodistoKoodiViite("1", Some("Valtionosuusrahoitteinen koulutus"), "opintojenrahoitus", None))),
            Opiskeluoikeusjakso(date(2013, 1, 1), Some(date(2013, 12, 31)), opiskeluoikeusKeskeyttänyt, Some(KoodistoKoodiViite("1", Some("Valtionosuusrahoitteinen koulutus"), "opintojenrahoitus", None))),
            Opiskeluoikeusjakso(date(2014, 1, 1), None, opiskeluoikeusAktiivinen, Some(KoodistoKoodiViite("1", Some("Valtionosuusrahoitteinen koulutus"), "opintojenrahoitus", None)))
          )
        )),
        Some(Läsnäolotiedot(List(
          Läsnäolojakso(date(2012, 9, 1), Some(date(2012, 12, 31)), KoodistoKoodiViite("lasna", Some("Läsnä"), "lasnaolotila", Some(1))),
          Läsnäolojakso(date(2013, 1, 1), Some(date(2013, 12, 31)), KoodistoKoodiViite("poissa", Some("Poissa"), "lasnaolotila", Some(1))),
          Läsnäolojakso(date(2014, 1, 1), None, KoodistoKoodiViite("lasna", Some("Läsnä"), "lasnaolotila", Some(1)))
        )))
  )))

  lazy val full = FullExample.full

  lazy val examples = List(
    Example("uusi", "Uusi oppija lisätään suorittamaan Autoalan perustutkintoa", uusi),
    Example("oppisopimus", "Uusi oppija, suorittaa oppisopimuksella", oppisopimus),
    Example("paikallinen", "Oppija on suorittanut paikallisen tutkinnon osan", paikallinen),
    Example("mukautettu", "Tutkinnon osan arviointia on mukautettu", mukautettu),
    Example("osatoisestatutkinnosta", "Oppija on suorittanut toiseen tutkintoon liittyvän tutkinnon osan", tutkinnonOsaToisestaTutkinnosta),
    Example("full", "Isompi esimerkki. Suorittaa perustutkintoa näyttönä. Tähän lisätty lähes kaikki kaavaillut tietokentät.", full),
    Example("ops", "Perustutkinto ops:n mukaan, läsnäolotiedoilla, hojks", ops)
  )

}

object FullExample {
  import TorOppijaExampleData._

  lazy val full = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      AmmatillinenOpiskeluOikeus(
        None,
        None,
        Some(LähdejärjestelmäId("847823465", lähdeWinnova)),
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        stadinAmmattiopisto,
        List(tutkintoSuoritus(
          tutkintoKoulutus = TutkintoKoulutus(
            KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None),
            Some("39/011/2014")
          ),
          tutkintonimike = Some(List(KoodistoKoodiViite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(KoodistoKoodiViite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
          suoritustapa = Some(suoritustapaNäyttö),
          järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos)),
          paikallinenId= Some("suoritus-12345"),
          suorituskieli = Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", None)),
          tila = tilaValmis,
          alkamisPäivä = None,
          toimipiste = toimipiste,
          arviointi = arviointiHyväksytty,
          vahvistus = Some(Vahvistus(Some(date(2016, 1, 9)), Some(stadinAmmattiopisto), Some(List(
            OrganisaatioHenkilö("Jack Bauer", "puheenjohtaja", tutkintotoimikunta),
            OrganisaatioHenkilö("Keijo Perttilä", "rehtori", stadinAmmattiopisto))))),
          osasuoritukset = Some(tutkinnonOsat)
        )),
        hojks = None,
        Some(KoodistoKoodiViite("tutkinto", Some("Tutkinto"), "opintojentavoite", None)),
        Some(OpiskeluoikeudenTila(
          List(
            Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen, Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
            Opiskeluoikeusjakso(date(2016, 1, 10), None, opiskeluoikeusPäättynyt, Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
          )
        )),
        None
      )
    )
  )

  private lazy val tutkinnonOsat = List(
    AmmatillinenOpsTutkinnonosaSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        KoodistoKoodiViite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat", Some(1)),
        true,
        laajuus = None
      ),
      näyttö = Some(näyttö("Huolto- ja korjaustyöt", "Autokorjaamo Oy, Riihimäki", Some(näytönArviointi))),
      paikallinenId = Some("suoritus-12345-1"),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2012, 10, 20)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = Some(Vahvistus(Some(date(2013, 1, 31)), Some(stadinAmmattiopisto), None))
    ),
    paikallisenOsanSuoritus,
    AmmatillinenOpsTutkinnonosaSuoritus(
      koulutusmoduuli =  OpsTutkinnonosa(
        KoodistoKoodiViite("100019", Some("Mittaus- ja korivauriotyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö("Mittaus- ja korivauriotöitä", "Autokorjaamo Oy, Riihimäki")),
      paikallinenId = Some("suoritus-12345-3"),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2013, 4, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = Some(Vahvistus(Some(date(2013, 5, 31)), Some(stadinAmmattiopisto), None))
    ),
    AmmatillinenOpsTutkinnonosaSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        KoodistoKoodiViite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö("Maalauksen esikäsittelytöitä", "Autokorjaamo Oy, Riihimäki")),
      paikallinenId = Some("suoritus-12345-4"),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2014, 10, 20)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = Some(Vahvistus(Some(date(2014, 11, 8)), Some(stadinAmmattiopisto), None))
    ),
    AmmatillinenOpsTutkinnonosaSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        KoodistoKoodiViite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö("Auton lisävarustetöitä", "Autokorjaamo Oy, Riihimäki")),
      paikallinenId = Some("suoritus-12345-5"),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2015, 4, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = Some(Vahvistus(Some(date(2015, 5, 1)), Some(stadinAmmattiopisto), None))
    ),
    AmmatillinenOpsTutkinnonosaSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        KoodistoKoodiViite("101050", Some("Yritystoiminnan suunnittelu"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      hyväksiluku = Some(hyväksiluku),
      paikallinenId = Some("suoritus-12345-6"),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = toimipiste,
      arviointi = Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2016, 2, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = Some(Vahvistus(Some(date(2016, 5, 1)), Some(stadinAmmattiopisto), None))
    )
  )
}

case class Example(name: String, description: String, data: TorOppija)
