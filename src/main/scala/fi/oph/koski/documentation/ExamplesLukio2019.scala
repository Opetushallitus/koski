package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.LukioExampleData.{opiskeluoikeusAktiivinen, lukionOppimäärä, nuortenOpetussuunnitelma, arviointi, numeerinenArviointi, pakollinen}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.MockOppijat.{asUusiOppija, uusiLukio}
import fi.oph.koski.schema._
import Lukio2019ExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits.str2localized

object ExamplesLukio2019 {
  lazy val oppija = Oppija(asUusiOppija(uusiLukio), List(opiskeluoikeus))

  lazy val oppimääränSuoritus = LukionOppimääränSuoritus2019(
    koulutusmoduuli = lukionOppimäärä,
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    toimipiste = jyväskylänNormaalikoulu,
    osasuoritukset = Some(List(
      oppiaineenSuoritus(äidinkieli("AI1")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
        moduulinSuoritus(moduuli("OÄI1")).copy(arviointi = numeerinenArviointi(8)),
        moduulinSuoritus(moduuli("OÄI2")).copy(arviointi = numeerinenArviointi(8)),
        moduulinSuoritus(moduuli("OÄI3")).copy(arviointi = numeerinenArviointi(8))
      ))),
      oppiaineenSuoritus(pitkäMatematiikka).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
        moduulinSuoritus(moduuli("MAB2")).copy(arviointi = numeerinenArviointi(8)),
        moduulinSuoritus(moduuli("MAB3")).copy(arviointi = numeerinenArviointi(8)),
        moduulinSuoritus(moduuli("MAB4")).copy(arviointi = numeerinenArviointi(8))
      ))),
      oppiaineenSuoritus(uskonto(Some("MU"))).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
        moduulinSuoritus(moduuli("UE1").copy(laajuus = laajuus(1.5))).copy(arviointi = numeerinenArviointi(7))
      ))),
      oppiaineenSuoritus(muuOppiaine("FY")).copy(arviointi = arviointi("10")).copy(osasuoritukset = Some(List(
        moduulinSuoritus(moduuli("FY1")).copy(arviointi = numeerinenArviointi(10)),
        moduulinSuoritus(moduuli("FY2")).copy(arviointi = numeerinenArviointi(10)),
        moduulinSuoritus(moduuli("FY3")).copy(arviointi = numeerinenArviointi(10)),
        paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY123", "Keittiöfysiikka")).copy(arviointi = numeerinenArviointi(10))
      )))
    ))
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
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus")
  )

  lazy val pitkäMatematiikka = LukionMatematiikka2019(
    oppimäärä = Koodistokoodiviite(koodiarvo = "MAA", koodistoUri = "oppiainematematiikka")
  )

  def uskonto(oppimäärä: Option[String] = None) = LukionUskonto2019(
    tunniste = Koodistokoodiviite(koodiarvo = "KT", koodistoUri = "koskioppiaineetyleissivistava"),
    uskonnonOppimäärä = oppimäärä.map(opp => Koodistokoodiviite(koodiarvo = opp, koodistoUri = "uskonnonoppimaara"))
  )

  def muuOppiaine(tunniste: String) = LukionMuuValtakunnallinenOppiaine2019(
    tunniste = Koodistokoodiviite(koodiarvo = tunniste, koodistoUri = "koskioppiaineetyleissivistava")
  )

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

  def valinnainenLaajuus(arvo: Double) = Some(laajuus(arvo))
}

