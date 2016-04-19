package fi.oph.tor.api

import java.time.LocalDate.{of => date}

import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._

trait OpiskeluoikeusTestMethodsLukio extends OpiskeluOikeusTestMethods[LukionOpiskeluoikeus]{
  val jyväskylänNormaalikoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu, Some(Koodistokoodiviite("00204", None, "oppilaitosnumero", None)), Some("Jyväskylän normaalikoulu"))
  val vahvistus: Some[Vahvistus] = Some(Vahvistus(päivä = date(2016, 6, 4), myöntäjäOrganisaatio = jyväskylänNormaalikoulu, myöntäjäHenkilöt = List(OrganisaatioHenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))

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
