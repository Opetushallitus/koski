package fi.oph.koski.documentation

import fi.oph.koski.documentation.ExampleData.tilaValmis
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.schema.{AikuistenPerusopetuksenOppiaineenSuoritus, PerusopetuksenOppiaine}

object AikuistenPerusopetusExampleData {
  def aikuistenSuoritus(aine: PerusopetuksenOppiaine) = AikuistenPerusopetuksenOppiaineenSuoritus(
    koulutusmoduuli = aine,
    suorituskieli = None,
    tila = tilaValmis,
    arviointi = None
  )

  val aikuistenAineet = Some(
    List(
      aikuistenSuoritus(äidinkieli("AI1")).copy(arviointi = arviointi(9)),
      aikuistenSuoritus(kieli("B1", "SV")).copy(arviointi = arviointi(8)),
      aikuistenSuoritus(kieli("B1", "SV").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = hyväksytty),
      aikuistenSuoritus(kieli("A1", "EN")).copy(arviointi = arviointi(8)),
      aikuistenSuoritus(oppiaine("KT")).copy(arviointi = arviointi(10)),
      aikuistenSuoritus(oppiaine("HI")).copy(arviointi = arviointi(8)),
      aikuistenSuoritus(oppiaine("YH")).copy(arviointi = arviointi(10)),
      aikuistenSuoritus(oppiaine("MA")).copy(arviointi = arviointi(9)),
      aikuistenSuoritus(oppiaine("KE")).copy(arviointi = arviointi(7)),
      aikuistenSuoritus(oppiaine("FY")).copy(arviointi = arviointi(9)),
      aikuistenSuoritus(oppiaine("BI")).copy(arviointi = arviointi(9), yksilöllistettyOppimäärä = true),
      aikuistenSuoritus(oppiaine("GE")).copy(arviointi = arviointi(9)),
      aikuistenSuoritus(oppiaine("MU")).copy(arviointi = arviointi(7)),
      aikuistenSuoritus(oppiaine("KU")).copy(arviointi = arviointi(8)),
      aikuistenSuoritus(oppiaine("KO")).copy(arviointi = arviointi(8)),
      aikuistenSuoritus(oppiaine("KO").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = hyväksytty),
      aikuistenSuoritus(oppiaine("TE")).copy(arviointi = arviointi(8)),
      aikuistenSuoritus(oppiaine("KS")).copy(arviointi = arviointi(9)),
      aikuistenSuoritus(oppiaine("LI")).copy(arviointi = arviointi(9), painotettuOpetus = true),
      aikuistenSuoritus(oppiaine("LI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(0.5))).copy(arviointi = hyväksytty),
      aikuistenSuoritus(kieli("B2", "DE").copy(pakollinen = false, laajuus = vuosiviikkotuntia(4))).copy(arviointi = arviointi(9)),
      aikuistenSuoritus(valinnainenOppiaine("TH", "Tietokoneen hyötykäyttö", "Kurssilla tarjotaan yksityiskohtaisempaa tietokoneen, oheislaitteiden sekä käyttöjärjestelmän ja ohjelmien tuntemusta.")).copy(arviointi = arviointi(9))
    ))
}
