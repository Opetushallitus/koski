package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.Lukio2019ExampleData._
import fi.oph.koski.documentation.LukioExampleData.{laajuus => _, _}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.MockOppijat.{asUusiOppija, uusiLukio, uusiLukionAineopiskelija}
import fi.oph.koski.localization.LocalizedStringImplicits.str2localized
import fi.oph.koski.schema._

object ExamplesLukio2019 {
  val lops2019perusteenDiaarinumero = Some("OPH-2263-2019")
  val lukionOppimäärä2019: LukionOppimäärä = LukionOppimäärä(perusteenDiaarinumero = lops2019perusteenDiaarinumero)
  val oppiainesuoritukset = List(
    oppiaineenSuoritus(LukioExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("OÄI1")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritus(moduuli("OÄI2")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritus(moduuli("OÄI3")).copy(arviointi = numeerinenArviointi(8))
    ))),
    oppiaineenSuoritus(LukioExampleData.matematiikka("MAA", None)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("MAB2")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritus(moduuli("MAB3")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritus(moduuli("MAB4")).copy(arviointi = numeerinenArviointi(8))
    ))),
    oppiaineenSuoritus(LukioExampleData.lukionUskonto(Some("MU"), None)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("UE1").copy(laajuus = laajuus(1.5))).copy(arviointi = numeerinenArviointi(7))
    ))),
    oppiaineenSuoritus(lukionKieli2019("AOM", "SV")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("RUA4").copy(laajuus = laajuus(1))).copy(arviointi = numeerinenArviointi(7))
    ))),
    oppiaineenSuoritus(LukioExampleData.lukionOppiaine("FY", None)).copy(arviointi = arviointi("10")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("FY1")).copy(arviointi = numeerinenArviointi(10)),
      moduulinSuoritus(moduuli("FY2")).copy(arviointi = numeerinenArviointi(10)),
      moduulinSuoritus(moduuli("FY3")).copy(arviointi = numeerinenArviointi(10)),
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY123", "Keittiöfysiikka")).copy(arviointi = numeerinenArviointi(10))
    )))
  )

  lazy val oppimääränSuoritus = LukionOppimääränSuoritus2019(
    koulutusmoduuli = lukionOppimäärä2019,
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    toimipiste = jyväskylänNormaalikoulu,
    osasuoritukset = Some(oppiainesuoritukset)
  )

  lazy val oppiaineidenOppimäärienSuoritus = LukionOppiaineidenOppimäärienSuoritus2019(
    koulutusmoduuli = LukionOppiaineidenOppimäärät2019(perusteenDiaarinumero = lops2019perusteenDiaarinumero),
    suorituskieli = suomenKieli,
    toimipiste = jyväskylänNormaalikoulu,
    osasuoritukset = Some(oppiainesuoritukset)
  )

  lazy val opiskeluoikeus: LukionOpiskeluoikeus =
    LukionOpiskeluoikeus(
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2021, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      ),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(oppimääränSuoritus)
    )

  lazy val oppiaineenOppimääräOpiskeluoikeus: LukionOpiskeluoikeus = opiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus))

  lazy val oppija = Oppija(asUusiOppija(uusiLukio), List(opiskeluoikeus))
  lazy val oppiaineidenOppimäärienOppija = Oppija(asUusiOppija(uusiLukionAineopiskelija), List(oppiaineenOppimääräOpiskeluoikeus))

  val examples = List(
    Example("lukio ops 2019 - oppimäärä", "Uuden 2019 opetussuunnitelman mukainen oppija, lukion oppimäärä", oppija),
    Example("lukio ops 2019 - oppiaineiden oppimäärä", "Uuden 2019 opetussuunnitelman mukainen oppija, lukion oppiaineiden oppimäärä", oppiaineidenOppimäärienOppija)
  )
}

object Lukio2019ExampleData {
  def oppiaineenSuoritus(aine: LukionOppiaine2019): LukionOppiaineenSuoritus2019 = LukionOppiaineenSuoritus2019(
    koulutusmoduuli = aine,
    suorituskieli = None,
    osasuoritukset = None
  )

  def lukionKieli2019(oppiaine: String, kieli: String) = LaajuudetonVierasTaiToinenKotimainenKieli2019(
    tunniste = Koodistokoodiviite(koodiarvo = oppiaine, koodistoUri = "koskioppiaineetyleissivistava"),
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima"))

  def moduulinSuoritus(moduuli: LukionModuuli2019) = LukionModuulinSuoritus2019(
    koulutusmoduuli = moduuli,
    suorituskieli = None
  )

  def paikallisenOpintojaksonSuoritus(opintojakso: LukionPaikallinenOpintojakso2019) = LukionPaikallisenOpintojaksonSuoritus2019(
    koulutusmoduuli = opintojakso,
    suorituskieli = None
  )

  def moduuli(moduuli: String, kurssinTyyppi: Koodistokoodiviite = pakollinen) = LukionModuuli2019(
    tunniste = Koodistokoodiviite(koodistoUri = "moduulikoodistolops2021", koodiarvo = moduuli),
    laajuus = laajuus(1),
    pakollinen = true
  )

  def paikallinenOpintojakso(koodi: String, kuvaus: String) = LukionPaikallinenOpintojakso2019(
    tunniste = PaikallinenKoodi(koodi, koodi),
    laajuus = laajuus(1),
    kuvaus = kuvaus,
    pakollinen = true
  )

  def laajuus(arvo: Double) = LaajuusOpintopisteissä(arvo = arvo, yksikkö = laajuusOpintopisteissä)
}

