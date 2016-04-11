package fi.oph.tor.documentation

import java.time.LocalDate.{of => date}

import fi.oph.tor.documentation.ExampleData._
import fi.oph.tor.documentation.PeruskoulutusExampleData._
import fi.oph.tor.organisaatio.MockOrganisaatiot
import fi.oph.tor.schema._

object PeruskoulutusExampleData {
  lazy val jyväskylänNormaalikoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.jyväskylänNormaalikoulu, Some(Koodistokoodiviite("00204", None, "oppilaitosnumero", None)), Some("Jyväskylän normaalikoulu"))

  def suoritus(aine: YleissivistavaOppiaine) = PeruskoulunOppiaineenSuoritus(
    koulutusmoduuli = aine,
    paikallinenId = None,
    suorituskieli = None,
    tila = tilaValmis,
    arviointi = None,
    vahvistus = None
  )

  def oppiaine(aine: String) = Oppiaine(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = aine))
  def äidinkieli(kieli: String) = AidinkieliJaKirjallisuus(kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus"))
  def kieli(oppiaine: String, kieli: String) = VierasTaiToinenKotimainenKieli(
    tunniste = Koodistokoodiviite(koodiarvo = oppiaine, koodistoUri = "koskioppiaineetyleissivistava"),
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima"))
  def uskonto(uskonto: String) = Uskonto(uskonto = Koodistokoodiviite(koodiarvo = uskonto, koodistoUri = "oppiaineuskonto"))

  def arviointi(arvosana: Int): Some[List[YleissivistävänkoulutuksenArviointi]] = {
    Some(List(YleissivistävänkoulutuksenArviointi(arvosana.toString)))
  }

  val hyväksytty = Some(List(YleissivistävänkoulutuksenArviointi("S")))

  def vuosiviikkotuntia(määrä: Double): Some[Laajuus] = Some(Laajuus(määrä.toFloat, Koodistokoodiviite("3", Some("Vuosiviikkotuntia"), "opintojenlaajuusyksikko")))
}

object ExamplesPeruskoulutus {
  val uusi = TorOppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2016, 9, 1)),
      arvioituPäättymispäivä = Some(date(2020, 5, 1)),
      päättymispäivä = None,
      oppilaitos = jyväskylänNormaalikoulu,
      suoritukset = Nil,
      opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(
        List(
          Opiskeluoikeusjakso(date(2012, 9, 1), Some(date(2016, 1, 9)), opiskeluoikeusAktiivinen, None),
          Opiskeluoikeusjakso(date(2016, 1, 10), None, opiskeluoikeusPäättynyt, None)
        )
      )),
      läsnäolotiedot = None
    ))
  )
  private val vahvistus: Some[Vahvistus] = Some(Vahvistus(Some(date(2016, 6, 4))))

  val päättötodistus = TorOppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      id = None,
      versionumero = None,
      lähdejärjestelmänId = None,
      alkamispäivä = Some(date(2007, 8, 15)),
      arvioituPäättymispäivä = Some(date(2016, 6, 4)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = jyväskylänNormaalikoulu,
      suoritukset = List(
        PeruskoulunPäättötodistus(
          koulutusmoduuli = Peruskoulutus(),
          paikallinenId = None,
          suorituskieli = None,
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistus,
          osasuoritukset = Some(
            List(
              suoritus(äidinkieli("AI1")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(kieli("B1", "SV")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(kieli("B1", "SV").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(vahvistus = vahvistus).copy(arviointi = hyväksytty),
              suoritus(kieli("A1", "EN")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(uskonto("KT1")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(10)),
              suoritus(oppiaine("HI")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(oppiaine("YH")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(10)),
              suoritus(oppiaine("MA")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("KE")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(7)),
              suoritus(oppiaine("FY")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("BI")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("GE")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("MU")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(7)),
              suoritus(oppiaine("KU")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(oppiaine("KO")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(oppiaine("KO").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(vahvistus = vahvistus).copy(arviointi = hyväksytty),
              suoritus(oppiaine("TE")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(8)),
              suoritus(oppiaine("KS")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("LI")).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9)),
              suoritus(oppiaine("LI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(0.5))).copy(vahvistus = vahvistus).copy(arviointi = hyväksytty),
              suoritus(kieli("B2", "DE").copy(pakollinen = false, laajuus = vuosiviikkotuntia(4))).copy(vahvistus = vahvistus).copy(arviointi = arviointi(9))
            ))
        )),
      opiskeluoikeudenTila = Some(OpiskeluoikeudenTila(
        List(
          Opiskeluoikeusjakso(date(2007, 8, 15), Some(date(2016, 6, 3)), opiskeluoikeusAktiivinen, None),
          Opiskeluoikeusjakso(date(2016, 6, 4), None, opiskeluoikeusPäättynyt, None)
        )
      )),
      läsnäolotiedot = None
    ))
  )

  val examples = List(
    Example("peruskoulutus - uusi", "Uusi oppija lisätään suorittamaan peruskoulua", uusi),
    Example("peruskoulutus - päättötodistus", "Oppija on saanut peruskoulun päättötodistuksen", päättötodistus)
  )
}
