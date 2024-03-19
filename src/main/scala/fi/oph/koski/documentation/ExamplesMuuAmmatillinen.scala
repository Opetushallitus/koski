package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

object ExamplesMuuAmmatillinen {
  lazy val examples = List(
    Example("ammatillinen - tutkinnon osaa pienempi kokonaisuus", "Oppija suorittaa kiinteistösihteerin tutkinnon osaa pienempää muuta ammatillista koulutusta", TutkinnonOsaaPienempiKokonaisuusExample.example),
    Example("ammatillinen - ammatilliseen tehtävään valmistava muu ammatillinen koulutus", "Oppija suorittaa ansio ja liikennelentäjän tehtävään valmistavaa muuta ammatillista koulutusta", MuunAmmatillisenKoulutuksenExample.ammatilliseenTehtäväänValmistavaKoulutusExample),
    Example("ammatillinen - muu ammatillinen koulutus", "Oppija suorittaa kiinteistösihteerin muuta ammatillista koulutusta", MuunAmmatillisenKoulutuksenExample.muuAmmatillinenKoulutusExample),
    Example("ammatillinen - muu ammatillinen koulutus valmis", "Oppija on suorittanut rakennusterveysasiantuntijan muun ammatillisen koulutuksen", MuunAmmatillisenKoulutuksenExample.muuAmmatillinenKoulutusKokonaisuuksillaExample)
  )
}
object TutkinnonOsaaPienempiKokonaisuusExample {

  lazy val opiskeluoikeus = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2020, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2018, 1, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    lisätiedot = None,
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus)
  )

  lazy val tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(
    koulutusmoduuli = tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus,
    alkamispäivä = None,
    osaamisenHankkimistavat = None,
    koulutussopimukset = None,
    suorituskieli = suomenKieli,
    toimipiste = stadinToimipiste,
    osasuoritukset = Some(List(
      ValtakunnallisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
        TutkinnonOsaaPienempiKokonaisuus(
          PaikallinenKoodi("AKTV", "Asunto- ja kiinteistöosakeyhtiön talous ja verotus"),
          None,
          finnish("Kurssilla opitaan hallitsemaan asunto- ja kiinteistöosakeyhtiön taloutta ja verotusta.")
        ),
        alkamispäivä = None,
        arviointi = Some(List(MuunAmmatillisenKoulutuksenExample.arviointiHyväksytty)),
        näyttö = None,
        liittyyTutkinnonOsaan = Koodistokoodiviite(
          "101481",
          koodistoUri = "tutkinnonosat"
        ),
        lisätiedot = None,
        suorituskieli = None
      ),
      ValtakunnallisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
        TutkinnonOsaaPienempiKokonaisuus(
          PaikallinenKoodi("ATK", "Tietokoneiden huolto"),
          Some(LaajuusKaikkiYksiköt(4, laajuusOsaamispisteissä)),
          finnish("Kurssilla opitaan korjaamaan tietokoneita.")
        ),
        alkamispäivä = None,
        arviointi = None,
        näyttö = None,
        liittyyTutkinnonOsaan = Koodistokoodiviite(
          "101481",
          koodistoUri = "tutkinnonosat"
        ),
        lisätiedot = None,
        suorituskieli = None
      ),
      MuunAmmatillisenKoulutuksenExample.yhteisenTutkinnonOsanOsaAlueenSuoritusValtakunnallinen,
      MuunAmmatillisenKoulutuksenExample.yhteisenTutkinnonOsanOsaAlueenSuoritusPaikallinen,
      MuunAmmatillisenKoulutuksenExample.yhteisenTutkinnonOsanOsaAlueenSuoritusTunnustettuKesken,
      MuunAmmatillisenKoulutuksenExample.yhteisenTutkinnonOsanOsaAlueenSuoritusRahoitettu
    ))
  )

  lazy val example = Oppija(
    exampleHenkilö,
    List(opiskeluoikeus)
  )
}

object MuunAmmatillisenKoulutuksenExample {
  lazy val muuAmmatillinenKoulutusKokonaisuuksilla: PaikallinenMuuAmmatillinenKoulutus = PaikallinenMuuAmmatillinenKoulutus(
    PaikallinenKoodi("RTA", "Rakennusterveysasiantuntija-koulutus (RTA)"),
    None,
    finnish("Koulutus antaa valmiudet rakennusterveysasiantuntijana toimimiseen")
  )

