package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.omnia
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

import java.time.LocalDate

object ExamplesAmmatillinen {
  lazy val examples = List(
    Example("ammatillinen - uusi", "Uusi oppija lisätään suorittamaan Autoalan perustutkintoa", AmmatillinenOldExamples.uusi),
    Example("ammatillinen - oppisopimus", "Uusi oppija, suorittaa oppisopimuksella", AmmatillinenOldExamples.oppisopimus),
    Example("ammatillinen - paikallinen", "Oppija on suorittanut paikallisen tutkinnon osan", AmmatillinenOldExamples.paikallinen),
    Example("ammatillinen - mukautettu", "Tutkinnon osan arviointia on mukautettu", AmmatillinenOldExamples.mukautettu),
    Example("ammatillinen - osatoisestatutkinnosta", "Oppija on suorittanut toiseen tutkintoon liittyvän tutkinnon osan", AmmatillinenOldExamples.tutkinnonOsaToisestaTutkinnosta),
    Example("ammatillinen - full", "Isompi esimerkki. Suorittaa perustutkintoa näyttönä. Tähän lisätty lähes kaikki kaavaillut tietokentät.", AmmatillinenOldExamples.full),
    Example("ammatillinen - ops", "Perustutkinto ops:n mukaan, läsnäolotiedoilla, hojks", AmmatillinenOldExamples.ops),
    Example("ammatillinen - perustutkinto", "Ympäristönhoitajaksi valmistunut opiskelija", AmmatillinenPerustutkintoExample.perustutkinto),
    Example("ammatillinen - reformin mukainen perustutkinto", "Autoalan perustutkinto", AmmatillinenReforminMukainenPerustutkintoExample.example),
    Example("ammatillinen - reformin mukainen erikoisammattitutkinto", "Automekaanikon erikoisammattitutkinto", ReforminMukainenErikoisammattitutkintoExample.example),
    Example("ammatillinen - reformin mukainen perustutkinto 2022", "Ajoneuvoalan perustutkinto", AmmatillinenReforminMukainenPerustutkinto2022Example.example),
    Example("ammatillinen - erikoisammattitutkinto", "Erikoisammattitutkinnon ja näyttötutkintoon valmistavan koulutuksen suorittanut opiskelija", AmmattitutkintoExample.erikoisammattitutkinto),
    Example("ammatillinen - tutkinnonosa", "Yhden tutkinnon osan suorittanut oppija", AmmatillinenPerustutkintoExample.osittainenPerustutkinto),
    Example("ammatillinen - reformi useita tutkinnon osia", "Useita tutkinnon osia suorittanut oppija", AmmatillinenOsittainenReformi.laaja),
    Example("ammatillinen - tunnustettu", "Tutkinnon osa tunnustettu aiemmin suoritetusta paikallisen tutkinnon osasta", AmmatillinenPerustutkintoExample.tunnustettuPaikallinenTutkinnonOsa),
    Example("ammatillinen - sisältyy toisen oppilaitoksen opiskeluoikeuteen", "Toisen oppilaitoksen opiskeluoikeuteen sisältyvä opiskeluoikeus", AmmatillinenPerustutkintoExample.sisältyvä, statusCode = 400),
    Example("ammatillinen - lisätiedot", "Opiskeluoikeus, johon liitetty kaikki mahdolliset opiskeluoikeuden lisätiedot", LisätiedotExample.example)
  )
}

object LisätiedotExample {
  val example = AmmatillinenExampleData.oppija(opiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus().copy(lisätiedot = Some(AmmatillinenExampleData.opiskeluoikeudenLisätiedot)))
}

object ReforminMukainenErikoisammattitutkintoExample {
  lazy val tutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357304", Some("Automekaanikon erikoisammattitutkinto"), "koulutus", None), Some("OPH-1886-2017"))

