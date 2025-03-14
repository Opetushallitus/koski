package fi.oph.koski.documentation

import fi.oph.koski.documentation.ExampleData.opiskeluoikeusLäsnä
import fi.oph.koski.documentation.VapaaSivistystyöExample.opiskeluoikeusHyväksytystiSuoritettu
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOppijat}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

import java.time.LocalDate

object ExamplesVapaaSivistystyöJotpa {
  lazy val varsinaisSuomenKansanopisto: Oppilaitos = Oppilaitos(
    MockOrganisaatiot.varsinaisSuomenKansanopisto,
    Some(Koodistokoodiviite("01694", None, "oppilaitosnumero", None)),
    Some("Varsinais-Suomen kansanopisto")
  )

  lazy val varsinaisSuomenKansanopistoToimipiste: OidOrganisaatio =
    OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste)

  lazy val suomenKieli: Koodistokoodiviite =
    Koodistokoodiviite("FI", "kieli")

  lazy val rahoitusJotpa: Koodistokoodiviite =
    Koodistokoodiviite("14", "opintojenrahoitus")

  object Opiskeluoikeus {
    lazy val keskeneräinen: VapaanSivistystyönOpiskeluoikeus =
      VapaanSivistystyönOpiskeluoikeus(
        oppilaitos = Some(varsinaisSuomenKansanopisto),
        tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
          VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(LocalDate.of(2023, 1, 1), opiskeluoikeusLäsnä, Some(rahoitusJotpa))
        )),
        suoritukset = List(PäätasonSuoritus.juuriAloittanut),
        lisätiedot = Some(VapaanSivistystyönOpiskeluoikeudenLisätiedot(
          jotpaAsianumero = Some(Koodistokoodiviite("01/5848/2023", "jotpaasianumero"))
        ))
      )

    lazy val suoritettu: VapaanSivistystyönOpiskeluoikeus =
      VapaanSivistystyönOpiskeluoikeus(
        oppilaitos = Some(varsinaisSuomenKansanopisto),
        tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
          VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(LocalDate.of(2023, 1, 1), opiskeluoikeusLäsnä, Some(rahoitusJotpa)),
          VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(LocalDate.of(2023, 2, 1), opiskeluoikeusHyväksytystiSuoritettu, Some(rahoitusJotpa)),
        )),
        suoritukset = List(PäätasonSuoritus.suoritettu),
        lisätiedot = Some(VapaanSivistystyönOpiskeluoikeudenLisätiedot(jotpaAsianumero = Some(Koodistokoodiviite("01/5848/2023", "jotpaasianumero"))))
      )

    lazy val keskeneräinenLähdejärjestelmästä: VapaanSivistystyönOpiskeluoikeus =
      keskeneräinen.copy(
        lähdejärjestelmänId = Some(LähdejärjestelmäId(Some("12385493"), Koodistokoodiviite("primus", "lahdejarjestelma"))),
        tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
          VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso(LocalDate.of(2023, 1, 2), opiskeluoikeusLäsnä, Some(rahoitusJotpa))
        )),
      )
  }

  object PäätasonSuoritus {
    lazy val juuriAloittanut: VapaanSivistystyönJotpaKoulutuksenSuoritus =
      VapaanSivistystyönJotpaKoulutuksenSuoritus(
        toimipiste = varsinaisSuomenKansanopistoToimipiste,
        suorituskieli = suomenKieli,
        koulutusmoduuli = VapaanSivistystyönJotpaKoulutus(
          opintokokonaisuus = VapaaSivistystyöExample.exampleOpintokokonaisuus,
        ),
        osasuoritukset = Some(Osasuoritus.osasuoritukset),
      )

    lazy val suoritettu: VapaanSivistystyönJotpaKoulutuksenSuoritus =
      VapaanSivistystyönJotpaKoulutuksenSuoritus(
        toimipiste = varsinaisSuomenKansanopistoToimipiste,
        suorituskieli = suomenKieli,
        koulutusmoduuli = VapaanSivistystyönJotpaKoulutus(
          opintokokonaisuus = VapaaSivistystyöExample.exampleOpintokokonaisuus,
          laajuus = Some(LaajuusOpintopisteissä(3))
        ),
        osasuoritukset = Some(Osasuoritus.arvioidutOsasuoritukset),
        vahvistus = Some(HenkilövahvistusValinnaisellaPaikkakunnalla(
          päivä = LocalDate.of(2023, 2, 1),
          myöntäjäOrganisaatio = varsinaisSuomenKansanopisto,
          myöntäjäHenkilöt = List(
            Organisaatiohenkilö("Reijo Reksi", finnish("Rehtori"), varsinaisSuomenKansanopisto),
          )
        ))
      )
  }

  object Osasuoritus {
    lazy val osasuoritukset = List(osasuoritus1, osasuoritus2, osasuoritus3Arvioimaton)
    lazy val arvioidutOsasuoritukset = List(osasuoritus1, osasuoritus2, osasuoritus3Arvioitu)

    lazy val osasuoritus1: VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.kurssi1,
      arviointi = Some(List(
        VapaanSivistystyöJotpaKoulutuksenArviointi(
          arvosana = Koodistokoodiviite("9", "arviointiasteikkovstjotpa"),
          päivä = LocalDate.of(2023, 2, 1)
        ),
        VapaanSivistystyöJotpaKoulutuksenArviointi(
          arvosana = Koodistokoodiviite("Hylätty", "arviointiasteikkovst"),
          päivä = LocalDate.of(2023, 1, 1)
        ),
      ))
    )

    lazy val osasuoritus2: VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.kurssi2,
      arviointi = Some(List(VapaanSivistystyöJotpaKoulutuksenArviointi(päivä = LocalDate.of(2023, 3, 1))))
    )

    lazy val osasuoritus3Arvioimaton: VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.kurssi3,
    )

    lazy val osasuoritus3Arvioitu: VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus = VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(
      koulutusmoduuli = Koulutusmoduuli.kurssi3,
      arviointi = Some(List(VapaanSivistystyöJotpaKoulutuksenArviointi(päivä = LocalDate.of(2023, 3, 1))))
    )

    object Koulutusmoduuli {
      lazy val kurssi1: VapaanSivistystyönJotpaKoulutuksenOsasuoritus = VapaanSivistystyönJotpaKoulutuksenOsasuoritus(
        tunniste = PaikallinenKoodi(nimi = finnish("Kuvantekemisen perusvälineistö"), koodiarvo = "1138-1"),
        laajuus = Some(LaajuusOpintopisteissä(1)),
      )
      lazy val kurssi2: VapaanSivistystyönJotpaKoulutuksenOsasuoritus = VapaanSivistystyönJotpaKoulutuksenOsasuoritus(
        tunniste = PaikallinenKoodi(nimi = finnish("Kuvallisen viestinnän perusteet"), koodiarvo = "1138-2"),
        laajuus = Some(LaajuusOpintopisteissä(1)),
      )
      lazy val kurssi3: VapaanSivistystyönJotpaKoulutuksenOsasuoritus = VapaanSivistystyönJotpaKoulutuksenOsasuoritus(
        tunniste = PaikallinenKoodi(nimi = finnish("Tussitekniikat I ja II"), koodiarvo = "1138-3"),
        laajuus = Some(LaajuusOpintopisteissä(1)),
      )
      lazy val kurssi3EiLaajuutta: VapaanSivistystyönJotpaKoulutuksenOsasuoritus = VapaanSivistystyönJotpaKoulutuksenOsasuoritus(
        tunniste = PaikallinenKoodi(nimi = finnish("Tussitekniikat I ja II"), koodiarvo = "1138-3"),
        laajuus = None
      )
    }
  }

  object Examples {
    val vstJotpaHenkilöKesken: UusiHenkilö = MockOppijat.asUusiOppija(KoskiSpecificMockOppijat.vstJotpaKeskenOppija)
    val vstJotpaKesken: Oppija = Oppija(
      henkilö = vstJotpaHenkilöKesken,
      opiskeluoikeudet = List(Opiskeluoikeus.keskeneräinen),
    )

    lazy val examples = List(
      Example("Jatkuvaan oppimiseen suunnattu vapaan sivistystyön koulutus", "Oppijan opinnot ovat kesken", vstJotpaKesken)
    )
  }
}
