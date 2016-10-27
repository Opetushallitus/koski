package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._

object PerusopetusExampleData {
  def arviointi(arvosana: Int): Some[List[PerusopetuksenOppiaineenArviointi]] = Some(List(PerusopetuksenOppiaineenArviointi(arvosana)))
  def arviointi(arvosana: String, kuvaus: Option[LocalizedString] = None): Some[List[PerusopetuksenOppiaineenArviointi]] = Some(List(PerusopetuksenOppiaineenArviointi(arvosana, kuvaus)))

  val hyväksytty = Some(List(PerusopetuksenOppiaineenArviointi("S")))

  def suoritus(aine: PerusopetuksenOppiaine) = PerusopetuksenOppiaineenSuoritus(
    koulutusmoduuli = aine,
    suorituskieli = None,
    tila = tilaValmis,
    arviointi = None
  )

  def vuosiviikkotuntia(määrä: Double): Some[LaajuusVuosiviikkotunneissa] = Some(LaajuusVuosiviikkotunneissa(määrä.toFloat))

  val exampleHenkilö = MockOppijat.koululainen.vainHenkilötiedot

  val perusopetus = Perusopetus(Some("104/011/2014"))
  val suoritustapaKoulutus = Koodistokoodiviite("koulutus", "perusopetuksensuoritustapa")
  val suoritustapaErityinenTutkinto = Koodistokoodiviite("erityinentutkinto", "perusopetuksensuoritustapa")
  val perusopetuksenOppimäärä = Koodistokoodiviite("perusopetus", "perusopetuksenoppimaara")
  val aikuistenOppimäärä = Koodistokoodiviite("aikuistenperusopetus", "perusopetuksenoppimaara")

  def valinnainenOppiaine(aine: String, nimi: String, kuvaus: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) =
    PerusopetuksenPaikallinenValinnainenOppiaine(tunniste = PaikallinenKoodi(koodiarvo = aine, nimi = nimi), laajuus = laajuus, kuvaus = kuvaus)
  def oppiaine(aine: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = MuuPeruskoulunOppiaine(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = aine), laajuus = laajuus)
  def äidinkieli(kieli: String) = PeruskoulunAidinkieliJaKirjallisuus(kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus"))
  def kieli(oppiaine: String, kieli: String) = PeruskoulunVierasTaiToinenKotimainenKieli(
    tunniste = Koodistokoodiviite(koodiarvo = oppiaine, koodistoUri = "koskioppiaineetyleissivistava"),
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima"))

  val kaikkiAineet = Some(
    List(
      suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(9)),
      suoritus(kieli("B1", "SV")).copy(arviointi = arviointi(8)),
      suoritus(kieli("B1", "SV").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = hyväksytty),
      suoritus(kieli("A1", "EN")).copy(arviointi = arviointi(8)),
      suoritus(oppiaine("KT")).copy(arviointi = arviointi(10)),
      suoritus(oppiaine("HI")).copy(arviointi = arviointi(8)),
      suoritus(oppiaine("YH")).copy(arviointi = arviointi(10)),
      suoritus(oppiaine("MA")).copy(arviointi = arviointi(9)),
      suoritus(oppiaine("KE")).copy(arviointi = arviointi(7)),
      suoritus(oppiaine("FY")).copy(arviointi = arviointi(9)),
      suoritus(oppiaine("BI")).copy(arviointi = arviointi(9), yksilöllistettyOppimäärä = true),
      suoritus(oppiaine("GE")).copy(arviointi = arviointi(9)),
      suoritus(oppiaine("MU")).copy(arviointi = arviointi(7)),
      suoritus(oppiaine("KU")).copy(arviointi = arviointi(8)),
      suoritus(oppiaine("KO")).copy(arviointi = arviointi(8)),
      suoritus(oppiaine("KO").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = hyväksytty),
      suoritus(oppiaine("TE")).copy(arviointi = arviointi(8)),
      suoritus(oppiaine("KS")).copy(arviointi = arviointi(9)),
      suoritus(oppiaine("LI")).copy(arviointi = arviointi(9), painotettuOpetus = true),
      suoritus(oppiaine("LI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(0.5))).copy(arviointi = hyväksytty),
      suoritus(kieli("B2", "DE").copy(pakollinen = false, laajuus = vuosiviikkotuntia(4))).copy(arviointi = arviointi(9)),
      suoritus(valinnainenOppiaine("TH", "Tietokoneen hyötykäyttö", "Kurssilla tarjotaan yksityiskohtaisempaa tietokoneen, oheislaitteiden sekä käyttöjärjestelmän ja ohjelmien tuntemusta.")).copy(arviointi = arviointi(9))
    ))

  def oppija(henkilö: Henkilö = exampleHenkilö, opiskeluoikeus: Opiskeluoikeus): Oppija = Oppija(henkilö, List(opiskeluoikeus))

  def opiskeluoikeus(oppilaitos: Oppilaitos = jyväskylänNormaalikoulu, suoritukset: List[PerusopetuksenPäätasonSuoritus]): PerusopetuksenOpiskeluoikeus = {
    PerusopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = oppilaitos,
      koulutustoimija = None,
      suoritukset = suoritukset,
      tila = PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )
      )
    )
  }

  def päättötodistusOpiskeluoikeus(oppilaitos: Oppilaitos = jyväskylänNormaalikoulu) = opiskeluoikeus(
    suoritukset = List(
      kahdeksannenLuokanSuoritus.copy(toimipiste = oppilaitos),
      yhdeksännenLuokanSuoritus.copy(toimipiste = oppilaitos),
      perusopetuksenOppimääränSuoritus.copy(toimipiste = oppilaitos))
  )

  val kahdeksannenLuokanSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(8), luokka = "8C", alkamispäivä = Some(date(2014, 8, 15)),
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    osasuoritukset = kaikkiAineet,
    vahvistus = vahvistus(date(2015, 5, 30))
  )

  val yhdeksännenLuokanSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(9), luokka = "9C", alkamispäivä = Some(date(2015, 8, 15)),
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    vahvistus = vahvistus(date(2016, 5, 30))
  )

  val perusopetuksenOppimääränSuoritus = PerusopetuksenOppimääränSuoritus(
    koulutusmoduuli = perusopetus,
    tila = tilaValmis,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistus(date(2016, 6, 4)),
    suoritustapa = suoritustapaKoulutus,
    oppimäärä = perusopetuksenOppimäärä,
    osasuoritukset = kaikkiAineet
  )
}
