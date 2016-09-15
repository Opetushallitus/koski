package fi.oph.koski.documentation

import java.time.LocalDate

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
      preIBAineSuoritus(lukionÄidinkieli("AI1"), List((valtakunnallinenKurssi("ÄI1"), "8"), (valtakunnallinenKurssi("ÄI2"), "8"), (valtakunnallinenKurssi("ÄI3"), "8"))),
      preIBAineSuoritus(lukionKieli("A1", "EN"), List((valtakunnallinenKurssi("ENA1"), "10"), (valtakunnallinenKurssi("ENA2"), "10"), (valtakunnallinenKurssi("ENA5"), "10"))),
      preIBAineSuoritus(lukionKieli("B1", "SV"), List((valtakunnallinenKurssi("RUB11"), "8"), (valtakunnallinenKurssi("RUB12"), "7"))),
      preIBAineSuoritus(lukionKieli("B2", "FR"), List((paikallinenKurssi("RAN3", "Ravintolaranska", "Patongit etc"), "9"))),
      preIBAineSuoritus(lukionKieli("B3", "ES"), List((paikallinenKurssi("ES1", "Turistiespanja", "Espanjan alkeet"), "S"))),
      preIBAineSuoritus(matematiikka("MAA"), List((valtakunnallinenKurssi("MAA11"), "7"), (valtakunnallinenKurssi("MAA12"), "7"), (valtakunnallinenKurssi("MAA13"), "7"), (valtakunnallinenKurssi("MAA2"), "7"))),
      preIBAineSuoritus(lukionOppiaine("BI"), List((valtakunnallinenKurssi("BI1"), "8"), (paikallinenKurssi("BI10", "Biologian erikoiskurssi", "Geenihommia"), "S"))),
      preIBAineSuoritus(lukionOppiaine("GE"), List((valtakunnallinenKurssi("GE2"), "10"))),
      preIBAineSuoritus(lukionOppiaine("FY"), List((valtakunnallinenKurssi("FY1"), "7"))),
      preIBAineSuoritus(lukionOppiaine("KE"), List((valtakunnallinenKurssi("KE1"), "8"))),
      preIBAineSuoritus(lukionOppiaine("KT"), List((valtakunnallinenKurssi("UK4"), "10"))),
      preIBAineSuoritus(lukionOppiaine("FI"), List((valtakunnallinenKurssi("FI1"), "S"))),
      preIBAineSuoritus(lukionOppiaine("PS"), List((valtakunnallinenKurssi("PS1"), "8"))),
      preIBAineSuoritus(lukionOppiaine("HI"), List((valtakunnallinenKurssi("HI3"), "9"), (valtakunnallinenKurssi("HI4"), "8"), (paikallinenKurssi("HI10", "Ajan lyhyt historia", "Juuh elikkäs"), "S"))),
      preIBAineSuoritus(lukionOppiaine("YH"), List((valtakunnallinenKurssi("YH1"), "8"))),
      preIBAineSuoritus(lukionOppiaine("LI"), List((valtakunnallinenKurssi("LI1"), "8"))),
      preIBAineSuoritus(lukionOppiaine("MU"), List((valtakunnallinenKurssi("MU1"), "8"))),
      preIBAineSuoritus(lukionOppiaine("KU"), List((valtakunnallinenKurssi("KU1"), "9"))),
      preIBAineSuoritus(lukionOppiaine("TE"), List((valtakunnallinenKurssi("TE1"), "7"))),
      preIBAineSuoritus(lukionOppiaine("OP"), List((valtakunnallinenKurssi("OP1"), "S")))
    ))
  )

  val standardLevel = "SL"
  val higherLevel = "HL"

  val ibTutkinnonSuoritus = IBTutkinnonSuoritus(
    toimipiste = ressunLukio,
    tila = tilaValmis,
    vahvistus = ExampleData.vahvistus(),
    osasuoritukset = Some(List(
      ibAineSuoritus(ibKieli("A", "FI", standardLevel), "4"),
      ibAineSuoritus(ibKieli("A2", "EN", higherLevel), "7"),
      ibAineSuoritus(ibOppiaine("HIS", higherLevel), "6"),
      ibAineSuoritus(ibOppiaine("PSY", standardLevel), "7"),
      ibAineSuoritus(ibOppiaine("BIO", higherLevel), "5"),
      ibAineSuoritus(ibOppiaine("MATST", standardLevel), "5"),
      ibAineSuoritus(ibCoreOppiaine("TOK"), "S")
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

  def ibAineSuoritus(oppiaine: IBOppiaine, arvosana: String) = IBOppiaineenSuoritus(
    koulutusmoduuli = oppiaine,
    tila = tilaValmis,
    osasuoritukset = None,
    arviointi = ibArviointi(arvosana)
  )

  def ibCoreOppiaine(aine: String) = IBCoreElementOppiaine(tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = aine), laajuus = None)

  def ibOppiaine(aine: String, taso: String) = MuuIBOppiaine(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = aine),
    laajuus = None,
    taso = Some(Koodistokoodiviite(koodiarvo = taso, koodistoUri = "oppiaineentasoib"))
  )

  def ibKieli(aine: String, kieli: String, taso: String) = Language(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = aine),
    laajuus = None,
    taso = Some(Koodistokoodiviite(koodiarvo = taso, koodistoUri = "oppiaineentasoib")),
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima")
  )

  def ibKurssi(kurssi: String) = IBKurssi(
    tunniste = Koodistokoodiviite(koodistoUri = "ibkurssit", koodiarvo = kurssi),
    pakollinen = true,
    laajuus = None
  )

  def ibArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[IBOppiaineenArviointi]] = {
    Some(List(IBOppiaineenArviointi(predicted = false, arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoib"), Some(päivä))))
  }

  val opiskeluoikeus = IBOpiskeluoikeus(
    oppilaitos = ressunLukio,
    alkamispäivä = Some(date(2012, 9, 1)),
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen),
        LukionOpiskeluoikeusjakso(date(2016, 1, 10), LukioExampleData.opiskeluoikeusPäättynyt)
      )
    ),
    suoritukset = List(preIBSuoritus, ibTutkinnonSuoritus)
  )

  val examples = List(
    Example("ib - pre-ib ja ib", "Oppija on suorittanut pre-IB vuoden ja IB-tutkinnon", Oppija(MockOppijat.ibOpiskelija.vainHenkilötiedot, List(opiskeluoikeus)))
  )
}
