package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesPerusopetus.toimintaAlueenSuoritus
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{jyväskylänNormaalikoulu, kulosaarenAlaAste}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

object PerusopetusExampleData {
  def arviointi(arvosana: Int, arviointipäivä: Option[LocalDate] = None): Some[List[PerusopetuksenOppiaineenArviointi]] = Some(List(PerusopetuksenOppiaineenArviointi(arvosana, arviointipäivä)))
  def arviointi(arvosana: String, kuvaus: Option[LocalizedString]): Some[List[PerusopetuksenOppiaineenArviointi]] = Some(List(PerusopetuksenOppiaineenArviointi(arvosana, kuvaus)))
  def arviointi(arvosana: String, kuvaus: Option[LocalizedString], arviointipäivä: Option[LocalDate]): Some[List[PerusopetuksenOppiaineenArviointi]] = Some(List(PerusopetuksenOppiaineenArviointi(arvosana, kuvaus, arviointipäivä)))

  val hyväksytty = Some(List(PerusopetuksenOppiaineenArviointi("S", kuvaus = None)))
  val osallistunut = Some(List(PerusopetuksenOppiaineenArviointi("O", kuvaus = None)))

  def suoritus(aine: NuortenPerusopetuksenOppiaine) = NuortenPerusopetuksenOppiaineenSuoritus(
    koulutusmoduuli = aine,
    suorituskieli = None,
    arviointi = None
  )

  def vuosiviikkotuntia(määrä: Double): Some[LaajuusVuosiviikkotunneissa] = Some(LaajuusVuosiviikkotunneissa(määrä.toFloat))

  val exampleHenkilö: UusiHenkilö = asUusiOppija(KoskiSpecificMockOppijat.koululainen)

  val perusopetuksenDiaarinumero = "104/011/2014"
  val perusopetus = NuortenPerusopetus(Some(perusopetuksenDiaarinumero))
  val suoritustapaKoulutus = Koodistokoodiviite("koulutus", "perusopetuksensuoritustapa")
  val suoritustapaErityinenTutkinto = Koodistokoodiviite("erityinentutkinto", "perusopetuksensuoritustapa")
  val perusopetuksenOppimäärä = Koodistokoodiviite("perusopetus", "perusopetuksenoppimaara")

  val omanÄidinkielenOpinnotSaame = Some(OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina(
    arvosana = Koodistokoodiviite(koodiarvo = "8", koodistoUri = "arviointiasteikkoyleissivistava"),
    arviointipäivä = None,
    kieli = Kielivalikoima.saame,
    laajuus = Some(LaajuusVuosiviikkotunneissa(1))
  ))

  def paikallinenOppiaine(aine: String, nimi: String, kuvaus: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) =
    NuortenPerusopetuksenPaikallinenOppiaine(tunniste = PaikallinenKoodi(koodiarvo = aine, nimi = nimi), laajuus = laajuus, kuvaus = kuvaus)
  def oppiaine(aine: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = MuuNuortenPerusopetuksenOppiaine(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = aine), laajuus = laajuus)