  lazy val osasuoritukset = List(
    tutkinnonOsanSuoritus("300054", "Korjauksen haltuunotto", None, arvosanaViisi).copy(
      näyttö = Some(
        näyttö(
          date(2018, 2, 2),
          "Vuosihuoltojen suorittaminen",
          "Volkswagen Center",
          Some(näytönArviointi.copy(
            arvioinnistaPäättäneet = Some(List(Koodistokoodiviite("5", Some("Muu koulutuksen järjestäjän edustaja"), "ammatillisennaytonarvioinnistapaattaneet", None))),
            arvosana = arvosanaViisi,
            arviointikohteet = arviointikohteet15
          ))
        )
      )
    ),
    tutkinnonOsanSuoritus("300051", "Ajoneuvon vianmääritys", None, arvosanaViisi),
    tutkinnonOsanSuoritus("300057", "Teknisenä asiantuntijana toiminen", None, arvosanaViisi),
    tutkinnonOsanSuoritus("300058", "Testaus ja kilpailutoiminta", None, arvosanaViisi)
  )

  lazy val tutkinnonSuoritus = AmmatillisenTutkinnonSuoritus(
    koulutusmoduuli = tutkinto,
    suoritustapa = suoritustapaReformi,
    osaamisenHankkimistavat = Some(List(
      OsaamisenHankkimistapajakso(date(2018, 1, 1), None, osaamisenHankkimistapaOppilaitos),
      OsaamisenHankkimistapajakso(date(2018, 8, 1), None, osaamisenHankkimistapaOppisopimus)
    )),
    koulutussopimukset = Some(List(
      Koulutussopimusjakso(
        alku = date(2018, 8, 1),
        loppu = None,
        työssäoppimispaikka = Some("Volkswagen Center"),
        työssäoppimispaikanYTunnus = Some("1572860-0"),
        paikkakunta = jyväskylä,
        maa = suomi,
        työtehtävät = Some(finnish("Autojen vuosihuollot"))
      )
    )),
    suorituskieli = suomenKieli,
    alkamispäivä = None,
    toimipiste = stadinToimipiste,
    osasuoritukset = Some(osasuoritukset)
  )

  lazy val opiskeluoikeus = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2020, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2018, 1, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
      hojks = None,
      erityinenTuki = Some(List(Aikajakso(date(2018, 1, 1), None))),
      vaativanErityisenTuenErityinenTehtävä = Some(List(Aikajakso(date(2018,1,1), None)))
    )),
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(
      tutkinnonSuoritus
    )
  )

  lazy val example = Oppija(
    exampleHenkilö,
    List(opiskeluoikeus)
  )
}

object AmmattitutkintoExample {
  lazy val tutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(
    Koodistokoodiviite("357305", Some("Autoalan työnjohdon erikoisammattitutkinto"), "koulutus", None),
    Some("40/011/2001")
  )

  lazy val näyttötutkintoonValmistavanKoulutuksenSuoritus = NäyttötutkintoonValmistavanKoulutuksenSuoritus(
    tutkinto = tutkinto,
    alkamispäivä = Some(date(2012, 9, 1)),
    päättymispäivä = None,
    toimipiste = stadinToimipiste,
    vahvistus = vahvistus(date(2015, 5, 31), stadinAmmattiopisto, Some(helsinki)),
    suorituskieli = suomenKieli,
    osasuoritukset = Some(List(
      NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(
        koulutusmoduuli = PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa(
          PaikallinenKoodi("104052", finnish("Johtaminen ja henkilöstön kehittäminen")),
          "Johtamisen ja henkilöstön kehittämisen valmistava koulutus"
        )
      ),
      NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(
        koulutusmoduuli = autonLisävarustetyöt(false, "valojärjestelmät")
      ),
      NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(
        koulutusmoduuli = autonLisävarustetyöt(false, "lämmitysjärjestelmät")
      )
    ))
  )

  lazy val ammatillisenTutkinnonSuoritus = AmmatillisenTutkinnonSuoritus(
    koulutusmoduuli = tutkinto,
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
      tutkinnonOsanSuoritus("104052", "Johtaminen ja henkilöstön kehittäminen", None, hyväksytty),
      tutkinnonOsanSuoritus("104053", "Asiakaspalvelu ja korjaamopalvelujen markkinointi", None, hyväksytty),
      tutkinnonOsanSuoritus("104054", "Työnsuunnittelu ja organisointi", None, hyväksytty),
      tutkinnonOsanSuoritus("104055", "Taloudellinen toiminta", None, hyväksytty),
      tutkinnonOsanSuoritus("104059", "Yrittäjyys", None, hyväksytty)
    ))
  )

  lazy val opiskeluoikeus = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(date(2016, 8, 31), opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(
      näyttötutkintoonValmistavanKoulutuksenSuoritus,
      ammatillisenTutkinnonSuoritus
    )
  )

  lazy val erikoisammattitutkinto = Oppija(
    exampleHenkilö,
    List(opiskeluoikeus)
  )
}

