package fi.oph.koski.documentation

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.ibis.IBO
import fi.oph.koski.schema._

import java.time.LocalDate
import java.time.LocalDate.{of => date}

// TODO TOR-747: Luonnos. Esimerkki vastaa IB-koulutuksen opiskeluoikeutta ibKoulutuksenOpiskeluoikeus (sama iboTunniste).
object ExamplesIBDiploma {
  val iboTunniste = "dip123"
  val ibKoulutuksenOpiskeluoikeus: IBOpiskeluoikeus = ExamplesIB.opiskeluoikeus.copy(iboTunniste = Some(iboTunniste))
  val vahvistuspäivä: LocalDate = date(2016, 7, 6)
  val examinationSession: IBExaminationSession = IBExaminationSession(year = 2016, month = 5)

  def oppiaineenArviointi(arvosana: String): Option[List[IBDiplomanOppiaineenArviointi]] =
    Some(List(IBDiplomanOppiaineenArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkoib"), Some(vahvistuspäivä))))

  def muuOppiaine(tunniste: String, taso: String, arvosana: String, additionalSubject: Boolean = false): IBDiplomanOppiaineenSuoritus =
    IBDiplomanOppiaineenSuoritus(
      koulutusmoduuli = IBDiplomanMuuOppiaine(Koodistokoodiviite(tunniste, "oppiaineetib"), Some(Koodistokoodiviite(taso, "oppiaineentasoib"))),
      examinationSession = examinationSession,
      arviointi = oppiaineenArviointi(arvosana),
      additionalSubject = additionalSubject,
    )

  def kieliOppiaine(tunniste: String, kieli: String, taso: String, arvosana: String): IBDiplomanOppiaineenSuoritus =
    IBDiplomanOppiaineenSuoritus(
      koulutusmoduuli = IBDiplomanKieliOppiaine(
        Koodistokoodiviite(tunniste, "oppiaineetib"),
        Some(Koodistokoodiviite(taso, "oppiaineentasoib")),
        Koodistokoodiviite(kieli, "kielivalikoima"),
      ),
      examinationSession = examinationSession,
      arviointi = oppiaineenArviointi(arvosana),
    )

  def coreArviointi(arvosana: String): Option[List[IBDiplomanCoreArviointi]] =
    Some(List(IBDiplomanCoreArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkocorerequirementsib"), Some(vahvistuspäivä))))

  def extendedEssay(aine: String, arvosana: String): IBDiplomanCoreSuoritus =
    IBDiplomanCoreSuoritus(
      koulutusmoduuli = IBDiplomanExtendedEssay(aine = Some(IBDiplomanMuuOppiaine(Koodistokoodiviite(aine, "oppiaineetib"), None))),
      examinationSession = examinationSession,
      arviointi = coreArviointi(arvosana),
    )

  def theoryOfKnowledge(arvosana: String): IBDiplomanCoreSuoritus =
    IBDiplomanCoreSuoritus(
      koulutusmoduuli = IBDiplomanTheoryOfKnowledge(),
      examinationSession = examinationSession,
      arviointi = coreArviointi(arvosana),
    )

  val suoritus: IBDiplomanSuoritus = IBDiplomanSuoritus(
    toimipiste = IBO.oppilaitos,
    vahvistus = Some(Päivämäärävahvistus(vahvistuspäivä, IBO.oppilaitos)),
    examinationSession = examinationSession,
    totalPoints = Some(37),
    combinedGrade = Some(2),
    result = Koodistokoodiviite("D", "ibdiplomaresult"),
    osasuoritukset = Some(List(
      kieliOppiaine("A", "FI", "HL", "7"),
      kieliOppiaine("B", "EN", "HL", "6"),
      muuOppiaine("HIS", "HL", "6"),
      muuOppiaine("BIO", "SL", "6"),
      muuOppiaine("MATAA", "SL", "5"),
      muuOppiaine("PHY", "SL", "5"),
      extendedEssay("HIS", "B"),
      theoryOfKnowledge("A"),
    )),
  )

  val opiskeluoikeus: IBDiplomaOpiskeluoikeus = IBDiplomaOpiskeluoikeus(
    oppilaitos = Some(IBO.oppilaitos),
    tila = IBDiplomaOpiskeluoikeudenTila(List(
      IBDiplomaOpiskeluoikeusjakso(vahvistuspäivä, ExampleData.opiskeluoikeusValmistunut)
    )),
    suoritukset = List(suoritus),
    iboTunniste = iboTunniste,
  )

  val examples: List[Example] = List(
    Example("ibdiploma", "IBO:n myöntämä IB Diploma, joka on tuotu IBIS-tulostiedostosta", Oppija(asUusiOppija(KoskiSpecificMockOppijat.ibDiploma), List(opiskeluoikeus)), 400)
  )
}
