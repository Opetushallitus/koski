package fi.oph.tor.documentation

import java.time.LocalDate.{of => date}
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._
import ExampleData._
import PeruskoulutusExampleData._

object PeruskoulutusExampleData {
  lazy val jyväskylänNormaalikoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu, Some(KoodistoKoodiViite("00204", None, "oppilaitosnumero", None)), Some("Jyväskylän normaalikoulu"))
}

object ExamplesPeruskoulutus {
  val uusi = TorOppija(
    exampleHenkilö,
    List(PeruskouluOpiskeluOikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      päättymispäivä = None,
      oppilaitos = jyväskylänNormaalikoulu,
      suoritukset = Nil,
      opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(
        List(
          Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen, Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None))),
          Opiskeluoikeusjakso(date(2016, 1, 10), None, opiskeluoikeusPäättynyt, Some(KoodistoKoodiViite("4", Some("Työnantajan kokonaan rahoittama"), "opintojenrahoitus", None)))
        )
      )),
      läsnäolotiedot = None
    ))
  )
  val examples = List(Example("peruskoulutus - uusi", "Uusi oppija lisätään suorittamaan peruskoulua", uusi))
}
