package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

object ExamplesVapaaSivistystyö {
  lazy val examples = List(
    Example("vapaa sivistystyö - oppivelvollisille suunnattu koulutus", "Oppija suorittaa oppivelvollisille suunnattua koulutusta kansanopistossa", VapaaSivistystyöExample.oppivelvollisuuskoulutusExample)
  )
}

object VapaaSivistystyöExample {

  lazy val opiskeluoikeus = VapaanSivistystyönOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
      VapaanSivistystyönOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä)
    )),
    lisätiedot = None,
    oppilaitos = Some(varsinaisSuomenKansanopisto),
    suoritukset = List(OppivelvollisilleSuunnatunVapaanSivistystyönKoulutuksenSuoritus(
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      tyyppi = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"), // TODO, oikea tyyppi
      koulutusmoduuli = OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(perusteenDiaarinumero = Some("OPH-1234-2020")), // TODO: Oikea perusteen diaarinumero, kunhan oikea diaarinumero saatavilla ja sisältö tuotu Kosken mockdataan
      vahvistus = None,
      osasuoritukset = Some(List(
        OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
          tyyppi = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"), // TODO, oikea tyyppi
          koulutusmoduuli = OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus(
            tunniste = Koodistokoodiviite("1002", "opintokokonaisuusnimet"),
            kuvaus = "TODO"
          ),
          osasuoritukset = Some(List(
            OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
              tyyppi = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"), // TODO, oikea tyyppi
              koulutusmoduuli = OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(
                tunniste = PaikallinenKoodi(koodiarvo = "A01", nimi = "Arjen rahankäyttö"),
                kuvaus = "Arjen rahankäyttö"
              ),
              arviointi = None
            )
          ))
        )
      ))
    ))
  )

  lazy val oppivelvollisuuskoulutusExample = Oppija(
    VapaaSivistystyöExampleData.exampleHenkilö,
    List(opiskeluoikeus)
  )
}

object VapaaSivistystyöExampleData {
  val exampleHenkilö = asUusiOppija(MockOppijat.vapaaSivistystyöOppivelvollinen)

  lazy val varsinaisSuomenKansanopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.varsinaisSuomenKansanopisto, Some(Koodistokoodiviite("01694", None, "oppilaitosnumero", None)), Some("Varsinais-Suomen kansanopisto"))

  lazy val varsinaisSuomenKansanopistoToimipiste: Toimipiste = Toimipiste(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste)

}
