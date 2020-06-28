package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData.{opiskeluoikeusAktiivinen, lukionOppimäärä, nuortenOpetussuunnitelma, arviointi, numeerinenArviointi, pakollinen}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.MockOppijat.{asUusiOppija, uusiLukio}
import fi.oph.koski.schema._
import Lukio2019ExampleData._

object ExamplesLukio2019 {
  lazy val oppija = Oppija(asUusiOppija(uusiLukio), List(opiskeluoikeus))
  lazy val opiskeluoikeus: LukionOpiskeluoikeus =
    LukionOpiskeluoikeus(
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2021, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      ),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(
        LukionOppimääränSuoritus2019(
          koulutusmoduuli = lukionOppimäärä,
          oppimäärä = nuortenOpetussuunnitelma,
          suorituskieli = suomenKieli,
          toimipiste = jyväskylänNormaalikoulu,
          osasuoritukset = Some(List(
            oppiaineenSuoritus(äidinkieli("AI1")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
              moduulinSuoritus(moduuli("OÄI1")).copy(arviointi = numeerinenArviointi(8))
            )))
          ))
        )
      )
    )

  val examples = List(
    Example("lukio - ops 2019", "Uuden 2019 opetussuunnitelman mukainen oppija", oppija)
  )
}

object Lukio2019ExampleData {
  def oppiaineenSuoritus(aine: LukionOppiaine2019): LukionOppiaineenSuoritus2019 = LukionOppiaineenSuoritus2019(
    koulutusmoduuli = aine,
    suorituskieli = None,
    osasuoritukset = None
  )

  def äidinkieli(kieli: String) = LukionÄidinkieliJaKirjallisuus2019(
    kieli = Koodistokoodiviite(koodiarvo = kieli,
      koodistoUri = "oppiaineaidinkielijakirjallisuus"),
    laajuus = valinnainenLaajuus(1.0)
  )

  def moduulinSuoritus(moduuli: LukionModuuli2019) = LukionModuulinSuoritus2019(
    koulutusmoduuli = moduuli,
    suorituskieli = None
  )

  def moduuli(moduuli: String, kurssinTyyppi: Koodistokoodiviite = pakollinen) = LukionModuuli2019(
    tunniste = Koodistokoodiviite(koodistoUri = "moduulikoodistolops2021", koodiarvo = moduuli),
    laajuus = laajuus(1.0),
    pakollinen = true
  )

  def laajuus(arvo: Double) = LaajuusOpintopisteissä(arvo = arvo, yksikkö = laajuusOpintopisteissä)

  def valinnainenLaajuus(arvo: Double) = Option(laajuus(arvo))
}

