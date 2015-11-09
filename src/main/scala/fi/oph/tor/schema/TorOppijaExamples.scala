package fi.oph.tor.schema

import java.time.LocalDate.{of => date}
import fi.oph.tor.schema.Läsnäolojakso

object TorOppijaExamples {
  private val näyttö = Näyttö("Toimi automekaanikkona kolarikorjauspuolella kaksi vuotta", "Autokorjaamo Oy, Riihimäki")

  private val tutkinnonOsat = List(
    Suoritus(
      TutkinnonosatoteutusOps(
        KoodistoKoodiViite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat", 1),
        true
      ),
      suorituskieli = None,
      Some(Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1))),
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
      Some(Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1))),
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
      Some(Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1))),
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
      Some(Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1))),
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
      Some(Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1))),
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
      Some(Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1))),
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
  val perustutkintoNäyttönä = TorOppija(
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
        Suoritus(Koulutustoteutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4), Some("39/011/2014"), Some(KoodistoKoodiViite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", 2)), Some(KoodistoKoodiViite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", 3))),
          Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", 1)),
          Some(SuoritustapaNaytto(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1), Some(näyttö))),
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
      OpintoOikeus(
        None,
        Some(date(2016, 9, 1)),
        Some(date(2020, 5, 1)),
        None,
        Organisaatio("1.2.246.562.10.346830761110", Some("HELSINGIN KAUPUNKI")),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        None,
        Suoritus(Koulutustoteutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4), Some("39/011/2014"), None, None),
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
      OpintoOikeus(
        None,
        Some(date(2016, 9, 1)),
        Some(date(2020, 5, 1)),
        None,
        Organisaatio("1.2.246.562.10.346830761110", Some("HELSINGIN KAUPUNKI")),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        None,
        Suoritus(Koulutustoteutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4), Some("39/011/2014"), None, None),
          None,
          Some(SuoritustapaOppisopimus(KoodistoKoodiViite("oppisopimus", Some("Oppisopimus"), "suoritustapa", 1), Some(näyttö), Some(Oppisopimus(Yritys("Autokorjaamo Oy", "1234567-8"))))),
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
      OpintoOikeus(
        Some(983498343),
        Some(date(2012, 9, 1)),
        Some(date(2015, 5, 31)),
        Some(date(2016, 1, 9)),
        Organisaatio("1.2.246.562.10.346830761110", Some("HELSINGIN KAUPUNKI")),
        Organisaatio("1.2.246.562.10.52251087186", Some("Stadin ammattiopisto")),
        Some(Organisaatio("1.2.246.562.10.42456023292", Some("Stadin ammattiopisto, Lehtikuusentien toimipaikka"))),
        Suoritus(Koulutustoteutus(KoodistoKoodiViite("351301", Some("Autoalan perustutkinto"), "koulutus", 4), Some("39/011/2014"), Some(KoodistoKoodiViite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", 2)), Some(KoodistoKoodiViite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", 3))),
          Some(KoodistoKoodiViite("FI", Some("suomi"), "kieli", 1)),
          Some(Suoritustapa(KoodistoKoodiViite("ops", Some("Ops"), "suoritustapa", 1))),
          Some(KoodistoKoodiViite("KESKEN", Some("Suoritus kesken"), "suorituksentila", 1)),
          alkamispäivä = Some(date(2012, 9, 1)),
          arviointi = None,
          None,
          Some(List(
            Suoritus(
              TutkinnonosatoteutusOps(
                KoodistoKoodiViite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", 1),
                true
              ),
              suorituskieli = None,
              Some(Suoritustapa(KoodistoKoodiViite("naytto", Some("Näyttö"), "suoritustapa", 1))),
              tila = None,
              alkamispäivä = None,
              Some(Arviointi(
                arvosana = KoodistoKoodiViite("3", Some("K3"), "ammatillisenperustutkinnonarviointiasteikko", 1),
                Some(date(2014, 10, 20)),
                arvosananKorottaminen = Some(true)
              )),
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
      )
    )
  )

  val examples = List(
    Example("uusi", "Uusi oppija lisätään suorittamaan Autoalan perustutkintoa", uusiOppijaEiSuorituksia),
    Example("oppisopimus", "Uusi oppija, suorittaa oppisopimuksella", oppisopimus),
    Example("full", "Isompi esimerkki. Suorittaa perustutkintoa näyttönä. Tähän lisätty lähes kaikki kaavaillut tietokentät.", perustutkintoNäyttönä),
    Example("ops", "Perustutkinto ops:n mukaan, läsnäolotiedoilla, hojks", perustutkintoOps)
  )

}

case class Example(name: String, description: String, oppija: TorOppija)