  lazy val muuAmmatillinenKoulutusOpiskeluoikeus = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2020, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2018, 1, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    lisätiedot = None,
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(muunAmmatillisenKoulutuksenSuoritus)
  )

  lazy val muuAmmatillinenKoulutusKokonaisuuksillaOpiskeluoikeus = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2018, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2016, 1, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen)),
      AmmatillinenOpiskeluoikeusjakso(date(2018, 5, 31), opiskeluoikeusValmistunut, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    lisätiedot = None,
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(
      MuunAmmatillisenKoulutuksenSuoritus(
        koulutusmoduuli = muuAmmatillinenKoulutusKokonaisuuksilla,
        None,
        osaamisenHankkimistavat = None,
        koulutussopimukset = None,
        suorituskieli = suomenKieli,
        alkamispäivä = None,
        toimipiste = stadinToimipiste,
        vahvistus = Some(HenkilövahvistusValinnaisellaPaikkakunnalla(date(2018, 5, 31), Some(helsinki), stadinAmmattiopisto, List(
          Organisaatiohenkilö("Reijo Reksi", "rehtori", stadinAmmattiopisto)))
        ),
        osasuoritukset = Some(List(
          muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
            tunniste = PaikallinenKoodi("RTT", "Rakenne- ja tuotantotekniikka"),
            kuvaus = "Rakennusfysiikka, fysikaaliset olosuhteet, kuntotutkimusmenetelmät, rakenne- ja tuotantotekniikka ja juridiikka",
            osasuoritukset = Some(List(
              muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
                tunniste = PaikallinenKoodi("RF", "Rakennusfysiikka"),
                kuvaus = "Rakennusfysiikka ja fysikaaliset olosuhteet",
                laajuus = Some(LaajuusKaikkiYksiköt(5, laajuusOpintopisteissä))
              ),
              muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
                tunniste = PaikallinenKoodi("KT", "Kuntotutkimusmenetelmät"),
                kuvaus = "Kuntotutkimuksen menetelmät",
                laajuus = Some(LaajuusKaikkiYksiköt(4, laajuusOpintopisteissä))
              ),
              muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
                tunniste = PaikallinenKoodi("RT", "Rakenne- ja tuotantotekniikka"),
                kuvaus = "Rakennetekniikka",
                laajuus = Some(LaajuusKaikkiYksiköt(2, laajuusOpintopisteissä))
              ),
              muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
                tunniste = PaikallinenKoodi("JR", "Juridiikka"),
                kuvaus = "Rakennuksiin liittyvä juridiikka",
                laajuus = Some(LaajuusKaikkiYksiköt(2, laajuusOpintopisteissä))
              )
            ))
          ),
          muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
            tunniste = PaikallinenKoodi("IIT", "Ilmanvaihto ja ilmastointitekniikka"),
            kuvaus = "Ilmanvaihdon ja ilmastoinnin tekniikka",
            osasuoritukset = Some(List(
              muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
                tunniste = PaikallinenKoodi("IITT", "Teoria"),
                kuvaus = "Ilmanvaihdon ja ilmastointitekniikan teoria",
                laajuus = Some(LaajuusKaikkiYksiköt(1.5f, laajuusOpintopisteissä))
              ),
              muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
                tunniste = PaikallinenKoodi("TM", "Tutkimusmenetelmät"),
                kuvaus = "Ilmanvaihdon ja ilmastointitekniikan tutkimusmenetelmät",
                laajuus = Some(LaajuusKaikkiYksiköt(1.5f, laajuusOpintopisteissä))
              )
            ))
          ),
          muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
            tunniste = PaikallinenKoodi("SETTT", "Sisäympäristön epäpuhtaudet"),
            kuvaus = "Sisäympäristön epäpuhtaudet, terveysvaikutukset, tutkiminen, torjunta",
            Some(List(
              muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
                tunniste = PaikallinenKoodi("KS", "Sisäympäristön epäpuhtaudet"),
                kuvaus = "Kemiallinen sisäympäristö",
                laajuus = Some(LaajuusKaikkiYksiköt(3, laajuusOpintopisteissä))
              ),
              muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
                tunniste = PaikallinenKoodi("STM", "Sisäympäristön tutkimusmenetelmät"),
                kuvaus = "Kemiallinen sisäympäristö",
                laajuus = Some(LaajuusKaikkiYksiköt(1, laajuusOpintopisteissä))
              ),
              muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
                tunniste = PaikallinenKoodi("TV", "Terveysvaikutukset"),
                kuvaus = "Vaikutukset terveydelle",
                laajuus = Some(LaajuusKaikkiYksiköt(2, laajuusOpintopisteissä))
              )
            ))
          ),
          muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
            tunniste = PaikallinenKoodi("OT", "Opinnäyte"),
            kuvaus = "Opinnäytetyö",
            laajuus = Some(LaajuusKaikkiYksiköt(15, laajuusOpintopisteissä))
          )
        ))
      )
    )
  )

  lazy val ammatilliseenTehtäväänValmistavaKoulutusOpiskeluoikeusVahvistettu =
    ammatilliseenTehtäväänValmistavaKoulutusOpiskeluoikeus.withSuoritukset(
      List(
        ammatilliseenTehtäväänValmistavaKoulutusOpiskeluoikeus.suoritukset.head.asInstanceOf[MuunAmmatillisenKoulutuksenSuoritus].copy(
          vahvistus = Some(HenkilövahvistusValinnaisellaPaikkakunnalla(
            date(2020, 5, 31),
            None,
            stadinAmmattiopisto,
            List(Organisaatiohenkilö("Reijo Reksi", "rehtori", stadinAmmattiopisto))
          ))
        )
      )
    )

  lazy val ammatilliseenTehtäväänValmistavaKoulutusOpiskeluoikeus = AmmatillinenOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2020, 5, 31)),
    tila = AmmatillinenOpiskeluoikeudenTila(List(
      AmmatillinenOpiskeluoikeusjakso(date(2018, 1, 1), opiskeluoikeusLäsnä, Some(ExampleData.valtionosuusRahoitteinen))
    )),
    lisätiedot = None,
    oppilaitos = Some(stadinAmmattiopisto),
    suoritukset = List(
      MuunAmmatillisenKoulutuksenSuoritus(
        koulutusmoduuli = ammatilliseenTehtäväänValmistavaKoulutus,
        None,
        osaamisenHankkimistavat = None,
        koulutussopimukset = None,
        suorituskieli = suomenKieli,
        alkamispäivä = None,
        toimipiste = stadinToimipiste,
        osasuoritukset = Some(List(
          muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
            tunniste = PaikallinenKoodi("LT", "Lentämisen teoria"),
            kuvaus = "Tutustutaan lentämisen perusteisiin",
            laajuus = None,
            osasuoritukset = Some(List(
              muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
                PaikallinenKoodi("LS", "Lentosuunnitelma"),
                kuvaus = "Tutustutaan lentosuunnitelman tekemiseen",
                laajuus = Some(LaajuusKaikkiYksiköt(1, laajuusOsaamispisteissä))
              )
            ))
          )
        ))
      )
    )
  )

  lazy val muunAmmatillisenKoulutuksenSuoritus = MuunAmmatillisenKoulutuksenSuoritus(
    koulutusmoduuli = muuAmmatillinenKoulutus,
    täydentääTutkintoa = Some(AmmatillinenTutkintoKoulutus(Koodistokoodiviite("351301", None, "koulutus", None), Some("39/011/2014"))),
    osaamisenHankkimistavat = None,
    koulutussopimukset = None,
    suorituskieli = suomenKieli,
    alkamispäivä = None,
    toimipiste = stadinToimipiste,
    osasuoritukset = Some(List(
      muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
        PaikallinenKoodi("AOYL", "Asunto-osakeyhtiölain ja huoneenvuokralainsäädännön perusteet"),
        "Asunto-osakeyhtiölain ja huoneenvuokralainsäädännön perusteet, huoneistokohtaiset korjaus- ja muutostyöt",
        laajuus = Some(LaajuusKaikkiYksiköt(5, laajuusOpintopisteissä))
      ),
      muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
        PaikallinenKoodi("AKOYTV", "Asunto- ja kiinteistöosakeyhtiön talous ja verotus"),
        "Laskentatoimen ja verotuksen perusteet, lainaosuus- ja rahoituslaskelmat, kiinteistövero, arvonlisävero sekä veroilmoituslomakkeet",
        laajuus = Some(LaajuusKaikkiYksiköt(15, laajuusOpintopisteissä))
      ),
      muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
        PaikallinenKoodi("TAP", "Tiedottaminen ja asiakaspalvelu"),
        "Tiedottaminen ja asiakaspalvelu, isännöitsijän ja sihteerin työparityöskentely sekä asiakaspalvelun henkilöturvallisuus",
        laajuus = Some(LaajuusKaikkiYksiköt(20, laajuusOpintopisteissä))
      ),
      muunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
        PaikallinenKoodi("KISI", "KISI-tentti"),
        "Valmiudet hoitaa kiinteistöalan yrityksen sihteeri-, toimisto- ja asiakaspalvelutehtäviä, vahvistaa osallistujan ammatillisia perusvalmiuksia"
      ),
      ValtakunnallisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
        TutkinnonOsaaPienempiKokonaisuus(
          PaikallinenKoodi("ATK", "ATK-Ajokortti"),
          None,
          finnish("Tietokoneiden käyttö")
        ),
        alkamispäivä = None,
        arviointi = None,
        näyttö = None,
        liittyyTutkinnonOsaan = Koodistokoodiviite(
          "101481",
          koodistoUri = "tutkinnonosat"
        ),
        lisätiedot = None,
        suorituskieli = None
      ),
      yhteisenTutkinnonOsanOsaAlueenSuoritusValtakunnallinen,
      yhteisenTutkinnonOsanOsaAlueenSuoritusPaikallinen,
    ))
  )

  lazy val muuAmmatillinenKoulutusExample = Oppija(
    exampleHenkilö,
    List(muuAmmatillinenKoulutusOpiskeluoikeus)
  )

  lazy val muuAmmatillinenKoulutusKokonaisuuksillaExample = Oppija(
    exampleHenkilö,
    List(muuAmmatillinenKoulutusKokonaisuuksillaOpiskeluoikeus)
  )

  lazy val ammatilliseenTehtäväänValmistavaKoulutusExample = Oppija(
    exampleHenkilö,
    List(ammatilliseenTehtäväänValmistavaKoulutusOpiskeluoikeus)
  )

  lazy val yhteisenTutkinnonOsanOsaAlueenSuoritusValtakunnallinen = YhteisenTutkinnonOsanOsaAlueenSuoritus(
    koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(
      Koodistokoodiviite("FK", "ammatillisenoppiaineet"),
      pakollinen = true,
      laajuus = None
    ),
    arviointi = Some(List(AmmatillinenExampleData.arviointiHyväksytty))
  )

  lazy val yhteisenTutkinnonOsanOsaAlueenSuoritusPaikallinen = YhteisenTutkinnonOsanOsaAlueenSuoritus(
    koulutusmoduuli = AmmatillisenTutkinnonÄidinkieli(
      Koodistokoodiviite("AI", "ammatillisenoppiaineet"),
      pakollinen = false,
      kieli = Koodistokoodiviite("AI1", "oppiaineaidinkielijakirjallisuus"),
      laajuus = None
    ),
    arviointi = Some(List(AmmatillinenExampleData.arviointiHyväksytty))
  )

  lazy val yhteisenTutkinnonOsanOsaAlueenSuoritusTunnustettuKesken = YhteisenTutkinnonOsanOsaAlueenSuoritus(
    koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(
      Koodistokoodiviite("ETK", "ammatillisenoppiaineet"),
      pakollinen = true,
      laajuus = None
    ),
    tunnustettu = Some(AmmatillinenExampleData.tunnustettu.copy(rahoituksenPiirissä = false))
  )

  lazy val yhteisenTutkinnonOsanOsaAlueenSuoritusRahoitettu = YhteisenTutkinnonOsanOsaAlueenSuoritus(
    koulutusmoduuli = ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(
      Koodistokoodiviite("PS", "ammatillisenoppiaineet"),
      pakollinen = true,
      laajuus = Some(LaajuusOsaamispisteissä(5, laajuusOsaamispisteissä))
    ),
    tunnustettu = Some(AmmatillinenExampleData.tunnustettu.copy(rahoituksenPiirissä = true)),
    arviointi = Some(List(AmmatillinenExampleData.arviointiHyväksytty))
  )

  lazy val arviointiHyväksytty = MuunAmmatillisenKoulutuksenArviointi(
    arvosana = hyväksytty, date(2013, 3, 20),
    arvioitsijat = Some(List(Arvioitsija("Jaana Arstila"), Arvioitsija("Pekka Saurmann"), Arvioitsija("Juhani Mykkänen"))))
}