object AmmatillinenPerustutkintoExample {

  import fi.oph.koski.documentation.AmmatillinenExampleData._

  val perustutkinto = oppija(opiskeluoikeus = perustutkintoOpiskeluoikeusValmis(valmistumispäivä = date(2016, 8, 31)))

  val osittainenPerustutkintoOpiskeluoikeus = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(ammatillisenTutkinnonOsittainenSuoritus),
    tila = AmmatillinenOpiskeluoikeudenTila(
      List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
        AmmatillinenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
      )
    )
  )

  def perustutkintoOpiskeluoikeusValmisOrganisaatiohistorialla(
    koulutustoimija: Koulutustoimija = kiipulasäätiö,
    oppilaitos: Oppilaitos = kiipulanAmmattiopisto,
    toimipiste: OrganisaatioWithOid = kiipulanAmmattiopistoNokianToimipaikka,
    organisaatioHistorianOppilaitos: Oppilaitos = kiipulanAmmattiopisto,
    vahvistuksenOrganisaatio: OrganisaatioWithOid = kiipulanAmmattiopisto
  ): AmmatillinenOpiskeluoikeus =
    AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis(
      oppilaitos = oppilaitos,
      toimipiste = toimipiste
    ).copy(
      koulutustoimija = Some(koulutustoimija),
      organisaatiohistoria = Some(List(
        OpiskeluoikeudenOrganisaatiohistoria(
          muutospäivä = LocalDate.of(2013, 1, 1),
          oppilaitos = Some(organisaatioHistorianOppilaitos),
          koulutustoimija = Some(Koulutustoimija(
            oid = MockOrganisaatiot.helsinginKaupunki,
            nimi = Some(Finnish(fi = "Helsingin kaupunki"))
          ))
        )
      )),
      suoritukset = List(
        AmmatillinenExampleData.ympäristöalanPerustutkintoValmis(toimipiste).copy(
          vahvistus = vahvistus(date(2016, 5, 31), vahvistuksenOrganisaatio, Some(helsinki)),
          keskiarvo = Some(4.0)
        )
      )
    )

  val sisältyvä = oppija(opiskeluoikeus = opiskeluoikeus(tutkinto = autoalanPerustutkinnonSuoritus(OidOrganisaatio(omnia))).copy(
    sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(Oppilaitos(omnia), "1.2.246.562.15.84012103747"))
  ))

  lazy val osittainenPerustutkinto = Oppija(exampleHenkilö, List(osittainenPerustutkintoOpiskeluoikeus))

  lazy val tunnustettuPaikallinenTutkinnonOsaOpiskeluoikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinnonSuoritus().copy(suoritustapa = suoritustapaNäyttö),
    osat = Some(List(
      MuunAmmatillisenTutkinnonOsanSuoritus(
        koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100031", Some("Moottorin ja voimansiirron huolto ja korjaus"), "tutkinnonosat", None), false, Some(LaajuusOsaamispisteissä(15))),
        tunnustettu = Some(OsaamisenTunnustaminen(
          Some(MuunAmmatillisenTutkinnonOsanSuoritus(
            koulutusmoduuli = PaikallinenTutkinnonOsa(PaikallinenKoodi("11-22-33", "Moottorin korjaus"),
              """|Opiskelijan on
                 |- tunnettava jakopyörästön merkitys moottorin toiminnalle
                 |- osattava kytkeä moottorin testauslaite ja tulkita mittaustuloksen suhdetta
                 |valmistajan antamiin ohjearvoihin
                 |- osattava käyttää moottorikorjauksessa tarvittavia perustyökaluja
                 |- osattava suorittaa jakopään hammashihnan vaihto annettujen ohjeiden
                 |mukaisesti
                 |- tunnettava venttiilikoneiston merkitys moottorin toiminnan osana
                 |osatakseen mm. ottaa se huomioon jakopään huoltoja tehdessään
                 |- noudatettava sovittuja työaikoja""".stripMargin, false, None),
            toimipiste = None,
            näyttö = Some(näyttö(date(2002, 4, 20), "Moottorin korjaus", "Autokorjaamo Oy, Riihimäki")),
            vahvistus = vahvistusValinnaisellaTittelillä(date(2002, 5, 28), stadinAmmattiopisto, None)
          )),
          "Tutkinnon osa on tunnustettu aiemmin suoritetusta autoalan perustutkinnon osasta (1.8.2000 nro 11/011/2000)"
        )),
        toimipiste = Some(stadinToimipiste),
        arviointi = Some(List(arviointiHyväksytty)),
        tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat,
        vahvistus = vahvistusValinnaisellaTittelillä(date(2013, 5, 31), stadinAmmattiopisto)
      )
    ))
  )

  lazy val tunnustettuPaikallinenTutkinnonOsa = Oppija(exampleHenkilö.copy(hetu = "250266-497T"), List(tunnustettuPaikallinenTutkinnonOsaOpiskeluoikeus))
}

