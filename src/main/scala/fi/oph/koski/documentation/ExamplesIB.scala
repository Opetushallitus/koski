package fi.oph.koski.documentation

import java.time.LocalDate

import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._
import ExampleData.{helsinki, tilaValmis}
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.localization.LocalizedString

object ExamplesIB {
  val ressunLukio: Oppilaitos = Oppilaitos(MockOrganisaatiot.ressunLukio, Some(Koodistokoodiviite("00082", None, "oppilaitosnumero", None)), Some("Ressun lukio"))
  val preIBSuoritus = PreIBSuoritus(
    toimipiste = ressunLukio,
    tila = tilaValmis,
    vahvistus = ExampleData.vahvistus(org = ressunLukio, kunta = helsinki),
    osasuoritukset = Some(List(
      preIBAineSuoritus(lukionÄidinkieli("AI1"),List(
        (valtakunnallinenKurssi("ÄI1"), "8"), (valtakunnallinenKurssi("ÄI2"), "8"), (valtakunnallinenKurssi("ÄI3"), "8")
      )).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(lukionKieli("A1", "EN"), List(
        (valtakunnallinenKurssi("ENA1"), "10"), (valtakunnallinenKurssi("ENA2"), "10"), (valtakunnallinenKurssi("ENA5"), "10")
      )).copy(arviointi = arviointi("10")),
      preIBAineSuoritus(lukionKieli("B1", "SV"), List(
        (valtakunnallinenKurssi("RUB11"), "8"), (valtakunnallinenKurssi("RUB12"), "7")
      )).copy(arviointi = arviointi("7")),
      preIBAineSuoritus(lukionKieli("B2", "FR"), List(
        (syventäväKurssi("RAN3", "Ravintolaranska", "Patongit etc"), "9")
      )).copy(arviointi = arviointi("9")),
      preIBAineSuoritus(lukionKieli("B3", "ES"), List(
        (syventäväKurssi("ES1", "Turistiespanja", "Espanjan alkeet"), "S")
      )).copy(arviointi = arviointi("6")),
      preIBAineSuoritus(matematiikka("MAA"), List(
        (valtakunnallinenKurssi("MAA11"), "7"),
        (valtakunnallinenKurssi("MAA12"), "7"),
        (valtakunnallinenKurssi("MAA13"), "7"),
        (valtakunnallinenKurssi("MAA2"), "7")
      )).copy(arviointi = arviointi("7")),
      preIBAineSuoritus(lukionOppiaine("BI"), List(
        (valtakunnallinenKurssi("BI1"), "8"), (syventäväKurssi("BI10", "Biologian erikoiskurssi", "Geenihommia"), "S")
      )).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(lukionOppiaine("GE"), List((valtakunnallinenKurssi("GE2"), "10"))).copy(arviointi = arviointi("10")),
      preIBAineSuoritus(lukionOppiaine("FY"), List((valtakunnallinenKurssi("FY1"), "7"))).copy(arviointi = arviointi("7")),
      preIBAineSuoritus(lukionOppiaine("KE"), List((valtakunnallinenKurssi("KE1"), "8"))).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(lukionOppiaine("KT"), List((valtakunnallinenKurssi("UK4"), "10"))).copy(arviointi = arviointi("10")),
      preIBAineSuoritus(lukionOppiaine("FI"), List((valtakunnallinenKurssi("FI1"), "S"))).copy(arviointi = arviointi("7")),
      preIBAineSuoritus(lukionOppiaine("PS"), List((valtakunnallinenKurssi("PS1"), "8"))).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(lukionOppiaine("HI"), List(
        (valtakunnallinenKurssi("HI3"), "9"),
        (valtakunnallinenKurssi("HI4"), "8"),
        (syventäväKurssi("HI10", "Ajan lyhyt historia", "Juuh elikkäs"), "S")
      )).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(lukionOppiaine("YH"), List((valtakunnallinenKurssi("YH1"), "8"))).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(lukionOppiaine("LI"), List((valtakunnallinenKurssi("LI1"), "8"))).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(lukionOppiaine("MU"), List((valtakunnallinenKurssi("MU1"), "8"))).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(lukionOppiaine("KU"), List((valtakunnallinenKurssi("KU1"), "9"))).copy(arviointi = arviointi("9")),
      preIBAineSuoritus(lukionOppiaine("TE"), List((valtakunnallinenKurssi("TE1"), "7"))).copy(arviointi = arviointi("7")),
      preIBAineSuoritus(lukionOppiaine("OP"), List((valtakunnallinenKurssi("OP1"), "S"))).copy(arviointi = arviointi("7"))
    ))
  )

  val standardLevel = "SL"
  val higherLevel = "HL"

