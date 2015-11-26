package fi.oph.tor.schema

import java.time.LocalDate.{of => date, _}

object TorOppijaExamples {
  private val näyttö = Näyttö("Toimi automekaanikkona kolarikorjauspuolella kaksi vuotta", "Autokorjaamo Oy, Riihimäki")
  private val suoritustapaNäyttö = KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", Some(1))
  private val suoritustapaOps = KoodistoKoodiViite("ops", Some("Opetussuunnitelmaperusteinen"), "suoritustapa", Some(1))
  private val järjestämismuotoOppisopimus = KoodistoKoodiViite("20", Some("Oppisopimusmuotoinen"), "jarjestamismuoto", Some(1))
  private val järjestämismuotoOppilaitos = KoodistoKoodiViite("10", Some("Oppilaitosmuotoinen"), "jarjestamismuoto", Some(1))
  private val toimipiste: Organisaatio = Organisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))
  private val opintojenLaajuusYksikkö = KoodistoKoodiViite("6", Some("osaamispistettä"), "opintojenlaajuusyksikko", Some(1))
  private val opiskeluoikeusAktiivinen = KoodistoKoodiViite("aktiivinen", Some("Aktiivinen"), "opiskeluoikeudentila", Some(1))
  private val opiskeluoikeusPäättynyt = KoodistoKoodiViite("paattynyt", Some("Päättynyt"), "opiskeluoikeudentila", Some(1))
  private val opiskeluoikeusKeskeyttänyt = KoodistoKoodiViite("keskeyttanyt", Some("Keskeyttänyt"), "opiskeluoikeudentila", Some(1))
  private val lähdeWinnova = KoodistoKoodiViite("winnova", Some("Winnova"), "lahdejarjestelma", Some(1))
  private val tutkinnonOsat = List(
    Suoritus(
      Some("suoritus-12345-1"),
      OpsTutkinnonosatoteutus(
        OpsTutkinnonosa(
          KoodistoKoodiViite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat", Some(1)),
          true,
          laajuus = Laajuus(30, opintojenLaajuusYksikkö)
        ),
        suoritustapa = Some(NäytöllinenSuoritustapa(suoritustapaNäyttö, Näyttö("Huolto- ja korjaustyöt", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      tila = None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", Some(1)),
        Some(date(2012, 10, 20)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2013, 1, 31)))),
      osasuoritukset = None
    ),
    Suoritus(
      Some("suoritus-12345-2"),
      PaikallinenTutkinnonosatoteutus(
        PaikallinenTutkinnonosa(
          Paikallinenkoodi("123456789", "Pintavauriotyöt", "kallion_oma_koodisto"),
          "Pintavauriotyöt",
          "Opetellaan korjaamaan pinnallisia vaurioita",
          false, Laajuus(15, opintojenLaajuusYksikkö)),
        suoritustapa = Some(NäytöllinenSuoritustapa(suoritustapaNäyttö, Näyttö("Pintavaurioiden korjausta", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      tila = None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", Some(1)),
        Some(date(2013, 3, 20)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2013, 5, 31)))),
      osasuoritukset = None
    ),
    Suoritus(
      Some("suoritus-12345-3"),
      OpsTutkinnonosatoteutus(
        OpsTutkinnonosa(
          KoodistoKoodiViite("100019", Some("Mittaus- ja korivauriotyöt"), "tutkinnonosat", Some(1)),
          true,
          Laajuus(30, opintojenLaajuusYksikkö)
        ),
        suoritustapa = Some(NäytöllinenSuoritustapa(suoritustapaNäyttö, Näyttö("Mittaus- ja korivauriotöitä", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      tila = None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", Some(1)),
        Some(date(2013, 4, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2013, 5, 31)))),
      osasuoritukset = None
    ),
    Suoritus(
      Some("suoritus-12345-4"),
      OpsTutkinnonosatoteutus(
        OpsTutkinnonosa(
          KoodistoKoodiViite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", Some(1)),
          true,
          Laajuus(15, opintojenLaajuusYksikkö)
        ),
        suoritustapa = Some(NäytöllinenSuoritustapa(suoritustapaNäyttö, Näyttö("Maalauksen esikäsittelytöitä", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", Some(1)),
        Some(date(2014, 10, 20)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2014, 11, 8)))),
      osasuoritukset = None
    ),
    Suoritus(
      Some("suoritus-12345-5"),
      OpsTutkinnonosatoteutus(
        OpsTutkinnonosa(
          KoodistoKoodiViite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat", Some(1)),
          true,
          Laajuus(15, opintojenLaajuusYksikkö)
        ),
        suoritustapa = Some(NäytöllinenSuoritustapa(suoritustapaNäyttö, Näyttö("Auton lisävarustetöitä", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      tila = None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", Some(1)),
        Some(date(2015, 4, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2015, 5, 1)))),
      osasuoritukset = None
    ),
    Suoritus(
      Some("suoritus-12345-6"),
      OpsTutkinnonosatoteutus(
        OpsTutkinnonosa(
          KoodistoKoodiViite("101050", Some("Yritystoiminnan suunnittelu"), "tutkinnonosat", Some(1)),
          true,
          Laajuus(15, opintojenLaajuusYksikkö)
        ),
        suoritustapa = Some(DefaultSuoritustapa(suoritustapaNäyttö)), // TODO: mikä suoritustapa tunnustetulle osaamiselle?
        hyväksiluku = Some(Hyväksiluku(OpsTutkinnonosa(KoodistoKoodiViite("100238", Some("Asennushitsaus"), "tutkinnonosat", Some(1)), true, Laajuus(15, opintojenLaajuusYksikkö)), Some("Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta")))
      ),
      suorituskieli = None,
      tila = None,
      alkamispäivä = None,
      toimipiste,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", Some(1)),
        Some(date(2016, 2, 1)),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2016, 5, 1)))),
      osasuoritukset = None
    )
  )

  val perustutkintoNäyttönä = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      OpiskeluOikeus(
        Some(983498343),
        Some(LähdejärjestelmäId("847823465", lähdeWinnova)),
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
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
          Some(Vahvistus(Some(date(2016, 1, 9)))),
          Some(tutkinnonOsat)
        ),
        hojks = None,
        Some(KoodistoKoodiViite("tutkinto", Some("Tutkinto"), "opintojentavoite", None)),
        Some(OpiskeluoikeudenTila(
          List(
            Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen),
            Opiskeluoikeusjakso(date(2016, 1, 9), None, opiskeluoikeusPäättynyt)
          )
        )),
        None,
        Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))
      )
    )
  )

  val uusiOppijaEiSuorituksia = TorOppija(
    Henkilö("010101-123N", "matti pekka", "matti", "virtanen"),
    List(
      OpiskeluOikeus(
        None,
        Some(LähdejärjestelmäId("847823465", lähdeWinnova)),
        Some(date(2016, 9, 1)),
        Some(date(2020, 5, 1)),
        None,
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        Suoritus(
          Some("suoritus-12345"),
          TutkintoKoulutustoteutus(TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")), None, None, None, None),
          None,
          None,
          Some(date(2015, 9, 1)),
          toimipiste,
          None,
          None,
          None
        ),
        hojks = None,
        None,
        Some(OpiskeluoikeudenTila(
          List(
            Opiskeluoikeusjakso(date(2016, 9, 1), None, opiskeluoikeusAktiivinen)
          )
        )),
        None,
        None
      )
    )
  )

  val oppisopimus = TorOppija(
    Henkilö("010101-123N", "matti pekka", "matti", "virtanen"),
    List(
      OpiskeluOikeus(
        None,
        Some(LähdejärjestelmäId("847823465", lähdeWinnova)),
        Some(date(2016, 9, 1)),
        Some(date(2020, 5, 1)),
        None,
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        Suoritus(
          Some("suoritus-12345"),
          TutkintoKoulutustoteutus(
            TutkintoKoulutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
            None, None, Some(DefaultSuoritustapa(suoritustapaNäyttö)),
            Some(OppisopimuksellinenJärjestämismuoto(järjestämismuotoOppisopimus, Oppisopimus(Yritys("Autokorjaamo Oy", "1234567-8"))))),
          None,
          None,
          None,
          toimipiste,
          None,
          None,
          None
        ),
        hojks = None,
        None,
        Some(OpiskeluoikeudenTila(
          List(
            Opiskeluoikeusjakso(date(2016, 9, 1), None, opiskeluoikeusAktiivinen)
          )
        )),
        None,
        None
      )
    )
  )

  val perustutkintoOps = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      OpiskeluOikeus(
        Some(983498343),
        Some(LähdejärjestelmäId("847823465", lähdeWinnova)),
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
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
                OpsTutkinnonosa(KoodistoKoodiViite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", None), true, Laajuus(15, opintojenLaajuusYksikkö)),
                suoritustapa = Some(DefaultSuoritustapa(suoritustapaOps))
              ),
              suorituskieli = None,
              tila = None,
              alkamispäivä = None,
              toimipiste,
              Some(
                List(
                  Arviointi(
                    arvosana = KoodistoKoodiViite("2", Some("H2"), "ammatillisenperustutkinnonarviointiasteikko", None),
                    Some(date(2014, 5, 20))
                  ),
                  Arviointi(
                    arvosana = KoodistoKoodiViite("3", Some("K3"), "ammatillisenperustutkinnonarviointiasteikko", None),
                    Some(date(2014, 10, 20))
                  )
                )
              ),
              Some(Vahvistus(Some(date(2014, 11, 8)))),
              osasuoritukset = None
            )
          ))
        ),
        hojks = Some(Hojks(hojksTehty = true, opetusryhmä = Some(KoodistoKoodiViite("1", Some("Yleinen opetusryhmä"), "opetusryhma", None)))),
        Some(KoodistoKoodiViite("tutkinto", Some("Tutkinto"), "opintojentavoite", None)),
        Some(OpiskeluoikeudenTila(
          List(
            Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2012, 12, 31)), opiskeluoikeusAktiivinen),
            Opiskeluoikeusjakso(date(2013, 1, 1), Some(date(2013, 12, 31)), opiskeluoikeusKeskeyttänyt),
            Opiskeluoikeusjakso(date(2014, 1, 1), None, opiskeluoikeusAktiivinen)
          )
        )),
        Some(Läsnäolotiedot(List(
          Läsnäolojakso(date(2012, 9, 1), Some(date(2012, 12, 31)), KoodistoKoodiViite("lasna", Some("Läsnä"), "lasnaolotila", Some(1))),
          Läsnäolojakso(date(2013, 1, 1), Some(date(2013, 12, 31)), KoodistoKoodiViite("poissa", Some("Poissa"), "lasnaolotila", Some(1))),
          Läsnäolojakso(date(2014, 1, 1), None, KoodistoKoodiViite("lasna", Some("Läsnä"), "lasnaolotila", Some(1)))
        ))),
        Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", Some(1)))
  )))

  val examples = List(
    Example("uusi", "Uusi oppija lisätään suorittamaan Autoalan perustutkintoa", uusiOppijaEiSuorituksia),
    Example("oppisopimus", "Uusi oppija, suorittaa oppisopimuksella", oppisopimus),
    Example("full", "Isompi esimerkki. Suorittaa perustutkintoa näyttönä. Tähän lisätty lähes kaikki kaavaillut tietokentät.", perustutkintoNäyttönä),
    Example("ops", "Perustutkinto ops:n mukaan, läsnäolotiedoilla, hojks", perustutkintoOps)
  )

}

case class Example(name: String, description: String, data: TorOppija)