object AmmatillinenReforminMukainenPerustutkintoExample {
  lazy val tutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", Some(11)), Some("OPH-2762-2017"))
  lazy val korkeakouluopintoSuoritus = AmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus(koulutusmoduuli = KorkeakouluopinnotTutkinnonOsa(), osasuoritukset = Some(List(saksa)))

  lazy val jatkoOpintovalmiuksiaTukevienOpintojenSuoritus = AmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(koulutusmoduuli = JatkoOpintovalmiuksiaTukeviaOpintojaTutkinnonOsa(), osasuoritukset = Some(List(
    LukioOpintojenSuoritus(
      koulutusmoduuli = PaikallinenLukionOpinto(
        tunniste = PaikallinenKoodi("MAA", "Maantieto"),
        kuvaus = "Lukion maantiedon oppimäärä",
        perusteenDiaarinumero = "33/011/2003"
      ),
      arviointi = arviointiViisi,
      tyyppi = Koodistokoodiviite(koodiarvo = "ammatillinenlukionopintoja", koodistoUri = "suorituksentyyppi")
    ),
    LukioOpintojenSuoritus(
      koulutusmoduuli = PaikallinenLukionOpinto(
        tunniste = PaikallinenKoodi("EN", "Englanti"),
        kuvaus = "Englannin kurssi",
        laajuus = Some(LaajuusOsaamispisteissä(3)),
        perusteenDiaarinumero = "33/011/2003"
      ),
      arviointi = arviointiViisi,
      tyyppi = Koodistokoodiviite(koodiarvo = "ammatillinenlukionopintoja", koodistoUri = "suorituksentyyppi")
    ),
    YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(Koodistokoodiviite("TVT", "ammatillisenoppiaineet"), pakollinen = true, Some(LaajuusOsaamispisteissä(3))), arviointi = arviointiViisi),

    MuidenOpintovalmiuksiaTukevienOpintojenSuoritus(
      PaikallinenOpintovalmiuksiaTukevaOpinto(PaikallinenKoodi("htm", "Hoitotarpeen määrittäminen"), "Hoitotarpeen määrittäminen"),
      arviointi = arviointiViisi
    )
  )))

  lazy val opiskeluoikeus = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2020, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2018, 1, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
      hojks = None,
      erityinenTuki = Some(List(Aikajakso(date(2018, 1, 1), None))),
      vaativanErityisenTuenErityinenTehtävä = Some(List(Aikajakso(date(2018,1,1), None)))
    )),
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(
      AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = tutkinto,
        suoritustapa = suoritustapaReformi,
        osaamisenHankkimistavat = Some(List(
          OsaamisenHankkimistapajakso(date(2018, 1, 1), None, osaamisenHankkimistapaOppilaitos)
        )),
        tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
        osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1719", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None)))),
        koulutussopimukset = None,
        suorituskieli = suomenKieli,
        alkamispäivä = None,
        toimipiste = stadinToimipiste,
        osasuoritukset = Some(List(
          tutkinnonOsanSuoritus("105708", "Huolto- ja korjaustyöt", AmmatillinenExampleData.ammatillisetTutkinnonOsat, arvosanaViisi).copy(
            näyttö = Some(
              näyttö(
                date(2018, 2, 2),
                "Vuosihuoltojen suorittaminen",
                "Volkswagen Center",
                Some(näytönArviointi.copy(
                  arvioinnistaPäättäneet = Some(List(Koodistokoodiviite("5", Some("Muu koulutuksen järjestäjän edustaja"), "ammatillisennaytonarvioinnistapaattaneet", None))),
                  arvosana = arvosanaViisi,
                  arviointikohteet = arviointikohteet15
                ))
              )
            )
          ),
          tutkinnonOsanSuoritus("105715", "Maalauksen esikäsittelytyöt", AmmatillinenExampleData.ammatillisetTutkinnonOsat, arvosanaViisi, pakollinen = false).copy(
            näyttö = Some(
              näyttö(
                date(2018, 2, 2),
                "Pieniä pohja- ja hiomamaalauksia",
                "Volkswagen Center",
                Some(näytönArviointi.copy(
                  arvioinnistaPäättäneet = Some(List(Koodistokoodiviite("5", Some("Muu koulutuksen järjestäjän edustaja"), "ammatillisennaytonarvioinnistapaattaneet", None))),
                  arvosana = arvosanaViisi,
                  arviointikohteet = arviointikohteet15
                ))
              )
            )
          ),
          yhteisenTutkinnonOsanSuoritus("400012", "Viestintä- ja vuorovaikutusosaaminen", arvosanaViisi, 8).copy(
            osasuoritukset = Some(List(
              YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(5))), arviointi = arviointiViisi, näyttö = Some(näyttö(date(2014, 5, 18), "Kirjaesitelmä", "Stadin ammattiopisto"))),
              YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = false, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(3))), arviointi = arviointiViisi)
            ))
          ).copy(arviointi = None).copy(vahvistus = None),
          korkeakouluopintoSuoritus,
          jatkoOpintovalmiuksiaTukevienOpintojenSuoritus
        ))
      )
    )
  )

  lazy val saksa = KorkeakouluopintojenSuoritus(
    koulutusmoduuli = KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus(PaikallinenKoodi("de", finnish("Saksa")), "Saksa", Some(LaajuusOsaamispisteissä(5))),
    arviointi = arviointiViisi
  )

  lazy val example = Oppija(
    exampleHenkilö.copy(hetu = "020882-577H"),
    List(opiskeluoikeus)
  )
}