  def osasuoritukset(predicted: Boolean): List[IBOppiaineenSuoritus] = List(
    ibAineSuoritus(ibKieli("A", "FI", standardLevel, 1), ibArviointi("4", predicted = predicted), List(
      (ibKurssi("FIN_S1", "A Finnish standard level 1"), "4", Some("B")),
      (ibKurssi("FIN_S2", "A Finnish standard level 2"), "4", Some("B")),
      (ibKurssi("FIN_S3", "A Finnish standard level 3"), "S", None),
      (ibKurssi("FIN_S4", "A Finnish standard level 4"), "5", Some("C")),
      (ibKurssi("FIN_S5", "A Finnish standard level 5"), "6", Some("B")),
      (ibKurssi("FIN_S6", "A Finnish standard level 6"), "5", Some("B")),
      (ibKurssi("FIN_S7", "A Finnish standard level 7"), "5", Some("B")),
      (ibKurssi("FIN_S8", "A Finnish standard level 8"), "S", None),
      (ibKurssi("FIN_S9", "A Finnish standard level 9"), "5", Some("C"))
    )),
    ibAineSuoritus(ibKieli("A2", "EN", higherLevel, 1), ibArviointi("7", predicted = predicted), List(
      (ibKurssi("ENG_B_H1", "B English higher level 1"), "6", Some("A")),
      (ibKurssi("ENG_B_H2", "B English higher level 2"), "7", None),
      (ibKurssi("ENG_B_H4", "B English higher level 4"), "S", None),
      (ibKurssi("ENG_B_H5", "B English higher level 5"), "6", None),
      (ibKurssi("ENG_B_H6", "B English higher level 6"), "6", None),
      (ibKurssi("ENG_B_H8", "B English higher level 8"), "5", None)
    )),
    ibAineSuoritus(ibOppiaine("HIS", higherLevel, 3), ibArviointi("6", predicted = predicted), List(
      (ibKurssi("HIS_H3", "History higher level 3"), "6", Some("A")),
      (ibKurssi("HIS_H4", "History higher level 4"), "6", Some("A")),
      (ibKurssi("HIS_H5", "History higher level 5"), "7", Some("B")),
      (ibKurssi("HIS_H6", "History higher level 6"), "6", Some("A")),
      (ibKurssi("HIS_H7", "History higher level 7"), "1", Some("C")),
      (ibKurssi("HIS_H9", "History higher level 9"), "S", None)
    )),
    ibAineSuoritus(ibOppiaine("PSY", standardLevel, 3), ibArviointi("7", predicted = predicted), List(
      (ibKurssi("PSY_S1", "Psychology standard level 1"), "6", Some("A")),
      (ibKurssi("PSY_S2", "Psychology standard level 2"), "6", Some("B")),
      (ibKurssi("PSY_S3", "Psychology standard level 3"), "6", Some("B")),
      (ibKurssi("PSY_S4", "Psychology standard level 4"), "5", Some("B")),
      (ibKurssi("PSY_S5", "Psychology standard level 5"), "S", None),
      (ibKurssi("PSY_S6", "Psychology standard level 6"), "6", Some("B")),
      (ibKurssi("PSY_S7", "Psychology standard level 7"), "5", Some("B")),
      (ibKurssi("PSY_S8", "Psychology standard level 8"), "2", Some("C")),
      (ibKurssi("PSY_S9", "Psychology standard level 9"), "S", None)
    )),
    ibAineSuoritus(ibOppiaine("BIO", higherLevel, 4), ibArviointi("5", predicted = predicted), List(
      (ibKurssi("BIO_H1", "Biology higher level 1"), "5", Some("B")),
      (ibKurssi("BIO_H2", "Biology higher level 2"), "4", Some("B")),
      (ibKurssi("BIO_H3", "Biology higher level 3"), "S", None),
      (ibKurssi("BIO_H4", "Biology higher level 4"), "5", Some("B")),
      (ibKurssi("BIO_H5", "Biology higher level 5"), "5", Some("B")),
      (ibKurssi("BIO_H6", "Biology higher level 6"), "2", Some("B")),
      (ibKurssi("BIO_H7", "Biology higher level 7"), "3", Some("C")),
      (ibKurssi("BIO_H8", "Biology higher level 8"), "4", Some("C")),
      (ibKurssi("BIO_H9", "Biology higher level 9"), "1", Some("C"))
    )),
    ibAineSuoritus(ibOppiaine("MATST", standardLevel, 5), ibArviointi("5", predicted = predicted), List(
      (ibKurssi("MATST_S1", "Mathematical studies standard level 1"), "5", Some("A")),
      (ibKurssi("MATST_S2", "Mathematical studies standard level 2"), "7", Some("A")),
      (ibKurssi("MATST_S3", "Mathematical studies standard level 3"), "6", Some("A")),
      (ibKurssi("MATST_S4", "Mathematical studies standard level 4"), "6", Some("A")),
      (ibKurssi("MATST_S5", "Mathematical studies standard level 5"), "4", Some("B")),
      (ibKurssi("MATST_S6", "Mathematical studies standard level 6"), "S", None)
    ))
  )

