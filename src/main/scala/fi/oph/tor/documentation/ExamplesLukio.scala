package fi.oph.tor.documentation

import java.time.LocalDate.{of => date}
import fi.oph.tor.documentation.ExampleData._
import fi.oph.tor.documentation.LukioExampleData._
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._

object ExamplesLukio {
  val uusi = TorOppija(
    exampleHenkilö,
    List(LukionOpiskeluoikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      päättymispäivä = None,
      oppilaitos = jyväskylänNormaalikoulu,
      suoritukset = List(
        LukionOppimääränSuoritus(
          paikallinenId = None,
          suorituskieli = suomenKieli,
          tila = tilaKesken,
          toimipiste = jyväskylänNormaalikoulu,
          osasuoritukset = None
        )
      ),
      opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(
        List(
          Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen, None),
          Opiskeluoikeusjakso(date(2016, 1, 10), None, opiskeluoikeusPäättynyt, None)
        )
      )),
      läsnäolotiedot = None
    ))
  )

  val examples = List(
    Example("lukio - uusi", "Uusi oppija lisätään suorittamaan lukiota", uusi)
  )
}

object LukioExampleData {
  lazy val jyväskylänNormaalikoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu, Some(Koodistokoodiviite("00204", None, "oppilaitosnumero", None)), Some("Jyväskylän normaalikoulu"))
}