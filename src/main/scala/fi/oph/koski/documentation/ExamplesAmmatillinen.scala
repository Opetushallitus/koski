package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.{AmmatillisenTutkinnonOsanSuoritus, _}

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
    Example("ammatillinen - erikoisammattitutkinto", "Erikoisammattitutkinnon ja näyttötutkintoon valmistavan koulutuksen suorittanut opiskelija", AmmattitutkintoExample.erikoisammattitutkinto),
    Example("ammatillinen - tutkinnonosa", "Yhden tutkinnon osan suorittanut oppija", AmmatillinenPerustutkintoExample.osittainenPerustutkinto),
    Example("ammatillinen - tunnustettu", "Tutkinnon osa tunnustettu aiemmin suoritetusta paikallisen tutkinnon osasta", AmmatillinenPerustutkintoExample.tunnustettuPaikallinenTutkinnonOsa)
  )
}

object AmmattitutkintoExample {
  lazy val tutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357305", Some("Autoalan työnjohdon erikoisammattitutkinto"), "koulutus", None), Some("40/011/2001"))
  lazy val opiskeluoikeus = AmmatillinenOpiskeluoikeus(
    alkamispäivä = Some(date(2012, 9, 1)),
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    päättymispäivä = Some(date(2016, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, None),
      AmmatillinenOpiskeluoikeusjakso(date(2016, 5, 31), opiskeluoikeusValmistunut, None)
    )),
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(
      NäyttötutkintoonValmistavanKoulutuksenSuoritus(
        tutkinto = tutkinto,
        tila = tilaValmis,
        alkamispäivä = Some(date(2012, 9, 1)),
        päättymispäivä = None,
        toimipiste = stadinToimipiste,
        vahvistus = vahvistusPaikkakunnalla(date(2015, 5, 31), stadinAmmattiopisto, helsinki),
        osasuoritukset = Some(List(
          NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(
            tila = tilaValmis,
            koulutusmoduuli = PaikallinenNäyttötutkintoonValmistavanKoulutuksenOsa(
              PaikallinenKoodi("104052", LocalizedString.finnish("Johtaminen ja henkilöstön kehittäminen")),
              "Johtamisen ja henkilöstön kehittämisen valmistava koulutus"
            )
          ),
          NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(
            tila = tilaValmis,
            koulutusmoduuli = autonLisävarustetyöt(false)
          )
        ))
      ),
      AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = tutkinto,
        suoritustapa = Some(suoritustapaNäyttö),
        järjestämismuoto = Some(järjestämismuotoOppisopimus),
        suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
        tila = tilaValmis,
        alkamispäivä = None,
        toimipiste = stadinToimipiste,
        vahvistus = vahvistusPaikkakunnalla(date(2016, 5, 31), stadinAmmattiopisto, helsinki),
        osasuoritukset = Some(List(
          tutkinnonOsanSuoritus("104052", "Johtaminen ja henkilöstön kehittäminen", hyväksytty),
          tutkinnonOsanSuoritus("104053", "Asiakaspalvelu ja korjaamopalvelujen markkinointi", hyväksytty),
          tutkinnonOsanSuoritus("104054", "Työnsuunnittelu ja organisointi", hyväksytty),
          tutkinnonOsanSuoritus("104055", "Taloudellinen toiminta", hyväksytty),
          tutkinnonOsanSuoritus("104059", "Yrittäjyys", hyväksytty)
        ))
      )
    )
  )

  lazy val erikoisammattitutkinto = Oppija(
    exampleHenkilö,
    List(opiskeluoikeus)
  )
}

object AmmatillinenPerustutkintoExample {

  import AmmatillinenExampleData._

  val perustutkinto = oppija(opiskeluoikeus = perustutkintoOpiskeluoikeusValmis())

