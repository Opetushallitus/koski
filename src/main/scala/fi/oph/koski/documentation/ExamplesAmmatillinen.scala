package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object ExamplesAmmatillinen {

  lazy val examples = List(
    Example("ammatillinen - uusi", "Uusi oppija lisätään suorittamaan Autoalan perustutkintoa", AmmatillinenOldExamples.uusi),
    Example("ammatillinen - oppisopimus", "Uusi oppija, suorittaa oppisopimuksella", AmmatillinenOldExamples.oppisopimus),
    Example("ammatillinen - paikallinen", "Oppija on suorittanut paikallisen tutkinnon osan", AmmatillinenOldExamples.paikallinen),
    Example("ammatillinen - mukautettu", "Tutkinnon osan arviointia on mukautettu", AmmatillinenOldExamples.mukautettu),
    Example("ammatillinen - osatoisestatutkinnosta", "Oppija on suorittanut toiseen tutkintoon liittyvän tutkinnon osan", AmmatillinenOldExamples.tutkinnonOsaToisestaTutkinnosta),
    Example("ammatillinen - full", "Isompi esimerkki. Suorittaa perustutkintoa näyttönä. Tähän lisätty lähes kaikki kaavaillut tietokentät.", AmmatillinenOldExamples.full),
    Example("ammatillinen - ops", "Perustutkinto ops:n mukaan, läsnäolotiedoilla, hojks", AmmatillinenOldExamples.ops),
    Example("ammatillinen - perustutkinto", "Ympäristönhoitajaksi valmistunut opiskelija", AmmatillinenPerustutkintoExample.todistus),
    Example("ammatillinen - erikoisammattitutkinto", "Erikoisammattitutkinnon ja näyttötutkintoon valmistavan koulutuksen suorittanut opiskelija", AmmattitutkintoExample.erikoisammattitutkinto)
  )
}

object AmmattitutkintoExample {
  lazy val tutkinto: AmmatillinenTutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357305", Some("Autoalan työnjohdon erikoisammattitutkinto"), "koulutus", None), Some("40/011/2001"))
  lazy val opiskeluoikeus = AmmatillinenOpiskeluoikeus(
        alkamispäivä = Some(date(2012, 9, 1)),
        arvioituPäättymispäivä = Some(date(2015, 5, 31)),
        päättymispäivä = Some(date(2016, 5, 31)),
        oppilaitos = stadinAmmattiopisto,
        suoritukset = List(
          NäyttötutkintoonValmistavanKoulutuksenSuoritus(
            koulutusmoduuli = tutkinto,
            tila = tilaValmis,
            alkamispäivä = Some(date(2012, 9, 1)),
            loppumispäivä = None,
            toimipiste = toimipiste,
            vahvistus = vahvistus(date(2015, 5, 31), stadinAmmattiopisto, helsinki),
            osasuoritukset = Some(List(
              NäyttötutkintoonValmistavanKoulutuksenOsanSuoritus(
                tila = tilaValmis, koulutusmoduuli = NäyttötutkintoonValmistavanKoulutuksenOsa(PaikallinenKoodi("104052", LocalizedString.finnish("Johtaminen ja henkilöstön kehittäminen")))
              )
            ))
          ),
          tutkintoSuoritus(
            tutkintoKoulutus = tutkinto,
            suoritustapa = Some(suoritustapaNäyttö),
            järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos)),
            suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
            tila = tilaValmis,
            alkamisPäivä = None,
            toimipiste = toimipiste,
            vahvistus = vahvistus(date(2016, 5, 31), stadinAmmattiopisto, helsinki),
            osasuoritukset = Some(List(
              tutkinnonOsanSuoritus("104052", "Johtaminen ja henkilöstön kehittäminen", hyväksytty),
              tutkinnonOsanSuoritus("104053", "Asiakaspalvelu ja korjaamopalvelujen markkinointi", hyväksytty),
              tutkinnonOsanSuoritus("104054", "Työnsuunnittelu ja organisointi", hyväksytty),
              tutkinnonOsanSuoritus("104055", "Taloudellinen toiminta", hyväksytty),
              tutkinnonOsanSuoritus("104059", "Yrittäjyys", hyväksytty)
            ))
          )
        ),
        tavoite = tavoiteTutkinto
      )

  lazy val erikoisammattitutkinto = Oppija(
    exampleHenkilö,
    List(opiskeluoikeus)
  )
}

