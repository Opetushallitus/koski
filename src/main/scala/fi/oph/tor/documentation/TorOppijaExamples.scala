package fi.oph.tor.documentation

import java.time.LocalDate.{of => date}

import fi.oph.tor.schema._

object TorOppijaExampleData {
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

  def näyttö(kuvaus: String, paikka: String, arviointi: Option[NäytönArviointi] = None) = Näyttö(kuvaus, paikka, arviointi)
  lazy val suoritustapaNäyttö = KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", Some(1))
  lazy val suoritustapaOps = KoodistoKoodiViite("ops", Some("Opetussuunnitelman mukainen"), "suoritustapa", Some(1))
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
  lazy val hyväksiluku = Hyväksiluku(OpsTutkinnonosa(KoodistoKoodiViite("100238", Some("Asennushitsaus"), "tutkinnonosat", Some(1)), true, None), Some("Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta"))
  lazy val paikallisenOsanSuoritus = Suoritus(Some("suoritus-12345-2"), PaikallinenTutkinnonosatoteutus(PaikallinenTutkinnonosa(Paikallinenkoodi("123456789", "Pintavauriotyöt", "kallion_oma_koodisto"), "Opetellaan korjaamaan pinnallisia vaurioita", false, None), suoritustapa = Some(NäytöllinenSuoritustapa(suoritustapaNäyttö, näyttö("Pintavaurioiden korjausta", "Autokorjaamo Oy, Riihimäki")))), suorituskieli = None, tila = None, alkamispäivä = None, toimipiste, Some(List(Arviointi(arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)), Some(date(2013, 3, 20)), arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))))), Some(Vahvistus(Some(date(2013, 5, 31)), Some(stadinAmmattiopisto), None)), osasuoritukset = None)

  def opiskeluoikeus(oppilaitos: Oppilaitos = Oppilaitos("1.2.246.562.10.52251087186"),
                     tutkinto: TutkintoKoulutustoteutus = TutkintoKoulutustoteutus(TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")), None, None, None, None),
                     tutkinnonOsat: Option[List[Suoritus]] = None) = {
    OpiskeluOikeus(
      None,
      None,
      None,
      Some(date(2016, 9, 1)),
      Some(date(2020, 5, 1)),
      None,
      oppilaitos,
      Suoritus(
        Some("suoritus-12345"),
        tutkinto,
        None,
        None,
        Some(date(2016, 9, 1)),
        toimipiste,
        None,
        None,
        tutkinnonOsat
      ),
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
      tutkinto = TutkintoKoulutustoteutus(
        TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
        None, None, Some(DefaultSuoritustapa(suoritustapaNäyttö)),
        Some(OppisopimuksellinenJärjestämismuoto(järjestämismuotoOppisopimus, Oppisopimus(Yritys("Autokorjaamo Oy", "1234567-8")))))
    ).copy(opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(List(
      Opiskeluoikeusjakso(date(2016, 9, 1), None, opiskeluoikeusAktiivinen, Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
    ))))
  )

  lazy val paikallinen = oppija(opiskeluOikeus = opiskeluoikeus(
    tutkinnonOsat = Some(List(paikallisenOsanSuoritus))
  ))

  lazy val tutkinnonOsaToisestaTutkinnosta = oppija(opiskeluOikeus = opiskeluoikeus(
    tutkinto = TutkintoKoulutustoteutus(
      TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
      None, None, Some(DefaultSuoritustapa(suoritustapaNäyttö)),
      None),

    tutkinnonOsat = Some(List(
      Suoritus(
        Some("suoritus-12345-1"),
        OpsTutkinnonosatoteutus(
          OpsTutkinnonosa(KoodistoKoodiViite("104052", "tutkinnonosat"), true, None, None, None),
          tutkinto = Some(TutkintoKoulutus(KoodistoKoodiViite("357305", "koulutus"), Some("40/011/2001")))
        ),
        suorituskieli = None,
        tila = None, // TODO: tila kuntoon
        alkamispäivä = None,
        toimipiste,
        Some(
          List(
            Arviointi(
              arvosana = k3,
              Some(date(2014, 10, 20))
            )
          )
        ),
        Some(Vahvistus(Some(date(2014, 11, 8)), Some(stadinAmmattiopisto), None)),
        osasuoritukset = None
      )
    ))
  ))

  lazy val ops = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      OpiskeluOikeus(
        None,
        None,
        None,
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        stadinAmmattiopisto,
        Suoritus(
          Some("suoritus-12345"),
          TutkintoKoulutustoteutus(
            TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
            Some(List(KoodistoKoodiViite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
            Some(List(KoodistoKoodiViite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
            Some(DefaultSuoritustapa(suoritustapaOps)),
            Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos))
          ),
          Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", None)),
          None,
          None,
          toimipiste,
          None,
          None,

          Some(List(
            Suoritus(
              Some("suoritus-12345-1"),
              OpsTutkinnonosatoteutus(
                OpsTutkinnonosa(KoodistoKoodiViite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(Laajuus(11, opintojenLaajuusYksikkö))),
                suoritustapa = Some(DefaultSuoritustapa(suoritustapaOps))
              ),
              suorituskieli = None,
              tila = None,
              alkamispäivä = None,
              toimipiste,
              Some(
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
              Some(Vahvistus(Some(date(2014, 11, 8)), Some(stadinAmmattiopisto), None)),
              osasuoritukset = None
            )
          ))
        ),
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

  lazy val examples = List(
    Example("uusi", "Uusi oppija lisätään suorittamaan Autoalan perustutkintoa", uusi),
    Example("oppisopimus", "Uusi oppija, suorittaa oppisopimuksella", oppisopimus),
    Example("paikallinen", "Oppija on suorittanut paikallisen tutkinnon osan", paikallinen),
    Example("osatoisestatutkinnosta", "Oppija on suorittanut toiseen tutkintoon liittyvän tutkinnon osan", tutkinnonOsaToisestaTutkinnosta),
    Example("full", "Isompi esimerkki. Suorittaa perustutkintoa näyttönä. Tähän lisätty lähes kaikki kaavaillut tietokentät.", FullExample.full),
    Example("ops", "Perustutkinto ops:n mukaan, läsnäolotiedoilla, hojks", ops)
  )

}

object FullExample {
  import TorOppijaExampleData._

  lazy val full = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      OpiskeluOikeus(
        None,
        None,
        Some(LähdejärjestelmäId("847823465", lähdeWinnova)),
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        stadinAmmattiopisto,
        Suoritus(
          Some("suoritus-12345"),
          TutkintoKoulutustoteutus(
            TutkintoKoulutus(
              KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None),
              Some("39/011/2014")
            ), Some(List(KoodistoKoodiViite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
            Some(List(KoodistoKoodiViite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
            suoritustapa = Some(DefaultSuoritustapa(suoritustapaNäyttö)),
            järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos))
          ),
          suorituskieli = Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", None)),
          Some(KoodistoKoodiViite("VALMIS", Some("Valmis"), "suorituksentila", None)),
          alkamispäivä = None,
          toimipiste,
          arviointi = None,
          Some(Vahvistus(Some(date(2016, 1, 9)), Some(stadinAmmattiopisto), Some(List(
            OrganisaatioHenkilö("Jack Bauer", "puheenjohtaja", tutkintotoimikunta),
            OrganisaatioHenkilö("Keijo Perttilä", "rehtori", stadinAmmattiopisto))))),
          Some(tutkinnonOsat)
        ),
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
    Suoritus(
      Some("suoritus-12345-1"),
      OpsTutkinnonosatoteutus(
        OpsTutkinnonosa(
          KoodistoKoodiViite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat", Some(1)),
          true,
          laajuus = None
        ),
        suoritustapa = Some(NäytöllinenSuoritustapa(suoritustapaNäyttö, näyttö("Huolto- ja korjaustyöt", "Autokorjaamo Oy, Riihimäki", Some(näytönArviointi))))
      ),
      suorituskieli = None,
      tila = None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2012, 10, 20)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2013, 1, 31)), Some(stadinAmmattiopisto), None)),
      osasuoritukset = None
    ),
    paikallisenOsanSuoritus,
    Suoritus(
      Some("suoritus-12345-3"),
      OpsTutkinnonosatoteutus(
        OpsTutkinnonosa(
          KoodistoKoodiViite("100019", Some("Mittaus- ja korivauriotyöt"), "tutkinnonosat", Some(1)),
          true,
          None
        ),
        suoritustapa = Some(NäytöllinenSuoritustapa(suoritustapaNäyttö, näyttö("Mittaus- ja korivauriotöitä", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      tila = None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2013, 4, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2013, 5, 31)), Some(stadinAmmattiopisto), None)),
      osasuoritukset = None
    ),
    Suoritus(
      Some("suoritus-12345-4"),
      OpsTutkinnonosatoteutus(
        OpsTutkinnonosa(
          KoodistoKoodiViite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", Some(1)),
          true,
          None
        ),
        suoritustapa = Some(NäytöllinenSuoritustapa(suoritustapaNäyttö, näyttö("Maalauksen esikäsittelytöitä", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2014, 10, 20)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2014, 11, 8)), Some(stadinAmmattiopisto), None)),
      osasuoritukset = None
    ),
    Suoritus(
      Some("suoritus-12345-5"),
      OpsTutkinnonosatoteutus(
        OpsTutkinnonosa(
          KoodistoKoodiViite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat", Some(1)),
          true,
          None
        ),
        suoritustapa = Some(NäytöllinenSuoritustapa(suoritustapaNäyttö, näyttö("Auton lisävarustetöitä", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      tila = None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2015, 4, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2015, 5, 1)), Some(stadinAmmattiopisto), None)),
      osasuoritukset = None
    ),
    Suoritus(
      Some("suoritus-12345-6"),
      OpsTutkinnonosatoteutus(
        OpsTutkinnonosa(
          KoodistoKoodiViite("101050", Some("Yritystoiminnan suunnittelu"), "tutkinnonosat", Some(1)),
          true,
          None
        ),
        suoritustapa = Some(DefaultSuoritustapa(suoritustapaNäyttö)), // TODO: mikä suoritustapa tunnustetulle osaamiselle?
        hyväksiluku = Some(hyväksiluku)
      ),
      suorituskieli = None,
      tila = None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        Some(date(2016, 2, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2016, 5, 1)), Some(stadinAmmattiopisto), None)),
      osasuoritukset = None
    )
  )
}

case class Example(name: String, description: String, data: TorOppija)