object AmmatillinenReforminMukainenPerustutkinto2022Example {
  lazy val tutkinto: AmmatillinenTutkintoKoulutus = ajoneuvoalanPerustutkinto

  lazy val opiskeluoikeus: AmmatillinenOpiskeluoikeus = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2026, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2022, 1, 8), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
      hojks = None,
    )),
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(
      AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = tutkinto,
        suoritustapa = suoritustapaReformi,
        osaamisenHankkimistavat = Some(List(
          OsaamisenHankkimistapajakso(date(2018, 1, 1), None, osaamisenHankkimistapaOppilaitos)
        )),
        tutkintonimike = Some(List(Koodistokoodiviite("10080", Some("Hyötyajoneuvomekaanikko"), "tutkintonimikkeet", None))),
        osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1008", Some("Ajoneuvotekniikan osaamisala"), "osaamisala", None)))),
        koulutussopimukset = None,
        suorituskieli = suomenKieli,
        alkamispäivä = None,
        toimipiste = stadinToimipiste,
        osasuoritukset = Some(List(
          tutkinnonOsanSuoritus("106945", "Ajoneuvon huoltotyöt", AmmatillinenExampleData.ammatillisetTutkinnonOsat, arvosanaViisi).copy(
            näyttö = Some(
              näyttö(
                date(2023, 2, 2),
                "Vuosihuoltojen suorittaminen",
                "Volkswagen Center",
                Some(näytönArviointi.copy(
                  arvioinnistaPäättäneet = Some(List(Koodistokoodiviite("5", Some("Muu koulutuksen järjestäjän edustaja"), "ammatillisennaytonarvioinnistapaattaneet", None))),
                  arvosana = arvosanaViisi,
                  arviointikohteet = arviointikohteet15
                ))
              )
            )
          ),
          tutkinnonOsanSuoritus("106943", "Ruiskumaalaustyöt", AmmatillinenExampleData.ammatillisetTutkinnonOsat, arvosanaViisi, pakollinen = false).copy(
            näyttö = Some(
              näyttö(
                date(2023, 2, 2),
                "Pieniä pohja- ja hiomamaalauksia",
                "Volkswagen Center",
                Some(näytönArviointi.copy(
                  arvioinnistaPäättäneet = Some(List(Koodistokoodiviite("5", Some("Muu koulutuksen järjestäjän edustaja"), "ammatillisennaytonarvioinnistapaattaneet", None))),
                  arvosana = arvosanaViisi,
                  arviointikohteet = arviointikohteet15
                ))
              )
            )
          ),
          yhteisenTutkinnonOsanSuoritus("106727", "Viestintä- ja vuorovaikutusosaaminen", arvosanaViisi, 8).copy(
            osasuoritukset = Some(List(
              YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(5))), arviointi = arviointiViisi, näyttö = Some(näyttö(date(2023, 5, 18), "Kirjaesitelmä", "Stadin ammattiopisto"))),
              YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = false, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(3))), arviointi = arviointiViisi)
            ))
          ).copy(arviointi = None).copy(vahvistus = None),
          AmmatillinenReforminMukainenPerustutkintoExample.korkeakouluopintoSuoritus,
          AmmatillinenReforminMukainenPerustutkintoExample.jatkoOpintovalmiuksiaTukevienOpintojenSuoritus
        ))
      )
    )
  )

  lazy val opiskeluoikeusJotpa = opiskeluoikeus.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2022, 1, 8), opiskeluoikeusLäsnä, Some(ExampleData.jatkuvanOppimisenRahoitus))
    ))
  )

  lazy val example: Oppija = Oppija(
    exampleHenkilö.copy(hetu = "060600A8482"),
    List(opiskeluoikeus)
  )
}

