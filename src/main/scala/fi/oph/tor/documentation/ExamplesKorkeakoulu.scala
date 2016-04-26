package fi.oph.tor.documentation

import java.time.LocalDate.{of => date}

import fi.oph.tor.documentation.ExampleData._
import fi.oph.tor.documentation.KorkeakouluTestdata._
import fi.oph.tor.localization.LocalizedStringImplicits._
import fi.oph.tor.oppija.MockOppijat
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._

object ExamplesKorkeakoulu {

  val uusi = Oppija(
    oppija,
    List(KorkeakoulunOpiskeluoikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      päättymispäivä = None,
      oppilaitos = helsinginYliopisto, None,
      suoritukset = Nil,
      tila = Some(KorkeakoulunOpiskeluoikeudenTila(
        List(
          KorkeakoulunOpiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen)
        )
      )),
      läsnäolotiedot = None
    ))
  )

  val examples = List(Example("korkeakoulu - uusi", "Uusi oppija lisätään suorittamaan korkeakoulututkintoa", uusi))

}

object KorkeakouluTestdata {
  lazy val oppija = MockOppijat.korkeakoululainen.vainHenkilötiedot
  lazy val helsinginYliopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.helsinginYliopisto, Some(Koodistokoodiviite("01901", None, "oppilaitosnumero", None)), Some("Helsingin yliopisto"))
}