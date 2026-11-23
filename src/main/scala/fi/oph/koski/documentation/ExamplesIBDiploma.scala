package fi.oph.koski.documentation

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.ibis.IBO
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

import java.time.LocalDate
import java.time.LocalDate.{of => date}

// TODO TOR-747: Luonnos. Esimerkki vastaa IB-koulutuksen opiskeluoikeutta ibKoulutuksenOpiskeluoikeus (sama iboTunniste).
object ExamplesIBDiploma {
  val iboTunniste = "dip123"
  val ibKoulutuksenOpiskeluoikeus: IBOpiskeluoikeus = ExamplesIB.opiskeluoikeus.copy(iboTunniste = Some(iboTunniste))
  val vahvistuspäivä: LocalDate = date(2016, 7, 6)
  val tutkintokerta: IBTutkintokerta = IBTutkintokerta("2016K", 2016, finnish("kevät"))
  val englanti: Option[Koodistokoodiviite] = Some(Koodistokoodiviite("EN", "kieli"))

  def oppiaineenArviointi(arvosana: String): Option[List[IBDiplomanOppiaineenArviointi]] =
    Some(List(IBDiplomanOppiaineenArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkoib"), Some(vahvistuspäivä))))

  def muuOppiaine(tunniste: String, taso: String, arvosana: String, additionalSubject: Boolean = false): IBDiplomanOppiaineenSuoritus =
    IBDiplomanOppiaineenSuoritus(
      koulutusmoduuli = IBDiplomanMuuOppiaine(Koodistokoodiviite(tunniste, "oppiaineetib"), Some(Koodistokoodiviite(taso, "oppiaineentasoib"))),
      tutkintokerta = tutkintokerta,
      arviointi = oppiaineenArviointi(arvosana),
      suorituskieli = englanti,
      additionalSubject = additionalSubject,
    )

  def kieliOppiaine(tunniste: String, kieli: String, taso: String, arvosana: String): IBDiplomanOppiaineenSuoritus =
    IBDiplomanOppiaineenSuoritus(
      koulutusmoduuli = IBDiplomanKieliOppiaine(
        Koodistokoodiviite(tunniste, "oppiaineetib"),
        Some(Koodistokoodiviite(taso, "oppiaineentasoib")),
        Koodistokoodiviite(kieli, "kielivalikoima"),
      ),
      tutkintokerta = tutkintokerta,
      arviointi = oppiaineenArviointi(arvosana),
      suorituskieli = Some(Koodistokoodiviite(kieli, "kieli")),
    )

  def coreArviointi(arvosana: String): Option[List[IBDiplomanCoreArviointi]] =
    Some(List(IBDiplomanCoreArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkocorerequirementsib"), Some(vahvistuspäivä))))

  def extendedEssay(aine: String, arvosana: String): IBDiplomanCoreSuoritus =
    IBDiplomanCoreSuoritus(
      koulutusmoduuli = IBDiplomanExtendedEssay(aine = Some(IBDiplomanMuuOppiaine(Koodistokoodiviite(aine, "oppiaineetib"), None))),
      tutkintokerta = tutkintokerta,
      arviointi = coreArviointi(arvosana),
      suorituskieli = englanti,
    )

  def theoryOfKnowledge(arvosana: String): IBDiplomanCoreSuoritus =
    IBDiplomanCoreSuoritus(
      koulutusmoduuli = IBDiplomanTheoryOfKnowledge(),
      tutkintokerta = tutkintokerta,
      arviointi = coreArviointi(arvosana),
      suorituskieli = englanti,
    )

  val suoritus: IBDiplomanSuoritus = IBDiplomanSuoritus(
    toimipiste = IBO.oppilaitos,
    vahvistus = Some(Päivämäärävahvistus(vahvistuspäivä, IBO.oppilaitos)),
    tutkintokerta = tutkintokerta,
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
