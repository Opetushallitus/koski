package fi.oph.tor.schema

import java.time.LocalDate.{of => date}

object TorOppijaExamples {
  private val näyttö = Näyttö("Toimi automekaanikkona kolarikorjauspuolella kaksi vuotta", "Autokorjaamo Oy, Riihimäki")
  private val suoritustapaNäyttö = KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustavat", 1)
  private val suoritustapaOps = KoodistoKoodiViite("ops", Some("Opetussuunnitelmaperusteinen"), "suoritustavat", 1)
  private val järjestämismuotoOppisopimus = KoodistoKoodiViite("20", Some("Oppisopimusmuotoinen"), "jarjestamismuoto", 1)
  private val järjestämismuotoOppilaitos: KoodistoKoodiViite = KoodistoKoodiViite("10", Some("Oppilaitosmuotoinen"), "jarjestamismuoto", 1)

  private val tutkinnonOsat = List(
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat", 1),
        true,
        suoritustapa = Some(SuoritustapaNäytöllä(suoritustapaNäyttö, Näyttö("Huolto- ja korjaustyöt", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      None,
      tila = None,
      alkamispäivä = None,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2012, 10, 20)),
        arvosananKorottaminen = None,
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2013, 1, 31)))),
      osasuoritukset = None
    ),
    Suoritus(
      TutkinnonosatoteutusPaikallinen(
        Paikallinenkoodi("123456789", "Pintavauriotyöt", "kallion_oma_koodisto"),
        "Opetellaan korjaamaan pinnallisia vaurioita",
        false,
        suoritustapa = Some(SuoritustapaNäytöllä(suoritustapaNäyttö, Näyttö("Pintavaurioiden korjausta", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      None,
      tila = None,
      alkamispäivä = None,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2013, 3, 20)),
        arvosananKorottaminen = None,
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2013, 5, 31)))),
      osasuoritukset = None
    ),
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("100019", Some("Mittaus- ja korivauriotyöt"), "tutkinnonosat", 1),
        true,
        suoritustapa = Some(SuoritustapaNäytöllä(suoritustapaNäyttö, Näyttö("Mittaus- ja korivauriotöitä", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      None,
      tila = None,
      alkamispäivä = None,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2013, 4, 1)),
        arvosananKorottaminen = None,
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2013, 5, 31)))),
      osasuoritukset = None
    ),
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", 1),
        true,
        suoritustapa = Some(SuoritustapaNäytöllä(suoritustapaNäyttö, Näyttö("Maalauksen esikäsittelytöitä", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      None,
      tila = None,
      alkamispäivä = None,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2014, 10, 20)),
        arvosananKorottaminen = None,
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2014, 11, 8)))),
      osasuoritukset = None
    ),
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat", 1),
        true,
        suoritustapa = Some(SuoritustapaNäytöllä(suoritustapaNäyttö, Näyttö("Auton lisävarustetöitä", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      None,
      tila = None,
      alkamispäivä = None,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2015, 4, 1)),
        arvosananKorottaminen = None,
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      Some(Vahvistus(Some(date(2015, 5, 1)))),
      osasuoritukset = None
    ),
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("101050", Some("Yritystoiminnan suunnittelu"), "tutkinnonosat", 1),
        true,
        suoritustapa = Some(SuoritustapaNäytöllä(suoritustapaNäyttö, Näyttö("Yritystoiminnan suunnittelua", "Autokorjaamo Oy, Riihimäki")))
      ),
      suorituskieli = None,
      None,
      tila = None,
      alkamispäivä = None,
      Some(List(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2016, 2, 1)),
        arvosananKorottaminen = None,
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
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        Organisaatio("1.2.246.562.10.346830761110", Some("HELSINGIN KAUPUNKI")),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        Some(Organisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
        Suoritus(
          Koulutustoteutus(
            KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4),
            Some("39/011/2014"), Some(List(KoodistoKoodiViite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", 2))),
            Some(List(KoodistoKoodiViite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", 3))),
            suoritustapa = Some(DefaultSuoritustapa(suoritustapaNäyttö)),
            järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos))
          ),
          suorituskieli = Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", 1)),
          None,
          Some(KoodistoKoodiViite("VALMIS", Some("Valmis"), "suorituksentila", 1)),
          alkamispäivä = None,
          arviointi = None,
          Some(Vahvistus(Some(date(2016, 1, 9)))),
          Some(tutkinnonOsat)
        ),
        hojks = None,
        Some(KoodistoKoodiViite("tutkinto", Some("Tutkinto"), "tavoite", 1)),
        None,
        Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", 1))
      )
    )
  )

  val uusiOppijaEiSuorituksia = TorOppija(
    Henkilö("010101-123N", "matti pekka", "matti", "virtanen"),
    List(
      OpiskeluOikeus(
        None,
        Some(date(2016, 9, 1)),
        Some(date(2020, 5, 1)),
        None,
        Organisaatio("1.2.246.562.10.346830761110", Some("HELSINGIN KAUPUNKI")),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        None,
        Suoritus(
          Koulutustoteutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4), Some("39/011/2014"), None, None, None, None),
          None,
          None,
          None,
          Some(date(2015, 9, 1)),
          None,
          None,
          None
        ),
        hojks = None,
        None,
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
        Some(date(2016, 9, 1)),
        Some(date(2020, 5, 1)),
        None,
        Organisaatio("1.2.246.562.10.346830761110", Some("HELSINGIN KAUPUNKI")),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        None,
        Suoritus(
          Koulutustoteutus(
            KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4),
            Some("39/011/2014"), None, None, Some(DefaultSuoritustapa(suoritustapaNäyttö)),
            Some(JärjestämismuotoOppisopimuksella(järjestämismuotoOppisopimus, Oppisopimus(Yritys("Autokorjaamo Oy", "1234567-8"))))),
          None,
          None,
          None,
          None,
          None,
          None,
          None
        ),
        hojks = None,
        None,
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
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        Organisaatio("1.2.246.562.10.346830761110", Some("HELSINGIN KAUPUNKI")),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        Some(Organisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
        Suoritus(
          Koulutustoteutus(
            KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4),
            Some("39/011/2014"),
            Some(List(KoodistoKoodiViite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", 2))),
            Some(List(KoodistoKoodiViite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", 3))),
            Some(DefaultSuoritustapa(suoritustapaOps)),
            Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos))
          ),
          Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", 1)),
          None,
          None,
          None,
          None,
          None,
          Some(List(
            Suoritus(
              TutkinnonosatoteutusOps(
                KoodistoKoodiViite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", 1),
                true,
                suoritustapa = Some(DefaultSuoritustapa(suoritustapaOps))
              ),
              suorituskieli = None,
              None,
              tila = None,
              alkamispäivä = None,
              Some(
                List(
                  Arviointi(
                    arvosana = KoodistoKoodiViite("2", Some("H2"), "ammatillisenperustutkinnonarviointiasteikko", 1),
                    Some(date(2014, 5, 20)),
                    arvosananKorottaminen = None,
                    None
                  ),
                  Arviointi(
                    arvosana = KoodistoKoodiViite("3", Some("K3"), "ammatillisenperustutkinnonarviointiasteikko", 1),
                    Some(date(2014, 10, 20)),
                    arvosananKorottaminen = Some(true),
                    None
                  )
                )
              ),
              Some(Vahvistus(Some(date(2014, 11, 8)))),
              osasuoritukset = None
            )
          ))
        ),
        hojks = Some(Hojks(hojksTehty = true)),
        Some(KoodistoKoodiViite("tutkinto", Some("Tutkinto"), "tavoite", 1)),
        Some(Läsnäolotiedot(List(
          Läsnäolojakso(date(2012, 9, 1), Some(date(2012, 12, 31)), KoodistoKoodiViite("lasna", Some("Läsnä"), "lasnaolotila", 1)),
          Läsnäolojakso(date(2013, 1, 1), Some(date(2013, 12, 31)), KoodistoKoodiViite("poissa", Some("Poissa"), "lasnaolotila", 1)),
          Läsnäolojakso(date(2014, 1, 1), None, KoodistoKoodiViite("lasna", Some("Läsnä"), "lasnaolotila", 1))
        ))),
        Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", 1))
  )))

  val examples = List(
    Example("uusi", "Uusi oppija lisätään suorittamaan Autoalan perustutkintoa", uusiOppijaEiSuorituksia),
    Example("oppisopimus", "Uusi oppija, suorittaa oppisopimuksella", oppisopimus),
    Example("full", "Isompi esimerkki. Suorittaa perustutkintoa näyttönä. Tähän lisätty lähes kaikki kaavaillut tietokentät.", perustutkintoNäyttönä),
    Example("ops", "Perustutkinto ops:n mukaan, läsnäolotiedoilla, hojks", perustutkintoOps)
  )

}

case class Example(name: String, description: String, oppija: TorOppija)