  def uskonto(uskonto: Option[String] = None, laajuus: Option[LaajuusVuosiviikkotunneissa] = None, oppiaineenKoodiarvo: String = "KT") =
    NuortenPerusopetuksenUskonto(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava",
      koodiarvo = oppiaineenKoodiarvo),
      laajuus = laajuus,
      uskonnonOppimäärä = uskonto.map(u => Koodistokoodiviite(koodistoUri = "uskonnonoppimaara", koodiarvo = u)))

  def äidinkieli(kieli: String, diaarinumero: Option[String] = None, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = NuortenPerusopetuksenÄidinkieliJaKirjallisuus(
    perusteenDiaarinumero = diaarinumero,
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus"),
    laajuus = laajuus
  )
  def kieli(oppiaine: String, kieli: String, laajuus: Option[LaajuusVuosiviikkotunneissa] = None) = NuortenPerusopetuksenVierasTaiToinenKotimainenKieli(
    tunniste = Koodistokoodiviite(koodiarvo = oppiaine, koodistoUri = "koskioppiaineetyleissivistava"),
    kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima"),
    laajuus = laajuus
  )

  val äidinkielenSuoritus = suoritus(äidinkieli("AI1", laajuus = vuosiviikkotuntia(1))).copy(arviointi = arviointi(9)).copy(suoritustapa = Some(suoritustapaErityinenTutkinto))

  val oppiaineSuoritukset = List(
    äidinkielenSuoritus,
    suoritus(kieli("B1", "SV", vuosiviikkotuntia(1)).copy(laajuus = vuosiviikkotuntia(1))).copy(arviointi = arviointi(8)),
    suoritus(kieli("B1", "SV", vuosiviikkotuntia(1)).copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = hyväksytty),
    suoritus(kieli("A1", "EN", vuosiviikkotuntia(1))).copy(arviointi = arviointi(8)),
    suoritus(uskonto(Some("OR"), vuosiviikkotuntia(1))).copy(arviointi = arviointi(10)),
    suoritus(oppiaine("HI", vuosiviikkotuntia(1))).copy(arviointi = arviointi(8)),
    suoritus(oppiaine("YH", vuosiviikkotuntia(1))).copy(arviointi = arviointi(10)),
    suoritus(oppiaine("MA", vuosiviikkotuntia(1))).copy(arviointi = arviointi(9)),
    suoritus(oppiaine("KE", vuosiviikkotuntia(1))).copy(arviointi = arviointi(7)),
    suoritus(oppiaine("FY", vuosiviikkotuntia(1))).copy(arviointi = arviointi(9)),
    suoritus(oppiaine("BI", vuosiviikkotuntia(1))).copy(arviointi = arviointi(9), yksilöllistettyOppimäärä = true),
    suoritus(oppiaine("GE", vuosiviikkotuntia(1))).copy(arviointi = arviointi(9)),
    suoritus(oppiaine("MU", vuosiviikkotuntia(1))).copy(arviointi = arviointi(7)),
    suoritus(oppiaine("KU", vuosiviikkotuntia(1))).copy(arviointi = arviointi(8)),
    suoritus(oppiaine("KO", vuosiviikkotuntia(1))).copy(arviointi = arviointi(8)),
    suoritus(oppiaine("KO").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = hyväksytty),
    suoritus(oppiaine("TE", vuosiviikkotuntia(1))).copy(arviointi = arviointi(8)),
    suoritus(oppiaine("KS", vuosiviikkotuntia(1))).copy(arviointi = arviointi(9)),
    suoritus(oppiaine("LI", vuosiviikkotuntia(1))).copy(arviointi = arviointi(9), painotettuOpetus = true),
    suoritus(oppiaine("LI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(0.5))).copy(arviointi = hyväksytty),
    suoritus(kieli("B2", "DE").copy(pakollinen = false, laajuus = vuosiviikkotuntia(4))).copy(arviointi = arviointi(9)),
    suoritus(paikallinenOppiaine("TH", "Tietokoneen hyötykäyttö", "Kurssilla tarjotaan yksityiskohtaisempaa tietokoneen, oheislaitteiden sekä käyttöjärjestelmän ja ohjelmien tuntemusta.")).copy(arviointi = arviointi(9))
  )

  val kaikkiAineet = Some(oppiaineSuoritukset)

  def oppija(henkilö: UusiHenkilö = exampleHenkilö, opiskeluoikeus: Opiskeluoikeus): Oppija = Oppija(henkilö, List(opiskeluoikeus))

  def opiskeluoikeus(
    oppilaitos: Oppilaitos = jyväskylänNormaalikoulu,
    suoritukset: List[PerusopetuksenPäätasonSuoritus],
    alkamispäivä: LocalDate = date(2008, 8, 15),
    päättymispäivä: Option[LocalDate] = Some(date(2016, 6, 4))
  ): PerusopetuksenOpiskeluoikeus = {
    PerusopetuksenOpiskeluoikeus(
      oppilaitos = Some(oppilaitos),
      koulutustoimija = None,
      suoritukset = suoritukset,
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
        List(NuortenPerusopetuksenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä)) ++ päättymispäivä.toList.map(päivä => NuortenPerusopetuksenOpiskeluoikeusjakso(päivä, opiskeluoikeusValmistunut))
      )
    )
  }

  def päättötodistusOpiskeluoikeus(oppilaitos: Oppilaitos = jyväskylänNormaalikoulu, toimipiste: OrganisaatioWithOid = jyväskylänNormaalikoulu,  luokka: String = "C") = opiskeluoikeus(
    oppilaitos = oppilaitos,
    suoritukset = List(
      seitsemännenLuokanLuokallejääntiSuoritus.copy(toimipiste = toimipiste, luokka = "7" + luokka),
      kahdeksannenLuokanSuoritus.copy(toimipiste = toimipiste, luokka = "8" + luokka),
      yhdeksännenLuokanSuoritus.copy(toimipiste = toimipiste, luokka = "9" + luokka),
      perusopetuksenOppimääränSuoritus.copy(toimipiste = toimipiste))
  )

  def suoritusTuplana() = opiskeluoikeus(
    oppilaitos = jyväskylänNormaalikoulu,
    suoritukset = List(
      kahdeksannenLuokanSuoritus.copy(toimipiste = jyväskylänNormaalikoulu, luokka = "8C", alkamispäivä = Some(date(2014, 8, 16))),
      kahdeksannenLuokanSuoritus.copy(toimipiste = jyväskylänNormaalikoulu, luokka = "8C")
    ),
    päättymispäivä = None
  )

  def päättötodistusLuokanTuplauksellaOpiskeluoikeus(oppilaitos: Oppilaitos = jyväskylänNormaalikoulu, toimipiste: OrganisaatioWithOid = jyväskylänNormaalikoulu,  luokka: String = "C") = opiskeluoikeus(
    oppilaitos = oppilaitos,
    suoritukset = List(
      seitsemännenLuokanSuoritus.copy(toimipiste = toimipiste, luokka = "7A"),
      seitsemännenLuokanLuokallejääntiSuoritus.copy(toimipiste = toimipiste, luokka = "7" + luokka),
      kahdeksannenLuokanSuoritus.copy(toimipiste = toimipiste, luokka = "8" + luokka),
      yhdeksännenLuokanSuoritus.copy(toimipiste = toimipiste, luokka = "9" + luokka),
      perusopetuksenOppimääränSuoritus.copy(toimipiste = toimipiste))
  )

  def vuosiluokanOpiskeluoikeus(oppilaitos: Oppilaitos = jyväskylänNormaalikoulu, toimipiste: OrganisaatioWithOid = jyväskylänNormaalikoulu,  luokka: String = "C") = opiskeluoikeus(
    oppilaitos = oppilaitos,
    suoritukset = List(seitsemännenLuokanSuoritus.copy(toimipiste = toimipiste, luokka = "7A")),
    päättymispäivä = None
  )

  def nuortenPerusOpetuksenOppiaineenOppimääränSuoritus(aine: String) = {
    NuortenPerusopetuksenOppiaineenOppimääränSuoritus(
      koulutusmoduuli = oppiaine(aine).copy(perusteenDiaarinumero = Some(perusopetuksenDiaarinumero)),
      toimipiste = jyväskylänNormaalikoulu,
      vahvistus = vahvistusPaikkakunnalla(date(2016, 6, 4)),
      suoritustapa = suoritustapaKoulutus,
      suorituskieli = suomenKieli,
      arviointi = arviointi("S", kuvaus = None)
    )
  }

  val kahdeksannenLuokanSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(8, perusopetuksenDiaarinumero), luokka = "8C", alkamispäivä = Some(date(2014, 8, 15)),
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    muutSuorituskielet = Some(List(sloveeni)),
    kielikylpykieli = Some(ruotsinKieli),
    osasuoritukset = kaikkiAineet,
    käyttäytymisenArvio = Some(PerusopetuksenKäyttäytymisenArviointi(kuvaus = Some("Esimerkillistä käyttäytymistä koko vuoden ajan"))),
    vahvistus = vahvistusPaikkakunnalla(date(2015, 5, 30)),
    suoritustapa = Some(suoritustapaKoulutus)
  )

  val seitsemännenLuokanSuoritusLaajuudet_ennen_1_8_2020 = kahdeksannenLuokanSuoritus.copy(
    koulutusmoduuli = PerusopetuksenLuokkaAste(7, perusopetuksenDiaarinumero), luokka = "7", alkamispäivä = Some(date(2019, 8, 15)),
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistusPaikkakunnalla(date(2020, 7, 31))
  )

  val seitsemännenLuokanSuoritusLaajuudet_jälkeen_1_8_2020 = kahdeksannenLuokanSuoritus.copy(
    koulutusmoduuli = PerusopetuksenLuokkaAste(7, perusopetuksenDiaarinumero), luokka = "7", alkamispäivä = Some(date(2020, 7, 15)),
    vahvistus = vahvistusPaikkakunnalla(date(2020, 8, 1))
  )

  val kahdeksannenLuokanLuokalleJääntiSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(8, perusopetuksenDiaarinumero), luokka = "8", alkamispäivä = Some(date(2013, 8, 15)),
    jääLuokalle = true,
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    osasuoritukset = kaikkiAineet.map(_.map(_.copy(arviointi = arviointi(4), yksilöllistettyOppimäärä = false)).filter(_.koulutusmoduuli.pakollinen)),
    vahvistus = vahvistusPaikkakunnalla(date(2015, 5, 30))
  )

  val seitsemännenLuokanSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(7, perusopetuksenDiaarinumero), luokka = "7C", alkamispäivä = Some(date(2013, 8, 15)),
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    osasuoritukset = kaikkiAineet,
    vahvistus = vahvistusPaikkakunnalla(date(2015, 5, 30))
  )

  val seitsemännenLuokanLuokallejääntiSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(7, perusopetuksenDiaarinumero), luokka = "7C", alkamispäivä = Some(date(2013, 8, 16)),
    jääLuokalle = true,
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    osasuoritukset = kaikkiAineet.map(_.map(_.copy(arviointi = arviointi(4), yksilöllistettyOppimäärä = false)).filter(_.koulutusmoduuli.pakollinen)),
    vahvistus = vahvistusPaikkakunnalla(date(2014, 5, 30))
  )

  val kuudennenLuokanOsaAikainenErityisopetusSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(6, perusopetuksenDiaarinumero), luokka = "6A", alkamispäivä = Some(date(2012, 6, 15)),
    toimipiste = kulosaarenAlaAste,
    suorituskieli = suomenKieli,
    osasuoritukset = kaikkiAineet,
    osaAikainenErityisopetus = None,
    vahvistus = vahvistusPaikkakunnalla(date(2013, 5, 30))
  )

  val yhdeksännenLuokanSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(9, perusopetuksenDiaarinumero), luokka = "9C", alkamispäivä = Some(date(2015, 8, 15)),
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    vahvistus = vahvistusPaikkakunnalla(date(2016, 5, 30))
  )

  val yhdeksännenLuokanLuokallejääntiSuoritus = PerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = PerusopetuksenLuokkaAste(9, perusopetuksenDiaarinumero), luokka = "9A", alkamispäivä = Some(date(2014, 8, 16)),
    jääLuokalle = true,
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = suomenKieli,
    osasuoritukset = kaikkiAineet.map(_.map(_.copy(arviointi = arviointi(4), yksilöllistettyOppimäärä = false)).filter(_.koulutusmoduuli.pakollinen)),
    vahvistus = vahvistusPaikkakunnalla(date(2016, 5, 30))
  )

  val perusopetuksenOppimääränSuoritusKesken = NuortenPerusopetuksenOppimääränSuoritus(
    koulutusmoduuli = perusopetus,
    toimipiste = jyväskylänNormaalikoulu,
    suoritustapa = suoritustapaKoulutus,
    suorituskieli = suomenKieli,
    omanÄidinkielenOpinnot = omanÄidinkielenOpinnotSaame
  )

  val perusopetuksenOppimääränSuoritus = NuortenPerusopetuksenOppimääränSuoritus(
    koulutusmoduuli = perusopetus,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistusPaikkakunnalla(date(2016, 6, 4)),
    suoritustapa = suoritustapaKoulutus,
    osasuoritukset = kaikkiAineet,
    suorituskieli = suomenKieli
  )

  val päättötodistusToimintaAlueilla = NuortenPerusopetuksenOppimääränSuoritus(
    koulutusmoduuli = perusopetus,
    toimipiste = jyväskylänNormaalikoulu,
    vahvistus = vahvistusPaikkakunnalla(date(2016, 6, 4)),
    suoritustapa = suoritustapaKoulutus,
    osasuoritukset = Some(List(
      toimintaAlueenSuoritus("1").copy(arviointi = arviointi("S", Some(Finnish("Motoriset taidot kehittyneet hyvin perusopetuksen aikana"))))
    )),
    suorituskieli = suomenKieli
  )
}

