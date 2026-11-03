package fi.oph.koski.documentation

import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.ressunLukio
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema._

import java.time.LocalDate
import java.time.LocalDate.{of => date}

// TODO TOR-747: Luonnos. Esimerkki vastaa ExamplesIB.opiskeluoikeus-oppijan IB-koulutusta (sama iboTunniste ja oppilaitos).
object ExamplesIBDiploma {
  val iboTunniste = "ABC123"
  val vahvistuspäivä: LocalDate = date(2016, 7, 6)
  val tutkintokerta: IBTutkintokerta = IBTutkintokerta("2016M", 2016, finnish("toukokuu"))

  def oppiaineenArviointi(arvosana: String): Option[List[IBDiplomanOppiaineenArviointi]] =
    Some(List(IBDiplomanOppiaineenArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkoib"), Some(vahvistuspäivä))))

  def muuOppiaine(tunniste: String, taso: String, arvosana: String, lisäaine: Boolean = false): IBDiplomanOppiaineenSuoritus =
    IBDiplomanOppiaineenSuoritus(
      koulutusmoduuli = IBDiplomanMuuOppiaine(Koodistokoodiviite(tunniste, "oppiaineetib"), Koodistokoodiviite(taso, "oppiaineentasoib")),
      tutkintokerta = tutkintokerta,
      arviointi = oppiaineenArviointi(arvosana),
      lisäaine = lisäaine,
    )

  def kieliOppiaine(tunniste: String, kieli: String, taso: String, arvosana: String): IBDiplomanOppiaineenSuoritus =
    IBDiplomanOppiaineenSuoritus(
      koulutusmoduuli = IBDiplomanKieliOppiaine(
        Koodistokoodiviite(tunniste, "oppiaineetib"),
        Koodistokoodiviite(taso, "oppiaineentasoib"),
        Koodistokoodiviite(kieli, "kielivalikoima"),
      ),
      tutkintokerta = tutkintokerta,
      arviointi = oppiaineenArviointi(arvosana),
    )

  def core(tunniste: String, aine: Option[String], arvosana: String): IBDiplomanCoreSuoritus =
    IBDiplomanCoreSuoritus(
      koulutusmoduuli = IBDiplomanCoreOppiaine(Koodistokoodiviite(tunniste, "oppiaineetib"), aine.map(Koodistokoodiviite(_, "oppiaineetib"))),
      tutkintokerta = tutkintokerta,
      arviointi = Some(List(IBDiplomanCoreArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkocorerequirementsib"), Some(vahvistuspäivä)))),
    )

  val suoritus: IBDiplomanSuoritus = IBDiplomanSuoritus(
    toimipiste = ressunLukio,
    vahvistus = Some(Päivämäärävahvistus(vahvistuspäivä, ressunLukio)),
    tutkintokerta = tutkintokerta,
    kokonaispistemäärä = Some(37),
    lisäpisteet = Some(2),
    tulos = Koodistokoodiviite("D", "ibdiplomantulos"),
    osasuoritukset = Some(List(
      kieliOppiaine("A", "FI", "HL", "7"),
      kieliOppiaine("B", "EN", "HL", "6"),
      muuOppiaine("HIS", "HL", "6"),
      muuOppiaine("BIO", "SL", "6"),
      muuOppiaine("MATAA", "SL", "5"),
      muuOppiaine("PHY", "SL", "5"),
      core("EE", Some("HIS"), "B"),
      core("TOK", None, "A"),
    )),
  )

  val opiskeluoikeus: IBDiplomaOpiskeluoikeus = IBDiplomaOpiskeluoikeus(
    oppilaitos = Some(ressunLukio),
    tila = IBDiplomaOpiskeluoikeudenTila(List(
      IBDiplomaOpiskeluoikeusjakso(vahvistuspäivä, ExampleData.opiskeluoikeusValmistunut)
    )),
    suoritukset = List(suoritus),
    iboTunniste = iboTunniste,
  )

  val examples: List[Example] = List(
    Example("ibdiploma", "IBO:n myöntämä IB Diploma, joka on tuotu IBIS-tulostiedostosta", Oppija(asUusiOppija(KoskiSpecificMockOppijat.ibDiploma), List(opiskeluoikeus)))
  )
}
