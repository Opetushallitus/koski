package fi.oph.koski.documentation

import java.time.LocalDate

import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._

object InternationalSchoolExampleData {
  lazy val internationalSchoolOfHelsinki: Oppilaitos = Oppilaitos(MockOrganisaatiot.internationalSchool, Some(Koodistokoodiviite("03510", None, "oppilaitosnumero", None)), Some("International School of Helsinki"))
  lazy val internationalSchoolOfHelsinkiToimipiste: Toimipiste = Toimipiste("1.2.246.562.10.63709849283")

  def pypSuoritus(grade: String, aloitusPäivä: LocalDate, vahvistusPäivä: Option[LocalDate]): PYPVuosiluokanSuoritus = PYPVuosiluokanSuoritus(
    koulutusmoduuli = PYPLuokkaAste(tunniste = Koodistokoodiviite(grade, "internationalschoolluokkaaste")),
    luokka = Some(s"${grade}A"),
    alkamispäivä = Some(aloitusPäivä),
    toimipiste = internationalSchoolOfHelsinki,
    vahvistus = vahvistusPäivä.flatMap(vahvistus),
    suorituskieli = ExampleData.englanti
  )

  def mypSuoritus(grade: Int, aloitusPäivä: LocalDate, vahvistusPäivä: Option[LocalDate]): MYPVuosiluokanSuoritus = MYPVuosiluokanSuoritus(
    koulutusmoduuli = MYPLuokkaAste(tunniste = Koodistokoodiviite(grade.toString, "internationalschoolluokkaaste")),
    luokka = Some(s"${grade.toString}B"),
    alkamispäivä = Some(aloitusPäivä),
    toimipiste = internationalSchoolOfHelsinki,
    vahvistus = vahvistusPäivä.flatMap(vahvistus),
    suorituskieli = ExampleData.englanti
  )

  def diplomaSuoritus(grade: Int, aloitusPäivä: LocalDate, vahvistusPäivä: Option[LocalDate]): DiplomaVuosiluokanSuoritus = DiplomaVuosiluokanSuoritus(
    koulutusmoduuli = IBDiplomaLuokkaAste(tunniste = Koodistokoodiviite(grade.toString, "internationalschoolluokkaaste")),
    luokka = Some(s"${grade.toString}C"),
    alkamispäivä = Some(aloitusPäivä),
    toimipiste = internationalSchoolOfHelsinki,
    vahvistus = vahvistusPäivä.flatMap(vahvistus),
    suorituskieli = ExampleData.englanti
  )

  def mypOppiaineenSuoritus(oppiaine: MYPOppiaine, arviointi: Option[MYPArviointi] = None): MYPOppiaineenSuoritus = MYPOppiaineenSuoritus(
    koulutusmoduuli = oppiaine,
    arviointi = arviointi.map(a => List(a))
  )

  def pypOppiaineenSuoritus(oppiaine: PYPOppiaine, arviointi: Option[SanallinenInternationalSchoolOppiaineenArviointi] = None): PYPOppiaineenSuoritus = PYPOppiaineenSuoritus(
    koulutusmoduuli = oppiaine,
    arviointi = arviointi.map(a => List(a))
  )

  def diplomaOppiaineenSuoritus(oppiaine: InternationalSchoolIBOppiaine , arviointi: Option[DiplomaArviointi] = None): DiplomaOppiaineenSuoritus = DiplomaOppiaineenSuoritus(
    koulutusmoduuli = oppiaine,
    arviointi = arviointi.map(a => List(a))
  )

  def diplomaTOKSuoritus(arviointi: Option[InternationalSchoolCoreRequirementsArviointi] = None): DiplomaCoreRequirementsOppiaineenSuoritus = DiplomaCoreRequirementsOppiaineenSuoritus(
    koulutusmoduuli = DiplomaCoreRequirementsOppiaine(tunniste = Koodistokoodiviite("TOK", "oppiaineetib")),
    arviointi = arviointi.map(a => List(a))
  )

  def mypOppiaine(tunniste: String): MYPOppiaine = MYPOppiaineMuu(
    tunniste = Koodistokoodiviite(tunniste, "oppiaineetinternationalschool")
  )

  def pypOppiaine(tunniste: String): PYPOppiaine = PYPOppiaineMuu(
    tunniste = Koodistokoodiviite(tunniste, "oppiaineetinternationalschool")
  )

  def languageAndLiterature(kieli: String): InternationalSchoolKieliOppiaine = LanguageAndLiterature(
    tunniste = Koodistokoodiviite("LL", "oppiaineetinternationalschool"),
    kieli = Koodistokoodiviite(kieli, "kielivalikoima")
  )

  def languageAqcuisition(kieli: String): InternationalSchoolKieliOppiaine = LanguageAcquisition(
    tunniste = Koodistokoodiviite("LAC", "oppiaineetinternationalschool"),
    kieli = Koodistokoodiviite(kieli, "kielivalikoima")
  )

  def diplomaIBOppiaine(tunniste: String, taso: Option[String] = None) = MuuDiplomaOppiaine(
    tunniste = Koodistokoodiviite(tunniste, "oppiaineetib"),
    taso = taso.map(Koodistokoodiviite(_, "oppiaineentasoib"))
  )

  def diplomaInternationalSchoolOppiaine(tunniste: String, taso: Option[String] = None) = InternationalSchoolMuuDiplomaOppiaine(
    tunniste = Koodistokoodiviite(tunniste, "oppiaineetinternationalschool"),
    taso = taso.map(Koodistokoodiviite(_, "oppiaineentasoib"))
  )

  def diplomaKieliOppiaine(tunniste: String, kieli: String, taso: Option[String] = None) = KieliDiplomaOppiaine(
    tunniste = Koodistokoodiviite(tunniste, "oppiaineetib"),
    kieli = Koodistokoodiviite(kieli, "kielivalikoima"),
    taso = taso.map(Koodistokoodiviite(_, "oppiaineentasoib"))
  )

  def diplomaArviointi(arvosana: Int): Option[InternationalSchoolIBOppiaineenArviointi] = Some(InternationalSchoolIBOppiaineenArviointi(arvosana = Koodistokoodiviite(arvosana.toString, "arviointiasteikkoib")))
  def diplomaArviointi(arvosana: String): Option[InternationalSchoolIBOppiaineenArviointi] = Some(InternationalSchoolIBOppiaineenArviointi(arvosana = Koodistokoodiviite(arvosana, "arviointiasteikkoib")))
  def arviointi(arvosana: Int): Option[NumeerinenInternationalSchoolOppiaineenArviointi] = Some(NumeerinenInternationalSchoolOppiaineenArviointi(Koodistokoodiviite(arvosana.toString, "arviointiasteikkoib")))
  def arviointi(arvosana: String) = Some(SanallinenInternationalSchoolOppiaineenArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkointernationalschool")))
  def tokArvionti(arvosana: String) = Some(InternationalSchoolCoreRequirementsArviointi(arvosana = Koodistokoodiviite(arvosana, "arviointiasteikkocorerequirementsib")))

  def vahvistus(päivä: LocalDate) = ExampleData.vahvistusPaikkakunnalla(päivä, internationalSchoolOfHelsinki, ExampleData.helsinki)
}