  val osittainenPerustutkintoOpiskeluoikeus = AmmatillinenOpiskeluoikeus(
    alkamispäivä = Some(date(2012, 9, 1)),
    arvioituPäättymispäivä = Some(date(2015, 5, 31)),
    päättymispäivä = Some(date(2016, 5, 31)),
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(AmmatillisenTutkinnonOsittainenSuoritus(
      koulutusmoduuli = AmmatillinenTutkintoKoulutus(
        Koodistokoodiviite("361902", Some("Luonto- ja ympäristöalan perustutkinto"), "koulutus", None),
        Some("62/011/2014")
      ),
      järjestämismuoto = Some(järjestämismuotoOppilaitos),
      suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = stadinToimipiste,
      osasuoritukset = Some(List(
        tutkinnonOsanSuoritus("100432", "Ympäristön hoitaminen", k3, 35)
      ))
    )),
    tila = AmmatillinenOpiskeluoikeudenTila(
      List(
        AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, None),
        AmmatillinenOpiskeluoikeusjakso(date(2016, 5, 31), opiskeluoikeusValmistunut, None)
      )
    )
  )

  lazy val osittainenPerustutkinto = Oppija(exampleHenkilö, List(osittainenPerustutkintoOpiskeluoikeus))

  lazy val tunnustettuPaikallinenTutkinnonOsaOpiskeluoikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinnonSuoritus().copy(suoritustapa = Some(suoritustapaNäyttö)),
    osat = Some(List(
      AmmatillisenTutkinnonOsanSuoritus(
        koulutusmoduuli = ValtakunnallinenTutkinnonOsa(Koodistokoodiviite("100031", Some("Moottorin ja voimansiirron huolto ja korjaus"), "tutkinnonosat", None), false, Some(LaajuusOsaamispisteissä(15))),
        tunnustettu = Some(OsaamisenTunnustaminen(
          Some(AmmatillisenTutkinnonOsanSuoritus(
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
            tila = tilaValmis,
            toimipiste = None,
            näyttö = Some(näyttö(date(2002, 4, 20), "Moottorin korjaus", "Autokorjaamo Oy, Riihimäki")),
            vahvistus = vahvistus(date(2002, 5, 28), stadinAmmattiopisto)
          )),
          "Tutkinnon osa on tunnustettu aiemmin suoritetusta autoalan perustutkinnon osasta (1.8.2000 nro 11/011/2000)"
        )),
        tila = tilaValmis,
        toimipiste = Some(stadinToimipiste),
        arviointi = Some(List(arviointiHyväksytty)),
        vahvistus = vahvistus(date(2013, 5, 31), stadinAmmattiopisto)
      )
    ))
  )

  lazy val tunnustettuPaikallinenTutkinnonOsa = Oppija(exampleHenkilö, List(tunnustettuPaikallinenTutkinnonOsaOpiskeluoikeus))
}

object AmmatillinenOldExamples {
  lazy val uusi: Oppija = oppija()

