package fi.oph.koski.documentation

import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._
import ExampleData.tilaValmis
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.LukioExampleData._

object ExamplesIB {
  val ressunLukio: Oppilaitos = Oppilaitos(MockOrganisaatiot.ressunLukio, Some(Koodistokoodiviite("00082", None, "oppilaitosnumero", None)), Some("Ressun lukio"))
  val preIBSuoritus = PreIBSuoritus(
    toimipiste = ressunLukio,
    tila = tilaValmis,
    vahvistus = ExampleData.vahvistus(),
    osasuoritukset = Some(List(
      preIBAineSuoritus(lukionOppiaine("MU"), List((valtakunnallinenKurssi("MU1"), "8"))),
      preIBAineSuoritus(lukionOppiaine("KU"), List((valtakunnallinenKurssi("KU1"), "S"), (valtakunnallinenKurssi("KU2"), "S"), (valtakunnallinenKurssi("KU3"), "S"), (valtakunnallinenKurssi("KU4"), "S"))),
      preIBAineSuoritus(lukionOppiaine("BI"), List((valtakunnallinenKurssi("BI1"), "6"))),
      preIBAineSuoritus(lukionOppiaine("KE"), List((valtakunnallinenKurssi("KE1"), "7"))),
      preIBAineSuoritus(lukionKieli("A1", "EN"), List((valtakunnallinenKurssi("ENA1"), "7"), (valtakunnallinenKurssi("ENA2"), "8"), (valtakunnallinenKurssi("ENA3"), "8"), (valtakunnallinenKurssi("ENA4"), "8")))
    ))
  )

  def preIBAineSuoritus(oppiaine: PreIBOppiaine, kurssit: List[(PreIBKurssi, String)]) = PreIBOppiaineenSuoritus(
    koulutusmoduuli = oppiaine,
    tila = tilaValmis,
    osasuoritukset = Some(kurssit.map { case (kurssi, arvosana) =>
      PreIBKurssinSuoritus(
        koulutusmoduuli = kurssi,
        tila = tilaValmis,
        arviointi = LukioExampleData.kurssinArviointi(arvosana)
      )
    })
  )

  def ibOppiaine(aine: String) = MuuIBOppiaine(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = aine),
    laajuus = None,
    taso = None
  )

  def ibKurssi(kurssi: String) = IBKurssi(
    tunniste = Koodistokoodiviite(koodistoUri = "ibkurssit", koodiarvo = kurssi),
    pakollinen = true,
    laajuus = None
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
