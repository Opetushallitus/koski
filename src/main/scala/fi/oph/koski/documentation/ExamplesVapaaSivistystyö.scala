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
    suoritukset = List(OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
      toimipiste = varsinaisSuomenKansanopistoToimipiste,
      tyyppi = Koodistokoodiviite("lukionoppimaara", koodistoUri = "suorituksentyyppi"), // TODO, oikea tyyppi
      koulutusmoduuli = OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(perusteenDiaarinumero = Some("70/011/2015")), // TODO: Oikea perusteen diaarinumero, kunhan sisältö on eperusteista saatavilla ja tuotu Kosken mock-dataksi
      arviointi = None,
      vahvistus = None,
      osaamiskokonaisuudet = None
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
