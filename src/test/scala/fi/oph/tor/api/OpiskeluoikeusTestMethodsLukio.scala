package fi.oph.tor.api

import java.time.LocalDate.{of => date}

import fi.oph.tor.documentation.ExampleData.jyväskylä
import fi.oph.tor.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.tor.schema._
import fi.oph.tor.localization.LocalizedStringImplicits._

trait OpiskeluoikeusTestMethodsLukio extends OpiskeluOikeusTestMethods[LukionOpiskeluoikeus]{
  val vahvistus: Some[Vahvistus] = Some(Vahvistus(päivä = date(2016, 6, 4), jyväskylä, myöntäjäOrganisaatio = jyväskylänNormaalikoulu, myöntäjäHenkilöt = List(OrganisaatioHenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

  override def defaultOpiskeluoikeus = LukionOpiskeluoikeus(
    id = None, versionumero = None, lähdejärjestelmänId = None, alkamispäivä = None, arvioituPäättymispäivä = None, päättymispäivä = None,
    oppilaitos = jyväskylänNormaalikoulu, koulutustoimija = None,
    suoritukset = List(päättötodistusSuoritus),
    opiskeluoikeudenTila = None, läsnäolotiedot = None
  )

  val päättötodistusSuoritus = LukionOppimääränSuoritus(
    koulutusmoduuli = Ylioppilastutkinto(perusteenDiaarinumero = Some("60/011/2015")),
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistus,
    osasuoritukset = None
  )
}
