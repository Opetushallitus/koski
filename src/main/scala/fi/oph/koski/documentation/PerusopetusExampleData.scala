package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{jyväskylänNormaalikoulu, kulosaarenAlaAste}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object PerusopetusExampleData {
  def arviointi(arvosana: Int): Some[List[PerusopetuksenOppiaineenArviointi]] = Some(List(PerusopetuksenOppiaineenArviointi(arvosana)))
  def arviointi(arvosana: String, kuvaus: Option[LocalizedString] = None): Some[List[PerusopetuksenOppiaineenArviointi]] = Some(List(PerusopetuksenOppiaineenArviointi(arvosana, kuvaus)))

  val hyväksytty = Some(List(PerusopetuksenOppiaineenArviointi("S")))

  def suoritus(aine: PerusopetuksenOppiaine) = NuortenPerusopetuksenOppiaineenSuoritus(
    koulutusmoduuli = aine,
    suorituskieli = None,
    arviointi = None
  )

  def vuosiviikkotuntia(määrä: Double): Some[LaajuusVuosiviikkotunneissa] = Some(LaajuusVuosiviikkotunneissa(määrä.toFloat))

  val exampleHenkilö = MockOppijat.koululainen.henkilö

  val perusopetuksenDiaarinumero = "104/011/2014"
  val perusopetus = NuortenPerusopetus(Some(perusopetuksenDiaarinumero))
  val suoritustapaKoulutus = Koodistokoodiviite("koulutus", "perusopetuksensuoritustapa")
  val suoritustapaErityinenTutkinto = Koodistokoodiviite("erityinentutkinto", "perusopetuksensuoritustapa")
  val perusopetuksenOppimäärä = Koodistokoodiviite("perusopetus", "perusopetuksenoppimaara")

  def valinnainenOppiaine(aine: String, nimi: String, kuvaus: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) =
    PerusopetuksenPaikallinenOppiaine(tunniste = PaikallinenKoodi(koodiarvo = aine, nimi = nimi), laajuus = laajuus, kuvaus = kuvaus)
  def oppiaine(aine: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = MuuPeruskoulunOppiaine(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = aine), laajuus = laajuus)
  def äidinkieli(kieli: String, diaarinumero: Option[String] = None) = PeruskoulunÄidinkieliJaKirjallisuus(
    perusteenDiaarinumero = diaarinumero,
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus")
  )
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

  def opiskeluoikeus(oppilaitos: Oppilaitos = jyväskylänNormaalikoulu, suoritukset: List[PerusopetuksenPäätasonSuoritus], alkamispäivä: LocalDate = date(2008, 8, 15), päättymispäivä: Option[LocalDate] = Some(date(2016, 6, 4))): PerusopetuksenOpiskeluoikeus = {
    PerusopetuksenOpiskeluoikeus(
      päättymispäivä = päättymispäivä,
      oppilaitos = Some(oppilaitos),
      koulutustoimija = None,
      suoritukset = suoritukset,
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
        List(NuortenPerusopetuksenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä)) ++ päättymispäivä.toList.map (päivä => NuortenPerusopetuksenOpiskeluoikeusjakso(päivä, opiskeluoikeusValmistunut))
      )
    )
  }

  def päättötodistusOpiskeluoikeus(oppilaitos: Oppilaitos = jyväskylänNormaalikoulu, toimipiste: OrganisaatioWithOid = jyväskylänNormaalikoulu,  luokka: String = "C") = opiskeluoikeus(
    oppilaitos = oppilaitos,
    suoritukset = List(
      seitsemännenLuokanTuplaus.copy(toimipiste = toimipiste, luokka = "7" + luokka),
      kahdeksannenLuokanSuoritus.copy(toimipiste = toimipiste, luokka = "8" + luokka),
      yhdeksännenLuokanSuoritus.copy(toimipiste = toimipiste, luokka = "9" + luokka),
      perusopetuksenOppimääränSuoritus.copy(toimipiste = toimipiste))
  )

  val kahdeksannenLuokanSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(8, perusopetuksenDiaarinumero), luokka = "8C", alkamispäivä = Some(date(2014, 8, 15)),
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    muutSuorituskielet = Some(List(sloveeni)),
    kielikylpykieli = Some(ruotsinKieli),
    osasuoritukset = kaikkiAineet,
    käyttäytymisenArvio = Some(PerusopetuksenKäyttäytymisenArviointi(kuvaus = Some("Esimerkillistä käyttäytymistä koko vuoden ajan"))),
    vahvistus = vahvistusPaikkakunnalla(date(2015, 5, 30))
  )

  val seitsemännenLuokanTuplaus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(7, perusopetuksenDiaarinumero), luokka = "7C", alkamispäivä = Some(date(2013, 8, 15)),
    jääLuokalle = true,
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    osasuoritukset = kaikkiAineet.map(_.map(_.copy(arviointi = arviointi(4), yksilöllistettyOppimäärä = false)).filter(_.koulutusmoduuli.pakollinen)),
    vahvistus = vahvistusPaikkakunnalla(date(2014, 5, 30))
  )

  val kuudennenLuokanSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(6, perusopetuksenDiaarinumero), luokka = "6A", alkamispäivä = Some(date(2012, 6, 15)),
    toimipiste = kulosaarenAlaAste,
    suorituskieli = suomenKieli,
    osasuoritukset = kaikkiAineet,
    vahvistus = vahvistusPaikkakunnalla(date(2013, 5, 30))
  )

  val yhdeksännenLuokanSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(9, perusopetuksenDiaarinumero), luokka = "9C", alkamispäivä = Some(date(2015, 8, 15)),
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    vahvistus = vahvistusPaikkakunnalla(date(2016, 5, 30))
  )

  val perusopetuksenOppimääränSuoritusKesken = NuortenPerusopetuksenOppimääränSuoritus(
    koulutusmoduuli = perusopetus,
    toimipiste = jyväskylänNormaalikoulu,
    suoritustapa = suoritustapaKoulutus,
    suorituskieli = suomenKieli
  )

  val perusopetuksenOppimääränSuoritus = NuortenPerusopetuksenOppimääränSuoritus(
    koulutusmoduuli = perusopetus,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistusPaikkakunnalla(date(2016, 6, 4)),
    suoritustapa = suoritustapaKoulutus,
    osasuoritukset = kaikkiAineet,
    suorituskieli = suomenKieli
  )
}

