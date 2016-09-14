package fi.oph.koski.documentation

import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._
import ExampleData.tilaValmis
import java.time.LocalDate.{of => date}

object ExamplesIB {
  val ressunLukio: Oppilaitos = Oppilaitos(MockOrganisaatiot.ressunLukio, Some(Koodistokoodiviite("00082", None, "oppilaitosnumero", None)), Some("Ressun lukio"))
  val preIBSuoritus = PreIBSuoritus(
    toimipiste = ressunLukio,
    tila = tilaValmis,
    vahvistus = ExampleData.vahvistus(),
    osasuoritukset = Some(List(
      PreIBOppiaineenSuoritus(
        koulutusmoduuli = LukioExampleData.oppiaine("MU"),
        tila = tilaValmis,
        osasuoritukset = Some(List(
          PreIBKurssinSuoritus(
            koulutusmoduuli = LukioExampleData.valtakunnallinenKurssi("MU1"),
            tila = tilaValmis,
            arviointi = LukioExampleData.kurssinArviointi("8")
          )
        ))
      )
    ))
  )

  val opiskeluoikeus = IBOpiskeluoikeus(
    oppilaitos = ressunLukio,
    alkamispäivä = Some(date(2012, 9, 1)),
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen),
        LukionOpiskeluoikeusjakso(date(2016, 1, 10), LukioExampleData.opiskeluoikeusPäättynyt)
      )
    ),
    suoritukset = List(preIBSuoritus)
  )

  val examples = List(Example("ib - pre-ib", "Oppija on suorittanut Pre-IB-vuoden", Oppija(MockOppijat.ibOpiskelija.vainHenkilötiedot, List(opiskeluoikeus))))
}
