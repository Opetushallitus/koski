package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.ExampleData.{englanti, helsinki, ruotsinKieli}
import fi.oph.koski.documentation.IBExampleData._
import fi.oph.koski.documentation.LukioExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{jyväskylänNormaalikoulu, ressunLukio}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOppijat}
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits.str2localized
import fi.oph.koski.util.OptionalLists.optionalList

object ExamplesIB {
  val preIBSuoritus = PreIBSuoritus2015(
    toimipiste = ressunLukio,
    vahvistus = ExampleData.vahvistusPaikkakunnalla(org = ressunLukio, kunta = helsinki),
    suorituskieli = englanti,
    osasuoritukset = Some(List(
      preIBAineSuoritus(LukioExampleData.lukionÄidinkieli("AI1", pakollinen = true),List(
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
      preIBAineSuoritus(LukioExampleData.matematiikka("MAA", None), List(
        (valtakunnallinenKurssi("MAA11"), "7"),
        (valtakunnallinenKurssi("MAA12"), "7"),
        (valtakunnallinenKurssi("MAA13"), "7"),
        (valtakunnallinenKurssi("MAA2"), "7")
      )).copy(arviointi = arviointi("7")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("BI", None), List(
        (valtakunnallinenKurssi("BI1"), "8"), (syventäväKurssi("BI10", "Biologian erikoiskurssi", "Geenihommia"), "S")
      )).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("GE", None), List((valtakunnallinenKurssi("GE2"), "10"))).copy(arviointi = arviointi("10")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("FY", None), List((valtakunnallinenKurssi("FY1"), "7"))).copy(arviointi = arviointi("7")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("KE", None), List((valtakunnallinenKurssi("KE1"), "8"))).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(lukionUskonto(uskonto = None, diaarinumero = None), List((valtakunnallinenKurssi("UK4"), "10"))).copy(arviointi = arviointi("10")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("FI", None), List((valtakunnallinenKurssi("FI1"), "S"))).copy(arviointi = arviointi("7")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("PS", None), List((valtakunnallinenKurssi("PS1"), "8"))).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("HI", None), List(
        (valtakunnallinenKurssi("HI3"), "9"),
        (valtakunnallinenKurssi("HI4"), "8"),
        (syventäväKurssi("HI10", "Ajan lyhyt historia", "Juuh elikkäs"), "S")
      )).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("YH", None), List((valtakunnallinenKurssi("YH1"), "8"))).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("LI", None), List((valtakunnallinenKurssi("LI1"), "8"))).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("MU", None), List((valtakunnallinenKurssi("MU1"), "8"))).copy(arviointi = arviointi("8")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("KU", None), List((valtakunnallinenKurssi("KU1"), "9"))).copy(arviointi = arviointi("9")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("TE", None), List((valtakunnallinenKurssi("TE1"), "7"))).copy(arviointi = arviointi("7")),
      preIBAineSuoritus(LukioExampleData.lukionOppiaine("OP", None), List((valtakunnallinenKurssi("OP1"), "S"))).copy(arviointi = arviointi("7")),
      MuidenLukioOpintojenSuoritus2015(
        koulutusmoduuli = MuuLukioOpinto2015(Koodistokoodiviite("TO", "lukionmuutopinnot")),
        osasuoritukset = Some(List(
          kurssisuoritus(soveltavaKurssi("MTA", "Monitieteinen ajattelu", "Monitieteisen ajattelun kurssi")).copy(arviointi = sanallinenArviointi("S", päivä = date(2016, 6, 8)))
        )),
        arviointi = arviointi("S")
      )
    ))
  )

  val standardLevel = "SL"
  val higherLevel = "HL"

  val preIBSuoritus2019 = PreIBSuoritus2019(
    toimipiste = ressunLukio,
    vahvistus = ExampleData.vahvistusPaikkakunnalla(org = ressunLukio, kunta = helsinki),
    suorituskieli = englanti,
    omanÄidinkielenOpinnot = Lukio2019ExampleData.omanÄidinkielenOpinnotSaame,
    puhviKoe = Lukio2019ExampleData.puhviKoe,
    suullisenKielitaidonKokeet = Some(List(Lukio2019ExampleData.suullisenKielitaidonKoeEnglanti, Lukio2019ExampleData.suullisenKielitaidonKoeEspanja)),
    ryhmä = Some("AH"),
    todistuksellaNäkyvätLisätiedot = Some("Suorittanut etäopetuskokeiluna"),
    osasuoritukset = Some(List(
      lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionÄidinkieli("AI1", false), List(
        (Lukio2019ExampleData.muuModuuliOppiaineissa("ÄI1"), "8"),
        (Lukio2019ExampleData.muuModuuliOppiaineissa("ÄI2").copy(pakollinen = false), "8")
      )).copy(arviointi = Lukio2019ExampleData.numeerinenLukionOppiaineenArviointi(9)),

      lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.matematiikka("MAA"), List(
        (Lukio2019ExampleData.muuModuuliOppiaineissa("MAB2"), "10"),
        (Lukio2019ExampleData.muuModuuliOppiaineissa("MAB3"), "10")
      )).copy(arviointi = Lukio2019ExampleData.numeerinenLukionOppiaineenArviointi(10)),

      lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionUskonto(Some("KA")), List(
        (Lukio2019ExampleData.muuModuuliOppiaineissa("UK1"), "9")
      )).copy(arviointi = Lukio2019ExampleData.numeerinenLukionOppiaineenArviointi(9)),

      lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("LI"), List(
        (Lukio2019ExampleData.muuModuuliOppiaineissa("LI2"), "8"),
        (Lukio2019ExampleData.paikallinenOpintojakso("LITT1", "Tanssin liikunnallisuus", "Tanssin liikunnallisuus"), "S")
      )).copy(arviointi = Lukio2019ExampleData.numeerinenLukionOppiaineenArviointi(8)),

      lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionOppiaine("FY"), List(
      )).copy(
        suoritettuErityisenäTutkintona = true,
        suorituskieli = Some(ruotsinKieli),
        arviointi = Lukio2019ExampleData.numeerinenLukionOppiaineenArviointi(8)
      ),

      LukionOppiaineenPreIBSuoritus2019(
        koulutusmoduuli = Lukio2019ExampleData.lukionOppiaine("KE"),
        osasuoritukset = Some(List(
          PreIBLukionModuulinSuoritusOppiaineissa2019(
            koulutusmoduuli = Lukio2019ExampleData.muuModuuliOppiaineissa("KE1"),
            arviointi = Lukio2019ExampleData.numeerinenArviointi(6),
            tunnustettu = Some(OsaamisenTunnustaminen(None, "Osoittanut osaamisen käytännössä."))
          )
        )),
        arviointi = Lukio2019ExampleData.numeerinenLukionOppiaineenArviointi(7)
      ),

      lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionKieli2019("A", "EN"), List(
        (Lukio2019ExampleData.vieraanKielenModuuliOppiaineissa("ENA1", 2, Some("EN")), "10"), // TODO: Poista kieli joistain moduuleista, kun se aletaan täyttää automaattisesti validoinnilla
        (Lukio2019ExampleData.vieraanKielenModuuliOppiaineissa("ENA2", 2, Some("EN")), "9")
      )).copy(arviointi = Lukio2019ExampleData.numeerinenLukionOppiaineenArviointi(9)),

      lukionOppiaineenPreIBSuoritus2019(Lukio2019ExampleData.lukionKieli2019("A", "ES"), List(
        (Lukio2019ExampleData.vieraanKielenModuuliOppiaineissa("VKA1", 2, Some("ES")), "6"),
        (Lukio2019ExampleData.vieraanKielenModuuliOppiaineissa("VKA2", 2, Some("ES")), "7")
      )).copy(arviointi = Lukio2019ExampleData.numeerinenLukionOppiaineenArviointi(6)),

      lukionOppiaineenPreIBSuoritus2019(PaikallinenLukionOppiaine2019(PaikallinenKoodi("ITT", "Tanssi ja liike"), "Tanssi ja liike", pakollinen = false), List(
        (Lukio2019ExampleData.paikallinenOpintojakso("ITT234", "Tanssin taito", "Perinteiset suomalaiset tanssit, valssi jne"), "6"),
        (Lukio2019ExampleData.paikallinenOpintojakso("ITT235", "Tanssin taito 2", "Uudemmat suomalaiset tanssit"), "7")
      )).copy(arviointi = Lukio2019ExampleData.numeerinenLukionOppiaineenArviointi(6)),

      muidenlukioOpintojenPreIBSuoritus2019(Lukio2019ExampleData.muutSuoritukset(), List(
        (Lukio2019ExampleData.muuModuuliMuissaOpinnoissa("ÄI1"), "7"),
        (Lukio2019ExampleData.vieraanKielenModuuliMuissaOpinnoissa("VKAAB31", 2, "TH"), "6"),
        (Lukio2019ExampleData.vieraanKielenModuuliMuissaOpinnoissa("RUB11", 2, "RU"), "6") // TODO: Poista kieli joistain moduuleista, kun se aletaan täyttää automaattisesti validoinnilla
      )),

      muidenlukioOpintojenPreIBSuoritus2019(Lukio2019ExampleData.lukiodiplomit(), List(
        (Lukio2019ExampleData.muuModuuliMuissaOpinnoissa("KULD2"), "9")
      )),

      muidenlukioOpintojenPreIBSuoritus2019(Lukio2019ExampleData.temaattisetOpinnot(), List(
        (Lukio2019ExampleData.paikallinenOpintojakso("HAI765", "Kansanmusiikki haitarilla", "Kansamusiikkia 2-rivisellä haitarilla"), "S")
      ))
    ))
  )

  def osasuoritukset(vainPredictedArviointi: Boolean): List[IBOppiaineenSuoritus] = List(
    ibAineSuoritus(
      ibKieli("A", "FI", standardLevel, 1),
      if (vainPredictedArviointi) None else ibArviointi("4"),
      ibPredictedArviointi("4"),
      List(
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
    ibAineSuoritus(
      ibKieli("A2", "EN", higherLevel, 1),
      if (vainPredictedArviointi) None else ibArviointi("7"),
      ibPredictedArviointi("6"),
      List(
        (ibKurssi("ENG_B_H1", "B English higher level 1"), "6", Some("A")),
        (ibKurssi("ENG_B_H2", "B English higher level 2"), "7", None),
        (ibKurssi("ENG_B_H4", "B English higher level 4"), "S", None),
        (ibKurssi("ENG_B_H5", "B English higher level 5"), "6", None),
        (ibKurssi("ENG_B_H6", "B English higher level 6"), "6", None),
        (ibKurssi("ENG_B_H8", "B English higher level 8"), "5", None)
      )),
    ibAineSuoritus(
      ibOppiaine("HIS", higherLevel, 3),
      if (vainPredictedArviointi) None else ibArviointi("6"),
      ibPredictedArviointi("6"),
      List(
        (ibKurssi("HIS_H3", "History higher level 3"), "6", Some("A")),
        (ibKurssi("HIS_H4", "History higher level 4"), "6", Some("A")),
        (ibKurssi("HIS_H5", "History higher level 5"), "7", Some("B")),
        (ibKurssi("HIS_H6", "History higher level 6"), "6", Some("A")),
        (ibKurssi("HIS_H7", "History higher level 7"), "1", Some("C")),
        (ibKurssi("HIS_H9", "History higher level 9"), "S", None)
      )),
    ibAineSuoritus(
      ibOppiaine("PSY", standardLevel, 3),
      if (vainPredictedArviointi) None else ibArviointi("7"),
      ibPredictedArviointi("7"),
      List(
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
    ibAineSuoritus(
      ibOppiaine("BIO", higherLevel, 4),
      if (vainPredictedArviointi) None else ibArviointi("5"),
      ibPredictedArviointi("5"),
      List(
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
    ibAineSuoritus(
      ibOppiaine("MATST", standardLevel, 5),
      if (vainPredictedArviointi) None else ibArviointi("5"),
      ibPredictedArviointi("5"),
      List(
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
    suorituskieli = englanti,
    vahvistus = ExampleData.vahvistusPaikkakunnalla(org = ressunLukio, kunta = helsinki),
    osasuoritukset = Some(osasuoritukset(vainPredictedArviointi = predicted)),
    theoryOfKnowledge = Some(IBTheoryOfKnowledgeSuoritus(
      IBOppiaineTheoryOfKnowledge(), ibCoreArviointi("A", predicted = predicted), osasuoritukset = Some(List(
        IBKurssinSuoritus(ibKurssi("TOK1", "TOK1"), ibKurssinArviointi("S"), None),
        IBKurssinSuoritus(ibKurssi("TOK2", "TOK2"), ibKurssinArviointi("S"), None)
      ))
    )),
    extendedEssay = Some(IBExtendedEssaySuoritus(
      IBOppiaineExtendedEssay(
        aine = ibKieli("A2", "EN", higherLevel, 1),
        aihe = LocalizedString.english("How is the theme of racial injustice treated in Harper Lee's To Kill a Mockingbird and Solomon Northup's 12 Years a Slave")
      ),
      ibCoreArviointi("B", predicted = predicted)
    )),
    creativityActionService = Some(IBCASSuoritus(
      IBOppiaineCAS(laajuus = Some(LaajuusTunneissa(267))), ibCASArviointi("S", predicted = predicted)
    )),
    lisäpisteet = Some(Koodistokoodiviite(koodiarvo = "3", koodistoUri = "arviointiasteikkolisapisteetib"))
  )

  def preIBAineSuoritus(oppiaine: PreIBOppiaine2015, kurssit: List[(PreIBKurssi2015, String)]) = PreIBOppiaineenSuoritus2015(
    koulutusmoduuli = oppiaine,
    osasuoritukset = Some(kurssit.map { case (kurssi, arvosana) =>
      PreIBKurssinSuoritus2015(
        koulutusmoduuli = kurssi,
        arviointi = LukioExampleData.sanallinenArviointi(arvosana)
      )
    })
  )

  def ibAineSuoritus(
    oppiaine: IBAineRyhmäOppiaine,
    päättöarviointi: Option[List[IBOppiaineenArviointi]],
    predictedArviointi: Option[List[IBOppiaineenPredictedArviointi]],
    kurssit: List[(IBKurssi, String, Option[String])] = Nil,
  ) = IBOppiaineenSuoritus(
    koulutusmoduuli = oppiaine,
    osasuoritukset = Some(kurssit.map { case (kurssi, kurssinArvosana, effort) =>
      IBKurssinSuoritus(koulutusmoduuli = kurssi, arviointi = ibKurssinArviointi(kurssinArvosana, effort))
    }),
    arviointi = päättöarviointi,
    predictedArviointi = predictedArviointi,
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

  def ibArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[IBOppiaineenArviointi]] = {
    Some(List(IBOppiaineenArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoib"), päivä = Some(päivä))))
  }

  def ibPredictedArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4)): Some[List[IBOppiaineenPredictedArviointi]] = {
    Some(List(IBOppiaineenPredictedArviointi(arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoib"), päivä = Some(päivä))))
  }

  def ibCASArviointi(arvosana: String, päivä: LocalDate = date(2016, 6, 4), predicted: Boolean): Some[List[IBCASOppiaineenArviointi]] = {
    Some(List(IBCASOppiaineenArviointi(predicted = predicted, arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoib"), päivä = Some(päivä))))
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
    oppilaitos = Some(ressunLukio),
    tila = LukionOpiskeluoikeudenTila(
      List(
        LukionOpiskeluoikeusjakso(date(2012, 9, 1), LukioExampleData.opiskeluoikeusAktiivinen, Some(ExampleData.valtionosuusRahoitteinen)),
        LukionOpiskeluoikeusjakso(date(2016, 6, 4), LukioExampleData.opiskeluoikeusPäättynyt, Some(ExampleData.valtionosuusRahoitteinen))
      )
    ),
    suoritukset = List(preIBSuoritus, ibTutkinnonSuoritus(predicted = false))
  )

  val kaikkiArviointityypitArvioinnissaSisältäväVanhanmallinenOpiskeluoikeus: IBOpiskeluoikeus = opiskeluoikeus.copy(
    suoritukset = opiskeluoikeus.suoritukset.map {
      case suoritus: IBTutkinnonSuoritus =>
        suoritus.copy(
          osasuoritukset = suoritus.osasuoritukset.map(_.map(osasuoritus =>
            osasuoritus.copy(
              arviointi = Some(osasuoritus.predictedArviointi.getOrElse(List.empty).map(IBOppiaineenArviointi.apply) ++ osasuoritus.arviointi.getOrElse(List.empty))
                .flatMap(optionalList),
              predictedArviointi = None
            )
          ))
        )
      case s: IBPäätasonSuoritus => s
    }
  )

  lazy val aktiivinenOpiskeluoikeus: IBOpiskeluoikeus =
    IBOpiskeluoikeus(
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        )
      ),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(vahvistamatonPreIB2019Suoritus)
    )

  lazy val vahvistamatonPreIB2019Suoritus = preIBSuoritus2019.copy(vahvistus = None)

  val opiskeluoikeusPredictedGrades = opiskeluoikeus.copy(
    tila = opiskeluoikeus.tila.copy(List(opiskeluoikeus.tila.opiskeluoikeusjaksot.head)),
    suoritukset = List(opiskeluoikeus.suoritukset.head, ibTutkinnonSuoritus(predicted = true).copy(vahvistus = None))
  )

  val opiskeluoikeusPreIB2019 = opiskeluoikeus.copy(
    suoritukset = List(preIBSuoritus2019)
  )

  val examples = List(
    Example("ib - final grades", "Oppija on suorittanut pre-IB vuoden ja IB-tutkinnon, IBO on vahvistanut arvosanat", Oppija(asUusiOppija(KoskiSpecificMockOppijat.ibFinal), List(opiskeluoikeus))),
    Example("ib - predicted grades", "Oppija on suorittanut pre-IB vuoden ja IB-tutkinnon, IBO ei ole vahvistanut arvosanoja", Oppija(asUusiOppija(KoskiSpecificMockOppijat.ibPredicted), List(opiskeluoikeusPredictedGrades))),
    Example("ib - Pre-IB 2019", "Oppija on suorittanut Pre-IB-opintoja lukion 2019 opetussuunnitelman mukaan", Oppija(asUusiOppija(KoskiSpecificMockOppijat.ibPreIB2019), List(opiskeluoikeusPreIB2019)))
  )
}

object IBExampleData {

  def lukionOppiaineenPreIBSuoritus2019(oppiaine: PreIBLukionOppiaine2019) = LukionOppiaineenPreIBSuoritus2019(
    koulutusmoduuli = oppiaine,
    osasuoritukset = None
  )

  def lukionOppiaineenPreIBSuoritus2019(oppiaine: PreIBLukionOppiaine2019, osasuoritukset: List[(PreIBLukionModuuliTaiPaikallinenOpintojakso2019, String)]) = LukionOppiaineenPreIBSuoritus2019(
    koulutusmoduuli = oppiaine,
    osasuoritukset = Some(osasuoritukset.map {
      case (moduuli:PreIBLukionModuuliOppiaineissa2019, arvosana) =>
        PreIBLukionModuulinSuoritusOppiaineissa2019(
          koulutusmoduuli = moduuli,
          arviointi = Lukio2019ExampleData.sanallinenArviointi(arvosana)
        )
      case (moduuli:PreIBPaikallinenOpintojakso2019, arvosana) =>
        PreIBLukionPaikallisenOpintojaksonSuoritus2019(
          koulutusmoduuli = moduuli,
          arviointi = Lukio2019ExampleData.sanallinenArviointi(arvosana)
        )
      case _ =>
        throw new IllegalArgumentException
    })
  )

  def muidenlukioOpintojenPreIBSuoritus2019(koulutusmoduuli: PreIBMuutSuorituksetTaiVastaavat2019) = MuidenLukioOpintojenPreIBSuoritus2019(
    koulutusmoduuli = koulutusmoduuli,
    osasuoritukset = None
  )

  def muidenlukioOpintojenPreIBSuoritus2019(koulutusmoduuli: PreIBMuutSuorituksetTaiVastaavat2019, osasuoritukset: List[(PreIBLukionModuuliTaiPaikallinenOpintojakso2019, String)]) = MuidenLukioOpintojenPreIBSuoritus2019(
    koulutusmoduuli = koulutusmoduuli,
    osasuoritukset = Some(osasuoritukset.map {
      case (moduuli:PreIBLukionModuuliMuissaOpinnoissa2019, arvosana) =>
        PreIBLukionModuulinSuoritusMuissaOpinnoissa2019(
          koulutusmoduuli = moduuli,
          arviointi = Lukio2019ExampleData.sanallinenArviointi(arvosana)
        )
      case (moduuli:PreIBPaikallinenOpintojakso2019, arvosana) =>
        PreIBLukionPaikallisenOpintojaksonSuoritus2019(
          koulutusmoduuli = moduuli,
          arviointi = Lukio2019ExampleData.sanallinenArviointi(arvosana)
        )
      case _ =>
        throw new IllegalArgumentException
    })
  )

  def moduulinSuoritusOppiaineissa(moduuli: PreIBLukionModuuliOppiaineissa2019) = PreIBLukionModuulinSuoritusOppiaineissa2019(
    koulutusmoduuli = moduuli,
    suorituskieli = None
  )

  def moduulinSuoritusMuissaOpinnoissa(moduuli: PreIBLukionModuuliMuissaOpinnoissa2019) = PreIBLukionModuulinSuoritusMuissaOpinnoissa2019(
    koulutusmoduuli = moduuli,
    suorituskieli = None
  )

  def paikallisenOpintojaksonSuoritus(opintojakso: PreIBPaikallinenOpintojakso2019) = PreIBLukionPaikallisenOpintojaksonSuoritus2019(
    koulutusmoduuli = opintojakso,
    suorituskieli = None
  )

  def muidenLukioOpintojenSuoritus(): MuidenLukioOpintojenPreIBSuoritus2019 = muidenLukioOpintojenSuoritus(Lukio2019ExampleData.muutSuoritukset)

  def lukioDiplomienSuoritus(): MuidenLukioOpintojenPreIBSuoritus2019 = muidenLukioOpintojenSuoritus(Lukio2019ExampleData.lukiodiplomit)

  def temaattistenOpintojenSuoritus(): MuidenLukioOpintojenPreIBSuoritus2019 = muidenLukioOpintojenSuoritus(Lukio2019ExampleData.temaattisetOpinnot)

  private def muidenLukioOpintojenSuoritus(koulutusmoduuli: MuutSuorituksetTaiVastaavat2019): MuidenLukioOpintojenPreIBSuoritus2019 = MuidenLukioOpintojenPreIBSuoritus2019(
    koulutusmoduuli = koulutusmoduuli,
    osasuoritukset = None
  )

}