object AmmatillinenOldExamples {
  lazy val uusi: Oppija = oppija()

  lazy val oppisopimus = oppija(
    henkilö = exampleHenkilö.copy(hetu = "160586-873P"),
    opiskeluoikeus = opiskeluoikeus(
      oppilaitos = stadinAmmattiopisto,
      tutkinto = AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", Some(11)), Some("39/011/2014")),
        tutkintonimike = None,
        osaamisala = None,
        suoritustapa = suoritustapaNäyttö,
        järjestämismuodot = Some(List(Järjestämismuotojakso(date(2016, 9, 1), None, järjestämismuotoOppisopimus))),
        suorituskieli = suomenKieli,
        alkamispäivä = Some(date(2016, 9, 1)),
        toimipiste = stadinToimipiste,
        vahvistus = None,
        osasuoritukset = None
      )
    ).copy(tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2016, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
    )))
  )

  lazy val paikallinen = oppija(
    henkilö = exampleHenkilö.copy(hetu = "170694-385F"),
    opiskeluoikeus = opiskeluoikeus(
      tutkinto = autoalanPerustutkinnonSuoritus().copy(suoritustapa = suoritustapaNäyttö),
      osat = Some(List(paikallisenOsanSuoritus))
  ))

  lazy val mukautettu = oppija(opiskeluoikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinnonSuoritus().copy(suoritustapa = suoritustapaOps),
    osat = Some(List(
      YhteisenAmmatillisenTutkinnonOsanSuoritus(
        koulutusmoduuli = YhteinenTutkinnonOsa(Koodistokoodiviite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(LaajuusOsaamispisteissä(11))),
        lisätiedot = Some(List(AmmatillisenTutkinnonOsanLisätieto(
          Koodistokoodiviite("mukautettu", "ammatillisentutkinnonosanlisatieto"),
          "Tutkinnon osan ammattitaitovaatimuksia ja osaamisen arviointi on mukautettu (ja/tai niistä on poikettu) ammatillisesta peruskoulutuksesta annetun lain\n(630/1998, muutos 246/2015) 19 a (ja/tai 21) §:n perusteella"))),
        suorituskieli = None,
        alkamispäivä = None,
        toimipiste = Some(stadinToimipiste),
        arviointi = Some(List(arviointiKiitettävä.copy(kuvaus=Some("Erinomaista kehitystä")))),
        vahvistus = vahvistusValinnaisellaTittelillä(date(2014, 11, 8), stadinAmmattiopisto),
        tutkinnonOsanRyhmä = yhteisetTutkinnonOsat,
        osasuoritukset = Some(List(
          YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(11))), arviointi = Some(List(arviointiKiitettävä))),
        ))
      )
    ))
  ))

  lazy val muunAmmatillisenTutkinnonOsanSuoritus = MuunAmmatillisenTutkinnonOsanSuoritus(
    koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None),
    tutkinto = Some(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357305", "koulutus"), Some("40/011/2001"))),
    suorituskieli = None,
    alkamispäivä = None,
    toimipiste = Some(stadinToimipiste),
    arviointi = Some(List(arviointiKiitettävä)),
    vahvistus = vahvistusValinnaisellaTittelillä(date(2014, 11, 8), stadinAmmattiopisto),
    tutkinnonOsanRyhmä = yksilöllisestiLaajentavatTutkinnonOsat
  )

  lazy val tutkinnonOsaToisestaTutkinnosta = oppija(
    henkilö = exampleHenkilö.copy(hetu = "240550-475R"),
    opiskeluoikeus = opiskeluoikeus(
      tutkinto = AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", Some(11)), Some("39/011/2014")),
        suoritustapa = suoritustapaNäyttö,
        järjestämismuodot = None,
        toimipiste = stadinToimipiste,
        suorituskieli = suomenKieli
      ),
      osat = Some(List(muunAmmatillisenTutkinnonOsanSuoritus))
  ))

  lazy val ops = Oppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      AmmatillinenOpiskeluoikeus(
        arvioituPäättymispäivä = Some(date(2015, 5, 31)),
        oppilaitos = Some(stadinAmmattiopisto),
        suoritukset = List(AmmatillisenTutkinnonSuoritus(
          koulutusmoduuli = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", Some(11)), Some("39/011/2014")),
          tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None)))),
          suoritustapa = suoritustapaOps,
          järjestämismuodot = Some(List(Järjestämismuotojakso(date(2012, 9, 1), None, järjestämismuotoOppilaitos))),
          toimipiste = stadinToimipiste,
          suorituskieli = suomenKieli,

          osasuoritukset = Some(List(
            YhteisenAmmatillisenTutkinnonOsanSuoritus(
              koulutusmoduuli = YhteinenTutkinnonOsa(Koodistokoodiviite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(LaajuusOsaamispisteissä(11))),
              tunnustettu = None,
              näyttö = None,
              lisätiedot = None,
              tutkinto = None,
              suorituskieli = None,
              alkamispäivä = None,
              toimipiste = Some(stadinToimipiste),
              arviointi = Some(
                List(
                  AmmatillinenArviointi(
                    arvosana = h2,
                    date(2014, 5, 20)
                  ),
                  AmmatillinenArviointi(
                    arvosana = k3,
                    date(2014, 10, 20)
                  )
                )
              ),
              vahvistus = vahvistusValinnaisellaTittelillä(date(2014, 11, 8), stadinAmmattiopisto),
              tutkinnonOsanRyhmä = yhteisetTutkinnonOsat,
              osasuoritukset = Some(List(
                YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(Koodistokoodiviite("AI", "ammatillisenoppiaineet"), pakollinen = true, kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"), laajuus = Some(LaajuusOsaamispisteissä(11))), arviointi = Some(List(arviointiKiitettävä))),
              ))
            )
          ))
        )),
        lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
          hojks = Some(Hojks(
            opetusryhmä = Koodistokoodiviite("1", Some("Yleinen opetusryhmä"), "opetusryhma"),
            alku = Some(date(2012, 10, 20)),
            loppu = Some(date(2013, 8, 15))
          ))
        )),
        tila = AmmatillinenOpiskeluoikeudenTila(
          List(
            AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("1", Some("Valtionosuusrahoitteinen koulutus"), "opintojenrahoitus", None))),
            AmmatillinenOpiskeluoikeusjakso(date(2016, 1, 9), opiskeluoikeusEronnut, None)
          )
        )
      )))

  lazy val full = Oppija(
    Henkilö.withOid("1.2.246.562.24.00000000010"),
    List(
      AmmatillinenOpiskeluoikeus(
        arvioituPäättymispäivä = Some(date(2015, 5, 31)),
        oppilaitos = Some(stadinAmmattiopisto),
        suoritukset = List(AmmatillisenTutkinnonSuoritus(
          koulutusmoduuli = AmmatillinenTutkintoKoulutus(
            Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", Some(11)),
            Some("39/011/2014")
          ),
          tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(Osaamisalajakso(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None)))),
          suoritustapa = suoritustapaNäyttö,
          järjestämismuodot = Some(List(Järjestämismuotojakso(date(2012, 9, 1), None, järjestämismuotoOppilaitos))),
          suorituskieli = suomenKieli,
          alkamispäivä = None,
          toimipiste = stadinToimipiste,
          vahvistus = Some(HenkilövahvistusValinnaisellaPaikkakunnalla(date(2016, 1, 9), Some(helsinki), stadinAmmattiopisto, List(
            Organisaatiohenkilö("Mauri Bauer", "puheenjohtaja", tutkintotoimikunta),
            Organisaatiohenkilö("Reijo Reksi", "rehtori", stadinAmmattiopisto)))),
          osasuoritukset = Some(tutkinnonOsat),
          ryhmä = Some("AUT12SN")
        )),
        tila = AmmatillinenOpiskeluoikeudenTila(
          List(
            AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
            AmmatillinenOpiskeluoikeusjakso(date(2016, 1, 9), opiskeluoikeusValmistunut, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
          )
        ),
        ostettu = true
      )
    )
  )

  lazy val tutkinnonOsat = List(
    MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(
        Koodistokoodiviite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat"),
        true,
        laajuus = None
      ),
      näyttö = Some(näyttö(date(2012, 10, 20), "Huolto- ja korjaustyöt", "Autokorjaamo Oy, Riihimäki", Some(näytönArviointi))),
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2012, 10, 20),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistusValinnaisellaTittelillä(date(2013, 1, 31), stadinAmmattiopisto),
      tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat
    ),
    paikallisenOsanSuoritus,
    MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(
        Koodistokoodiviite("100019", Some("Mittaus- ja korivauriotyöt"), "tutkinnonosat"),
        true,
        None
      ),
      näyttö = Some(näyttö(date(2013, 4, 1), "Mittaus- ja korivauriotöitä", "Autokorjaamo Oy, Riihimäki")),
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2013, 4, 1),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistusValinnaisellaTittelillä(date(2013, 5, 31), stadinAmmattiopisto),
      tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat
    ),
    MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(
        Koodistokoodiviite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat"),
        true,
        None
      ),
      näyttö = Some(näyttö(date(2014, 10, 20), "Maalauksen esikäsittelytöitä", "Autokorjaamo Oy, Riihimäki")),
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2014, 10, 20),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistusValinnaisellaTittelillä(date(2014, 11, 8), stadinAmmattiopisto),
      tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat
    ),
    MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = autonLisävarustetyöt(true),
      näyttö = Some(näyttö(date(2015, 4, 1), "Auton lisävarustetöitä", "Autokorjaamo Oy, Riihimäki")),
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2015, 4, 1),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistusValinnaisellaTittelillä(date(2015, 5, 1), stadinAmmattiopisto),
      tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat
    ),
    MuunAmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = MuuValtakunnallinenTutkinnonOsa(
        Koodistokoodiviite("101050", Some("Yritystoiminnan suunnittelu"), "tutkinnonosat"),
        true,
        None
      ),
      tunnustettu = Some(tunnustettu),
      suorituskieli = None,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2016, 1, 9),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistusPaikkakunnallaJaValinnaisellaTittelillä(date(2016, 1, 9), stadinAmmattiopisto, helsinki),
      tutkinnonOsanRyhmä = ammatillisetTutkinnonOsat
    )
  )
}

object AmmatillinenOsittainenReformi {
  lazy val laaja = Oppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      AmmatillinenPerustutkintoExample.osittainenPerustutkintoOpiskeluoikeus.copy(
        suoritukset = List(AmmatillinenExampleData.ammatillisenTutkinnonOsittainenAutoalanSuoritus)
      )
    )
  )
}
