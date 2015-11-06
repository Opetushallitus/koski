package fi.oph.tor.schema

import java.time.LocalDate.{of => date}

object TorOppijaExamples {
  private val tutkinnonOsat = List(
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat", 1),
        true
      ),
      suorituskieli = None,
      Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
      tila = None,
      alkamispäivä = None,
      Some(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2012, 10, 20)),
        arvosananKorottaminen = None
      )),
      Some(Vahvistus(Some(date(2013, 1, 31)))),
      osasuoritukset = None
    ),
    Suoritus(
      TutkinnonosatoteutusPaikallinen(
        Paikallinenkoodi("123456789", "Pintavauriotyöt", "kallion_oma_koodisto"),
        "Opetellaan korjaamaan pinnallisia vaurioita",
        false
      ),
      suorituskieli = None,
      Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
      tila = None,
      alkamispäivä = None,
      Some(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2013, 3, 20)),
        arvosananKorottaminen = None
      )),
      Some(Vahvistus(Some(date(2013, 5, 31)))),
      osasuoritukset = None
    ),
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("100019", Some("Mittaus- ja korivauriotyöt"), "tutkinnonosat", 1),
        true
      ),
      suorituskieli = None,
      Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
      tila = None,
      alkamispäivä = None,
      Some(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2013, 4, 1)),
        arvosananKorottaminen = None
      )),
      Some(Vahvistus(Some(date(2013, 5, 31)))),
      osasuoritukset = None
    ),
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", 1),
        true
      ),
      suorituskieli = None,
      Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
      tila = None,
      alkamispäivä = None,
      Some(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2014, 10, 20)),
        arvosananKorottaminen = None
      )),
      Some(Vahvistus(Some(date(2014, 11, 8)))),
      osasuoritukset = None
    ),
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat", 1),
        true
      ),
      suorituskieli = None,
      Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
      tila = None,
      alkamispäivä = None,
      Some(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2015, 4, 1)),
        arvosananKorottaminen = None
      )),
      Some(Vahvistus(Some(date(2015, 5, 1)))),
      osasuoritukset = None
    ),
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("101050", Some("Yritystoiminnan suunnittelu"), "tutkinnonosat", 1),
        true
      ),
      suorituskieli = None,
      Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)), //Optional??
      tila = None,
      alkamispäivä = None,
      Some(Arviointi(
        arvosana = KoodistoKoodiViite("Hyväksytty", Some("Hyväksytty"), "ammattijaerikoisammattitutkintojenarviointiasteikko", 1),
        Some(date(2016, 2, 1)),
        arvosananKorottaminen = None
      )),
      Some(Vahvistus(Some(date(2016, 5, 1)))),
      osasuoritukset = None
    )
  )
  private val autoalanPerustutkinto: Koulutustoteutus = Koulutustoteutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4), Some("39/011/2014"), Some(KoodistoKoodiViite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", 2)), Some(KoodistoKoodiViite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", 3)))

  val full = TorOppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      OpintoOikeus(
        Some(983498343),
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        Organisaatio("1.2.246.562.10.346830761110", Some("HELSINGIN KAUPUNKI")),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        Some(Organisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
        Suoritus(
          autoalanPerustutkinto,
          Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", 1)),
          Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)),
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
    Henkilö(None, Some("010101-123N"), Some("matti pekka"), Some("matti"), Some("virtanen")),
    List(
      OpintoOikeus(
        None,
        Some(date(2016, 9, 1)),
        Some(date(2020, 5, 1)),
        None,
        Organisaatio("1.2.246.562.10.346830761110", Some("HELSINGIN KAUPUNKI")),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        None,
        Suoritus(
          autoalanPerustutkinto,
          Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", 1)),
          Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1)),
          Some(KoodistoKoodiViite("KESKEN", Some("Kesken"), "suorituksentila", 1)),
          None,
          None,
          None,
          None
        ),
        hojks = None,
        Some(KoodistoKoodiViite("tutkinto", Some("Tutkinto"), "tavoite", 1)),
        None,
        Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", 1))
      )
    )
  )

  val examples = List(
    Example("uusi", "Uusi oppija lisätään suorittamaan Autoalan perustutkintoa", uusiOppijaEiSuorituksia),
    Example("full", "Esimerkki, johon lisätty lähes kaikki kaavaillut tietokentät.", full)
  )

}

case class Example(name: String, description: String, oppija: TorOppija)