  def ibTutkinnonSuoritus(predicted: Boolean) = IBTutkinnonSuoritus(
    toimipiste = ressunLukio,
    tila = tilaValmis,
    vahvistus = ExampleData.vahvistus(org = ressunLukio, kunta = helsinki),
    osasuoritukset = Some(osasuoritukset(predicted = predicted)),
    theoryOfKnowledge = Some(IBTheoryOfKnowledgeSuoritus(
      IBOppiaineTheoryOfKnowledge(), tilaValmis, ibCoreArviointi("A", predicted = predicted), osasuoritukset = Some(List(
        IBKurssinSuoritus(ibKurssi("TOK1", "TOK1"), tilaValmis, ibKurssinArviointi("S"), None),
        IBKurssinSuoritus(ibKurssi("TOK2", "TOK2"), tilaValmis, ibKurssinArviointi("S"), None)
      ))
    )),
    extendedEssay = Some(IBExtendedEssaySuoritus(
      IBOppiaineExtendedEssay(
        aine = ibKieli("A2", "EN", higherLevel, 1),
        aihe = LocalizedString.english("How is the theme of racial injustice treated in Harper Lee's To Kill a Mockingbird and Solomon Northup's 12 Years a Slave")
      ),
      tilaValmis, ibCoreArviointi("B", predicted = predicted)
    )),
    creativityActionService = Some(IBCASSuoritus(
      IBOppiaineCAS(laajuus = Some(LaajuusTunneissa(267))), tilaValmis, ibArviointi("S", predicted = predicted)
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

  def ibAineSuoritus(oppiaine: IBAineRyhmäOppiaine, arviointi: Option[List[IBOppiaineenArviointi]], kurssit: List[(IBKurssi, String, Option[String])] = Nil) = IBOppiaineenSuoritus(
    koulutusmoduuli = oppiaine,
    tila = tilaValmis,
    osasuoritukset = Some(kurssit.map { case (kurssi, kurssinArvosana, effort) =>
      IBKurssinSuoritus(koulutusmoduuli = kurssi, tila = tilaValmis, arviointi = ibKurssinArviointi(kurssinArvosana, effort))
    }),
    arviointi = arviointi
  )

  def ibOppiaine(aine: String, taso: String, ryhmä: Int) = IBOppiaineMuu(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = aine),
    laajuus = None,
    taso = Some(Koodistokoodiviite(koodiarvo = taso, koodistoUri = "oppiaineentasoib")),
    ryhmä = Koodistokoodiviite(koodiarvo = ryhmä.toString, koodistoUri = "aineryhmaib")
  )

  def ibKieli(aine: String, kieli: String, taso: String, ryhmä: Int) = IBOppiaineLanguage(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = aine),
    laajuus = None,
    taso = Some(Koodistokoodiviite(koodiarvo = taso, koodistoUri = "oppiaineentasoib")),
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima"),
    ryhmä = Koodistokoodiviite(koodiarvo = ryhmä.toString, koodistoUri = "aineryhmaib")
  )

  def ibKurssi(kurssi: String, kuvaus: String) = IBKurssi(
    kuvaus = kuvaus,
    tunniste = PaikallinenKoodi(koodiarvo = kurssi, LocalizedString.english(kurssi)),
    pakollinen = true,
    laajuus = None
  )

  def ibArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4), predicted: Boolean): Some[List[IBOppiaineenArviointi]] = {
    Some(List(IBOppiaineenArviointi(predicted = predicted, arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoib"), Some(päivä))))
  }

  def ibCoreArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4), predicted: Boolean): Some[List[IBCoreRequirementsArviointi]] = {
    Some(List(IBCoreRequirementsArviointi(predicted = predicted, arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkocorerequirementsib"), päivä = Some(päivä))))
  }

  def ibKurssinArviointi(arvosana: String, effort: Option[String] = None, päivä: LocalDate = date(2016, 6, 4)): Some[List[IBKurssinArviointi]] =
    Some(List(IBKurssinArviointi(
      arvosana = Koodistokoodiviite(koodiarvo = arvosana,
      koodistoUri = "arviointiasteikkoib"),
      effort = effort.map(e => Koodistokoodiviite(koodiarvo = e, koodistoUri = "effortasteikkoib")),
      päivä = päivä
    )))

  val opiskeluoikeus = IBOpiskeluoikeus(
    oppilaitos = ressunLukio,
    alkamispäivä = Some(date(2012, 9, 1)),
    päättymispäivä = Some(date(2016, 1, 10)),
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen),
        LukionOpiskeluoikeusjakso(date(2016, 1, 10), LukioExampleData.opiskeluoikeusPäättynyt)
      )
    ),
    suoritukset = List(preIBSuoritus, ibTutkinnonSuoritus(predicted = false))
  )

  val opiskeluoikeusPredictedGrades = opiskeluoikeus.copy(
    suoritukset = List(opiskeluoikeus.suoritukset.head, ibTutkinnonSuoritus(predicted = true))
  )

  val examples = List(
    Example("ib - final grades", "Oppija on suorittanut pre-IB vuoden ja IB-tutkinnon, IBO on vahvistanut arvosanat", Oppija(MockOppijat.ibFinal.vainHenkilötiedot, List(opiskeluoikeus))),
    Example("ib - predicted grades", "Oppija on suorittanut pre-IB vuoden ja IB-tutkinnon, IBO ei ole vahvistanut arvosanoja", Oppija(MockOppijat.ibPredicted.vainHenkilötiedot, List(opiskeluoikeusPredictedGrades)))
  )
}