object AmmatillinenPerustutkintoExample {
  import AmmatillinenExampleData._

  lazy val todistus = Oppija(
    exampleHenkilö,
    List(
      AmmatillinenOpiskeluoikeus(
        alkamispäivä = Some(date(2012, 9, 1)),
        arvioituPäättymispäivä = Some(date(2015, 5, 31)),
        päättymispäivä = Some(date(2016, 5, 31)),
        oppilaitos = stadinAmmattiopisto,
        suoritukset = List(tutkintoSuoritus(
          tutkintoKoulutus = AmmatillinenTutkintoKoulutus(
            Koodistokoodiviite("361902", Some("Luonto- ja ympäristöalan perustutkinto"), "koulutus", None),
            Some("62/011/2014")
          ),
          tutkintonimike = Some(List(Koodistokoodiviite("10083", Some("Ympäristönhoitaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(Koodistokoodiviite("1590", Some("Ympäristöalan osaamisala"), "osaamisala", None))),
          suoritustapa = Some(suoritustapaOps),
          järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos)),
          suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
          tila = tilaValmis,
          alkamisPäivä = None,
          toimipiste = toimipiste,
          vahvistus = vahvistus(date(2016, 5, 31), stadinAmmattiopisto, helsinki),
          osasuoritukset = Some(List(
            tutkinnonOsanSuoritus("100431", "Kestävällä tavalla toimiminen", k3, 40).copy(työssäoppimisjaksot = Some(List(
              Työssäoppimisjakso(date(2014, 1, 1), Some(date(2014, 3, 15)), jyväskylä, suomi, LocalizedString.finnish("Toimi harjoittelijana Sortti-asemalla"), LaajuusOsaamispisteissä(5))
            ))),
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
        tavoite = tavoiteTutkinto,
        tila = Some(AmmatillinenOpiskeluoikeudenTila(
          List(
            AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 5, 31)), ammatillinenOpiskeluoikeusAktiivinen, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
            AmmatillinenOpiskeluoikeusjakso(date(2016, 6, 1), None, ammatillinenOpiskeluoikeusPäättynyt, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
          )
        ))
      )
    )
  )

}

object AmmatillinenOldExamples {
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
        suorituskieli = None,
        tila = tilaKesken,
        alkamisPäivä = Some(date(2016, 9, 1)),
        toimipiste = toimipiste,
        vahvistus = None,
        osasuoritukset = None
      )
    ).copy(tila = Some(AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2016, 9, 1), None, ammatillinenOpiskeluoikeusAktiivinen, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
    ))))
  )

  lazy val paikallinen = oppija(opiskeluOikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinto.copy(suoritustapa = Some(suoritustapaNäyttö)),
    osat = Some(List(paikallisenOsanSuoritus))
  ))

  lazy val mukautettu = oppija(opiskeluOikeus = opiskeluoikeus(
    tutkinto = autoalanPerustutkinto.copy(suoritustapa = Some(suoritustapaOps)),
    osat = Some(List(
      AmmatillisenTutkinnonOsanSuoritus(
        koulutusmoduuli = OpsTutkinnonosa(Koodistokoodiviite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(LaajuusOsaamispisteissä(11))),
        lisätiedot = Some(List(AmmatillisenTutkinnonOsanLisätieto(
          Koodistokoodiviite("mukautettu", "ammatillisentutkinnonosanlisatieto"),
          "Tutkinnon osan ammattitaitovaatimuksia ja osaamisen arviointi on mukautettu (ja/tai niistä on poikettu) ammatillisesta peruskoulutuksesta annetun lain\n(630/1998, muutos 246/2015) 19 a (ja/tai 21) §:n perusteella"))),
        suorituskieli = None,
        tila = tilaValmis,
        alkamispäivä = None,
        toimipiste = Some(toimipiste),
        arviointi = arviointiKiitettävä,
        vahvistus = vahvistus(date(2014, 11, 8), stadinAmmattiopisto, helsinki)
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
      AmmatillisenTutkinnonOsanSuoritus(
        koulutusmoduuli = OpsTutkinnonosa(Koodistokoodiviite("104052", "tutkinnonosat"), true, None, None, None),
        tutkinto = Some(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("357305", "koulutus"), Some("40/011/2001"))),
        suorituskieli = None,
        tila = tilaValmis,
        alkamispäivä = None,
        toimipiste = Some(toimipiste),
        arviointi = arviointiKiitettävä,
        vahvistus = vahvistus(date(2014, 11, 8), stadinAmmattiopisto, helsinki)
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
        oppilaitos = stadinAmmattiopisto,
        suoritukset = List(tutkintoSuoritus(
          tutkintoKoulutus = AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", None), Some("39/011/2014")),
          tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
          suoritustapa = Some(suoritustapaOps),
          järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos)),
          tila = tilaKesken,
          toimipiste = toimipiste,
          suorituskieli = suomenKieli,

          osasuoritukset = Some(List(
            AmmatillisenTutkinnonOsanSuoritus(
              koulutusmoduuli = OpsTutkinnonosa(Koodistokoodiviite("101053", Some("Viestintä- ja vuorovaikutusosaaminen"), "tutkinnonosat", None), true, Some(LaajuusOsaamispisteissä(11))),
              tunnustettu = None,
              näyttö = None,
              lisätiedot = None,
              tutkinto = None,
              suorituskieli = None,
              tila = tilaValmis,
              alkamispäivä = None,
              toimipiste = Some(toimipiste),
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
              vahvistus = vahvistus(date(2014, 11, 8), stadinAmmattiopisto, helsinki)
            )
          ))
        )),
        lisätiedot = Some(AmmatillisenOpiskeluoikeudenLisätiedot(
          hojks = Some(Hojks(
            opetusryhmä = Koodistokoodiviite("1", Some("Yleinen opetusryhmä"), "opetusryhma"),
            peruste = Koodistokoodiviite("8", Some("Kuulovamma"), "ammatillisenerityisopetuksenperuste")
          ))
        )),
        tavoite = tavoiteTutkinto,
        tila = Some(AmmatillinenOpiskeluoikeudenTila(
          List(
            AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), Some(date(2012, 12, 31)), ammatillinenOpiskeluoikeusAktiivinen, Some(Koodistokoodiviite("1", Some("Valtionosuusrahoitteinen koulutus"), "opintojenrahoitus", None))),
            AmmatillinenOpiskeluoikeusjakso(date(2013, 1, 1), Some(date(2013, 12, 31)), ammatillinenOpiskeluoikeusKeskeyttänyt, Some(Koodistokoodiviite("1", Some("Valtionosuusrahoitteinen koulutus"), "opintojenrahoitus", None))),
            AmmatillinenOpiskeluoikeusjakso(date(2014, 1, 1), None, ammatillinenOpiskeluoikeusAktiivinen, Some(Koodistokoodiviite("1", Some("Valtionosuusrahoitteinen koulutus"), "opintojenrahoitus", None)))
          )
        )),
        läsnäolotiedot = Some(YleisetLäsnäolotiedot(List(
          YleinenLäsnäolojakso(date(2012, 9, 1), Some(date(2012, 12, 31)), Koodistokoodiviite("lasna", Some("Läsnä"), "lasnaolotila", Some(1))),
          YleinenLäsnäolojakso(date(2013, 1, 1), Some(date(2013, 12, 31)), Koodistokoodiviite("poissa", Some("Poissa"), "lasnaolotila", Some(1))),
          YleinenLäsnäolojakso(date(2014, 1, 1), None, Koodistokoodiviite("lasna", Some("Läsnä"), "lasnaolotila", Some(1)))
        )))
      )))

  lazy val full = Oppija(
    Henkilö.withOid("1.2.246.562.24.00000000001"),
    List(
      AmmatillinenOpiskeluoikeus(
        lähdejärjestelmänId = Some(LähdejärjestelmäId("847823465", lähdeWinnova)),
        alkamispäivä = Some(date(2012, 9, 1)),
        arvioituPäättymispäivä = Some(date(2015, 5, 31)),
        päättymispäivä = Some(date(2016, 1, 9)),
        oppilaitos = stadinAmmattiopisto,
        suoritukset = List(tutkintoSuoritus(
          tutkintoKoulutus = AmmatillinenTutkintoKoulutus(
            Koodistokoodiviite("351301", Some("Autoalan perustutkinto"), "koulutus", None),
            Some("39/011/2014")
          ),
          tutkintonimike = Some(List(Koodistokoodiviite("10024", Some("Autokorinkorjaaja"), "tutkintonimikkeet", None))),
          osaamisala = Some(List(Koodistokoodiviite("1525", Some("Autokorinkorjauksen osaamisala"), "osaamisala", None))),
          suoritustapa = Some(suoritustapaNäyttö),
          järjestämismuoto = Some(DefaultJärjestämismuoto(järjestämismuotoOppilaitos)),
          suorituskieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None)),
          tila = tilaValmis,
          alkamisPäivä = None,
          toimipiste = toimipiste,
          vahvistus = Some(Henkilövahvistus(date(2016, 1, 9), helsinki, stadinAmmattiopisto, List(
            OrganisaatioHenkilö("Mauri Bauer", "puheenjohtaja", tutkintotoimikunta),
            OrganisaatioHenkilö("Reijo Reksi", "rehtori", stadinAmmattiopisto)))),
          osasuoritukset = Some(tutkinnonOsat)
        )),
        tavoite = tavoiteTutkinto,
        tila = Some(AmmatillinenOpiskeluoikeudenTila(
          List(
            AmmatillinenOpiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), ammatillinenOpiskeluoikeusAktiivinen, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
            AmmatillinenOpiskeluoikeusjakso(date(2016, 1, 10), None, ammatillinenOpiskeluoikeusPäättynyt, Some(Koodistokoodiviite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
          )
        )),
        läsnäolotiedot = None
      )
    )
  )

  private lazy val tutkinnonOsat = List(
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        Koodistokoodiviite("100016", Some("Huolto- ja korjaustyöt"), "tutkinnonosat", Some(1)),
        true,
        laajuus = None
      ),
      näyttö = Some(näyttö("Huolto- ja korjaustyöt", "Autokorjaamo Oy, Riihimäki", Some(näytönArviointi))),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(toimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2012, 10, 20),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2013, 1, 31), stadinAmmattiopisto, helsinki)
    ),
    paikallisenOsanSuoritus,
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli =  OpsTutkinnonosa(
        Koodistokoodiviite("100019", Some("Mittaus- ja korivauriotyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö("Mittaus- ja korivauriotöitä", "Autokorjaamo Oy, Riihimäki")),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(toimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2013, 4, 1),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2013, 5, 31), stadinAmmattiopisto, helsinki)
    ),
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        Koodistokoodiviite("100034", Some("Maalauksen esikäsittelytyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö("Maalauksen esikäsittelytöitä", "Autokorjaamo Oy, Riihimäki")),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(toimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2014, 10, 20),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2014, 11, 8), stadinAmmattiopisto, helsinki)
    ),
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        Koodistokoodiviite("100037", Some("Auton lisävarustetyöt"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      näyttö = Some(näyttö("Auton lisävarustetöitä", "Autokorjaamo Oy, Riihimäki")),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(toimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2015, 4, 1),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2015, 5, 1), stadinAmmattiopisto, helsinki)
    ),
    AmmatillisenTutkinnonOsanSuoritus(
      koulutusmoduuli = OpsTutkinnonosa(
        Koodistokoodiviite("101050", Some("Yritystoiminnan suunnittelu"), "tutkinnonosat", Some(1)),
        true,
        None
      ),
      tunnustettu = Some(tunnustettu),
      suorituskieli = None,
      tila = tilaValmis,
      alkamispäivä = None,
      toimipiste = Some(toimipiste),
      arviointi = Some(List(AmmatillinenArviointi(
        arvosana = Koodistokoodiviite("Hyväksytty", Some("Hyväksytty"), "arviointiasteikkoammatillinenhyvaksyttyhylatty", Some(1)),
        date(2016, 2, 1),
        arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen")))
      ))),
      vahvistus = vahvistus(date(2016, 5, 1), stadinAmmattiopisto, helsinki)
    )
  )
}