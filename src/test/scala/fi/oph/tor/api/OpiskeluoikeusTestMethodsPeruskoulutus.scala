package fi.oph.tor.api

import java.time.LocalDate.{of => date}

import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._

trait OpiskeluoikeusTestMethodsPeruskoulutus extends OpiskeluOikeusTestMethods[PerusopetuksenOpiskeluoikeus]{
  val vahvistus: Some[Vahvistus] = Some(Vahvistus(date(2016, 6, 4), jyväskylä, jyväskylänNormaalikoulu, List(OrganisaatioHenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

  override def defaultOpiskeluoikeus = PerusopetuksenOpiskeluoikeus(
    id = None, versionumero = None, lähdejärjestelmänId = None, alkamispäivä = None, arvioituPäättymispäivä = None, päättymispäivä = None,
    oppilaitos = jyväskylänNormaalikoulu, koulutustoimija = None,
    suoritukset = List(päättötodistusSuoritus),
    opiskeluoikeudenTila = None, läsnäolotiedot = None
  )

  val päättötodistusSuoritus = PeruskoulunPäättötodistus(
    koulutusmoduuli = Peruskoulutus(perusteenDiaarinumero = Some("104/011/2014")),
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistus
  )
}
