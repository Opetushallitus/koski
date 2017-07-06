package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesAikuistenPerusopetus._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.schema._

object ExamplesAikuistenPerusopetuksenAlkuvaihe {
  val examples = List(
    Example("aikuisten perusopetuksen alkuvaihe", "Aikuisopiskelija on suorittanut aikuisten perusopetuksen oppimäärän alkuvaiheen opinnot opetussuunnitelman 2017 mukaisesti", aikuistenPerusopetuksenAlkuvaihe)
  )

  def aikuistenPerusopetuksenAlkuvaihe = {
    Oppija(
      exampleHenkilö,
      List(PerusopetuksenOpiskeluoikeus(
        alkamispäivä = Some(date(2008, 8, 15)),
        päättymispäivä = Some(date(2016, 6, 4)),
        oppilaitos = Some(jyväskylänNormaalikoulu),
        koulutustoimija = None,
        suoritukset = List(aikuistenPerusopetuksenAlkuvaiheenSuoritus),
        tila = PerusopetuksenOpiskeluoikeudenTila(
          List(
            PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
            PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
          )
        )
      ))
    )
  }

  def aikuistenPerusopetuksenAlkuvaiheenSuoritus = AikuistenPerusopetuksenAlkuvaiheenSuoritus(
    aikuistenPerusopetus2017,
    suorituskieli = suomenKieli,
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistusPaikkakunnalla(),
    suoritustapa = suoritustapaErityinenTutkinto,
    osasuoritukset = alkuvaiheenOppiaineet
  )

  def alkuvaiheenOppiaineet = Some(List(
    alkuvaiheenOppiaineenSuoritus(äidinkieli("AI1")).copy(arviointi = arviointi(9), osasuoritukset = Some(List(
      kurssinSuoritus2017("LÄI1"),
      kurssinSuoritus2017("LÄI2"),
      kurssinSuoritus2017("LÄI3"),
      kurssinSuoritus2017("LÄI4"),
      kurssinSuoritus2017("LÄI5"),
      kurssinSuoritus2017("LÄI6"),
      kurssinSuoritus2017("LÄI7"),
      kurssinSuoritus2017("LÄI8"),
      kurssinSuoritus2017("LÄI9")
    ))),
    alkuvaiheenOppiaineenSuoritus(kieli("A1", "EN")).copy(arviointi = arviointi(7), osasuoritukset = Some(List(
      kurssinSuoritus2017("AENA1"),
      kurssinSuoritus2017("AENA2"),
      kurssinSuoritus2017("AENA3"),
      kurssinSuoritus2017("AENA4")
    ))),
    alkuvaiheenOppiaineenSuoritus(oppiaine("MA")).copy(arviointi = arviointi(10), osasuoritukset = Some(List(
      kurssinSuoritus2017("LMA1"),
      kurssinSuoritus2017("LMA2"),
      kurssinSuoritus2017("LMA3")
    ))),
    // Yhteiskuntatietous ja kulttuurintuntemus
    alkuvaiheenOppiaineenSuoritus(oppiaine("YH")).copy(arviointi = arviointi(8), osasuoritukset = Some(List(
      kurssinSuoritus2017("LYK1"),
      kurssinSuoritus2017("LYK2")
    ))),
    // Ympäristö- ja luonnontieto
    alkuvaiheenOppiaineenSuoritus(oppiaine("YL")).copy(arviointi = arviointi(8), osasuoritukset = Some(List(
      kurssinSuoritus2017("LYL1")
    ))),
    // Terveystieto
    alkuvaiheenOppiaineenSuoritus(oppiaine("TE")).copy(arviointi = arviointi(10), osasuoritukset = Some(List(
      kurssinSuoritus2017("ATE1")
    ))),
    // Opinto-ohjaus
    alkuvaiheenOppiaineenSuoritus(oppiaine("OP")).copy(arviointi = arviointi("S"))
  ))

  def alkuvaiheenOppiaineenSuoritus(aine: PerusopetuksenOppiaine) = AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus(
    koulutusmoduuli = aine,
    suorituskieli = None,
    tila = tilaValmis,
    arviointi = None
  )

  def kurssinSuoritus2017(koodiarvo: String) = AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus(
    ValtakunnallinenAikuistenPerusopetuksenAlkuvaiheenKurssi2017(Koodistokoodiviite(koodiarvo, "aikuistenperusopetuksenalkuvaiheenkurssit2017")),
    tilaValmis,
    arviointi = arviointi(9)
  )
}