  lazy val oppisopimus = oppija(
    opiskeluoikeus = opiskeluoikeus(
      oppilaitos = stadinAmmattiopisto,
      tutkinto = AmmatillisenTutkinnonSuoritus(
        koulutusmoduuli = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
        tutkintonimike = None,
        osaamisala = None,
        suoritustapa = Some(suoritustapaNäyttö),
        järjestämismuoto = Some(järjestämismuotoOppisopimus),
        suorituskieli = None,
        tila = tilaKesken,
        alkamispäivä = Some(date(2016, 9, 1)),
        toimipiste = stadinToimipiste,
        vahvistus = None,
        osasuoritukset = None
      )
    ).copy(tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2016, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
    )))
  )

  lazy val paikallinen = oppija(opiskeluoikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinnonSuoritus().copy(suoritustapa = Some(suoritustapaNäyttö)),
    osat = Some(List(paikallisenOsanSuoritus))
  ))

  lazy val mukautettu = oppija(opiskeluoikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinnonSuoritus().copy(suoritustapa = Some(suoritustapaOps)),
    osat = Some(List(
      AmmatillisenTutkinnonOsanSuoritus(
        koulutusmoduuli = ValtakunnallinenTutkinnonOsa(Koodistokoodiviite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(LaajuusOsaamispisteissä(11))),
        lisätiedot = Some(List(AmmatillisenTutkinnonOsanLisätieto(
          Koodistokoodiviite("mukautettu", "ammatillisentutkinnonosanlisatieto"),
          "Tutkinnon osan ammattitaitovaatimuksia ja osaamisen arviointi on mukautettu (ja/tai niistä on poikettu) ammatillisesta peruskoulutuksesta annetun lain\n(630/1998, muutos 246/2015) 19 a (ja/tai 21) §:n perusteella"))),
        suorituskieli = None,
        tila = tilaValmis,
        alkamispäivä = None,
        toimipiste = Some(stadinToimipiste),
        arviointi = Some(List(arviointiKiitettävä.copy(kuvaus=Some("Erinomaista kehitystä")))),
        vahvistus = vahvistus(date(2014, 11, 8), stadinAmmattiopisto)
      )
    ))
  ))

  lazy val tutkinnonOsaToisestaTutkinnosta = oppija(opiskeluoikeus = opiskeluoikeus(
    tutkinto = AmmatillisenTutkinnonSuoritus(
      koulutusmoduuli = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
      suoritustapa = Some(suoritustapaNäyttö),
      järjestämismuoto = None,
      tila = tilaKesken,
      toimipiste = stadinToimipiste
    ),

    osat = Some(List(
      AmmatillisenTutkinnonOsanSuoritus(
        koulutusmoduuli = ValtakunnallinenTutkinnonOsa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None),
        tutkinto = Some(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357305", "koulutus"), Some("40/011/2001"))),
        suorituskieli = None,
        tila = tilaValmis,
        alkamispäivä = None,
        toimipiste = Some(stadinToimipiste),
        arviointi = Some(List(arviointiKiitettävä)),
        vahvistus = vahvistus(date(2014, 11, 8), stadinAmmattiopisto)
      )
    ))
  ))

  lazy val ops = Oppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      AmmatillinenOpiskeluoikeus(
        alkamispäivä = Some(date(2012, 9, 1)),
        arvioituPäättymispäivä = Some(date(2015, 5, 31)),
        päättymispäivä = Some(date(2016, 1, 9)),
        oppilaitos = Some(stadinAmmattiopisto),
        suoritukset = List(AmmatillisenTutkinnonSuoritus(
          koulutusmoduuli = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
          tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
          suoritustapa = Some(suoritustapaOps),
          järjestämismuoto = Some(järjestämismuotoOppilaitos),
          tila = tilaKesken,
          toimipiste = stadinToimipiste,
          suorituskieli = suomenKieli,

          osasuoritukset = Some(List(
            AmmatillisenTutkinnonOsanSuoritus(
              koulutusmoduuli = ValtakunnallinenTutkinnonOsa(Koodistokoodiviite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(LaajuusOsaamispisteissä(11))),
              tunnustettu = None,
              näyttö = None,
              lisätiedot = None,
              tutkinto = None,
              suorituskieli = None,
              tila = tilaValmis,
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
              vahvistus = vahvistus(date(2014, 11, 8), stadinAmmattiopisto)
            )
          ))
        )),
        lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
          hojks = Some(Hojks(
            opetusryhmä = Koodistokoodiviite("1", Some("Yleinen opetusryhmä"), "opetusryhma")
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
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      AmmatillinenOpiskeluoikeus(
        alkamispäivä = Some(date(2012, 9, 1)),
        arvioituPäättymispäivä = Some(date(2015, 5, 31)),
        päättymispäivä = Some(date(2016, 1, 9)),
        oppilaitos = Some(stadinAmmattiopisto),
        suoritukset = List(AmmatillisenTutkinnonSuoritus(
          koulutusmoduuli = AmmatillinenTutkintoKoulutus(
            Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", None),
            Some("39/011/2014")
          ),
          tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
          suoritustapa = Some(suoritustapaNäyttö),
          järjestämismuoto = Some(järjestämismuotoOppilaitos),
          suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
          tila = tilaValmis,
          alkamispäivä = None,
          toimipiste = stadinToimipiste,
          vahvistus = Some(HenkilövahvistusPaikkakunnalla(date(2016, 1, 9), helsinki, stadinAmmattiopisto, List(
            Organisaatiohenkilö("Mauri Bauer", "puheenjohtaja", tutkintotoimikunta),
            Organisaatiohenkilö("Reijo Reksi", "rehtori", stadinAmmattiopisto)))),
          osasuoritukset = Some(tutkinnonOsat)
        )),
        tila = AmmatillinenOpiskeluoikeudenTila(
          List(
            AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), opiskeluoikeusLäsnä, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
            AmmatillinenOpiskeluoikeusjakso(date(2016, 1, 9), opiskeluoikeusValmistunut, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
          )
        )
      )
    )
  )

  private lazy val tutkinnonOsat = List(
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = ValtakunnallinenTutkinnonOsa(
        Koodistokoodiviite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat", Some(1)),
        true,
        laajuus = None
      ),
      näyttö = Some(näyttö(date(2012, 10, 20), "Huolto- ja korjaustyöt", "Autokorjaamo Oy, Riihimäki", Some(näytönArviointi))),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2012, 10, 20),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2013, 1, 31), stadinAmmattiopisto)
    ),
    paikallisenOsanSuoritus,
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = ValtakunnallinenTutkinnonOsa(
        Koodistokoodiviite("100019", Some("Mittaus- ja korivauriotyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö(date(2013, 4, 1), "Mittaus- ja korivauriotöitä", "Autokorjaamo Oy, Riihimäki")),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2013, 4, 1),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2013, 5, 31), stadinAmmattiopisto)
    ),
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = ValtakunnallinenTutkinnonOsa(
        Koodistokoodiviite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö(date(2014, 10, 20), "Maalauksen esikäsittelytöitä", "Autokorjaamo Oy, Riihimäki")),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2014, 10, 20),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2014, 11, 8), stadinAmmattiopisto)
    ),
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = autonLisävarustetyöt(true),
      näyttö = Some(näyttö(date(2015, 4, 1), "Auton lisävarustetöitä", "Autokorjaamo Oy, Riihimäki")),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2015, 4, 1),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2015, 5, 1), stadinAmmattiopisto)
    ),
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = ValtakunnallinenTutkinnonOsa(
        Koodistokoodiviite("101050", Some("Yritystoiminnan suunnittelu"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      tunnustettu = Some(tunnustettu),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(stadinToimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2016, 2, 1),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistusPaikkakunnalla(date(2016, 5, 1), stadinAmmattiopisto, helsinki)
    )
  )
}