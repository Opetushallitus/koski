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
    vahvistus = ExampleData.vahvistus(org = ressunLukio, kunta = helsinki),
    osasuoritukset = Some(List(
      ibAineSuoritus(ibKieli("A", "FI", standardLevel), "4", List(
        (ibKurssi("FIN_S1"), "4", Some("B")),
        (ibKurssi("FIN_S2"), "4", Some("B")),
//        (ibKurssi("FIN_S3"), "S", None), // Commented out for now to circumvent a performance issue in katselukäyttöliittymä
//        (ibKurssi("FIN_S4"), "5", Some("C")),
//        (ibKurssi("FIN_S5"), "6", Some("B")),
//        (ibKurssi("FIN_S6"), "5", Some("B")),
//        (ibKurssi("FIN_S7"), "5", Some("B")),
//        (ibKurssi("FIN_S8"), "S", None),
        (ibKurssi("FIN_S9"), "5", Some("C"))
      )),
      ibAineSuoritus(ibKieli("A2", "EN", higherLevel), "7", List(
        (ibKurssi("ENG_B_H1"), "6", Some("A")),
        (ibKurssi("ENG_B_H2"), "7", None),
//        (ibKurssi("ENG_B_H4"), "S", None),
//        (ibKurssi("ENG_B_H5"), "6", None),
//        (ibKurssi("ENG_B_H6"), "6", None),
        (ibKurssi("ENG_B_H8"), "5", None)
      )),
      ibAineSuoritus(ibOppiaine("HIS", higherLevel), "6", List(
        (ibKurssi("HIS_H3"), "6", Some("A")),
        (ibKurssi("HIS_H4"), "6", Some("A")),
//        (ibKurssi("HIS_H5"), "7", Some("B")),
//        (ibKurssi("HIS_H6"), "6", Some("A")),
//        (ibKurssi("HIS_H7"), "1", Some("C")),
        (ibKurssi("HIS_H9"), "S", None)
      )),
      ibAineSuoritus(ibOppiaine("PSY", standardLevel), "7", List(
        (ibKurssi("PSY_S1"), "6", Some("A")),
//        (ibKurssi("PSY_S2"), "6", Some("B")),
//        (ibKurssi("PSY_S3"), "6", Some("B")),
//        (ibKurssi("PSY_S4"), "5", Some("B")),
//        (ibKurssi("PSY_S5"), "S", None),
//        (ibKurssi("PSY_S6"), "6", Some("B")),
//        (ibKurssi("PSY_S7"), "5", Some("B")),
        (ibKurssi("PSY_S8"), "2", Some("C")),
        (ibKurssi("PSY_S9"), "S", None)
      )),
      ibAineSuoritus(ibOppiaine("BIO", higherLevel), "5", List(
        (ibKurssi("BIO_H1"), "5", Some("B")),
//        (ibKurssi("BIO_H2"), "4", Some("B")),
//        (ibKurssi("BIO_H3"), "S", None),
//        (ibKurssi("BIO_H4"), "5", Some("B")),
//        (ibKurssi("BIO_H5"), "5", Some("B")),
//        (ibKurssi("BIO_H6"), "2", Some("B")),
//        (ibKurssi("BIO_H7"), "3", Some("C")),
        (ibKurssi("BIO_H8"), "4", Some("C")),
        (ibKurssi("BIO_H9"), "1", Some("C"))
      )),
      ibAineSuoritus(ibOppiaine("MATST", standardLevel), "5", List(
        (ibKurssi("MATST_S1"), "5", Some("A")),
        (ibKurssi("MATST_S2"), "7", Some("A")),
//        (ibKurssi("MATST_S3"), "6", Some("A")),
//        (ibKurssi("MATST_S4"), "6", Some("A")),
//        (ibKurssi("MATST_S5"), "4", Some("B")),
        (ibKurssi("MATST_S6"), "S", None)
      )),
      ibAineSuoritus(IBOppiaineTheoryOfKnowledge(), "S", List(
        (ibKurssi("TOK1"), "S", None),
        (ibKurssi("TOK2"), "S", None)
      )),
      ibAineSuoritus(IBOppiaineCAS(laajuus = Some(LaajuusTunneissa(267))), "S", List(
        (ibKurssi("CAS1"), "S", None)
      )),
      ibAineSuoritus(
        IBOppiaineExtendedEssay(
          aine = ibKieli("A2", "EN", higherLevel),
          aihe = LocalizedString.english("How is the theme of racial injustice treated in Harper Lee's To Kill a Mockingbird and Solomon Northup's 12 Years a Slave")
        ), "S", List((ibKurssi("EE1"), "S", None)))
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

  def ibAineSuoritus(oppiaine: IBOppiaine, arvosana: String, kurssit: List[(IBKurssi, String, Option[String])] = Nil) = IBOppiaineenSuoritus(
    koulutusmoduuli = oppiaine,
    tila = tilaValmis,
    osasuoritukset = Some(kurssit.map { case (kurssi, kurssinArvosana, effort) =>
      IBKurssinSuoritus(koulutusmoduuli = kurssi, tila = tilaValmis, arviointi = ibKurssinArviointi(kurssinArvosana, effort))
    }),
    arviointi = ibArviointi(arvosana)
  )

  def ibOppiaine(aine: String, taso: String) = IBOppiaineMuu(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = aine),
    laajuus = None,
    taso = Some(Koodistokoodiviite(koodiarvo = taso, koodistoUri = "oppiaineentasoib"))
  )

  def ibKieli(aine: String, kieli: String, taso: String) = IBOppiaineLanguage(
    tunniste = Koodistokoodiviite(koodistoUri = "oppiaineetib", koodiarvo = aine),
    laajuus = None,
    taso = Some(Koodistokoodiviite(koodiarvo = taso, koodistoUri = "oppiaineentasoib")),
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima")
  )

  def ibKurssi(kurssi: String) = IBKurssi(
    tunniste = PaikallinenKoodi(koodiarvo = kurssi, LocalizedString.english(kurssi)),
    pakollinen = true,
    laajuus = None
  )

  def ibArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[IBOppiaineenArviointi]] = {
    Some(List(IBOppiaineenArviointi(predicted = false, arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoib"), Some(päivä))))
  }

  def ibKurssinArviointi(arvosana: String, effort: Option[String], päivä: LocalDate = date(2016, 6, 4)): Some[List[IBKurssinArviointi]] =
    Some(List(IBKurssinArviointi(
      arvosana = Koodistokoodiviite(koodiarvo = arvosana,
      koodistoUri = "arviointiasteikkoib"),
      effort = effort.map(e => Koodistokoodiviite(koodiarvo = e, koodistoUri = "effortasteikkoib")),
      päivä = päivä
    )))

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
