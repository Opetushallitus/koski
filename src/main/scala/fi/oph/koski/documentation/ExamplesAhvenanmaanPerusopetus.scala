package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema._

object AhvenanmaanPerusopetusExampleData {
  val ahvenanmaanDiaarinumero = "ÅLR2020/9841"

  val oppiaine = { (koodiarvo: String, nimi: String) =>
    AhvenanmaanPerusopetuksenMuuOppiaine(
      tunniste = Koodistokoodiviite(koodiarvo, "ahvenanmaankoskioppiaineetyleissivistava"),
      pakollinen = true
    )
  }

  val vierasKieli = { (koodiarvo: String, kieli: String) =>
    AhvenanmaanPerusopetuksenVierasKieli(
      tunniste = Koodistokoodiviite(koodiarvo, "ahvenanmaankoskioppiaineetyleissivistava"),
      kieli = Koodistokoodiviite(kieli, "kielivalikoima"),
      pakollinen = true
    )
  }

  def numeerinen(arvosana: Int, päivä: LocalDate = date(2026, 6, 4)) =
    Some(List(NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi(
      arvosana = Koodistokoodiviite(arvosana.toString, "ahvenanmaanarviointiasteikkoyleissivistava"),
      päivä = Some(päivä)
    )))

  def sanallinen(arvosana: String = "G") =
    Some(List(SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi(
      arvosana = Koodistokoodiviite(arvosana, "ahvenanmaanarviointiasteikkoyleissivistava"),
      päivä = Some(date(2026, 6, 4))
    )))

  def oppiaineenSuoritus(
    oppiaine: AhvenanmaanPerusopetuksenOppiaine,
    arvosana: Int,
    päivä: LocalDate = date(2026, 6, 4)
  ) =
    AhvenanmaanPerusopetuksenOppiaineenSuoritus(
      koulutusmoduuli = oppiaine,
      mukautettuOppimäärä = false,
      arviointi = numeerinen(arvosana, päivä)
    )

  // Gemensamma ämnen (åk 7–9 / avgångsbetyg) betygsformulärin mukaisessa
  // järjestyksessä, lopuksi yksi valinnainen tillvalsämne (B1-finska). Käsityö
  // on jaettu TX/TN:ään ja oppilaanohjaus (EH) jätetty pois – se ei ole
  // arvosanallinen oppiaine betygsformulärissa.
  def oppiaineet(päivä: LocalDate): List[AhvenanmaanPerusopetuksenOppiaineenSuoritus] = List(
    oppiaineenSuoritus(oppiaine("SV", "Svenska"), 9, päivä),
    oppiaineenSuoritus(oppiaine("MA", "Matematik"), 8, päivä),
    oppiaineenSuoritus(oppiaine("BI", "Biologi"), 8, päivä),
    oppiaineenSuoritus(oppiaine("GE", "Geografi"), 7, päivä),
    oppiaineenSuoritus(oppiaine("FY", "Fysik"), 7, päivä),
    oppiaineenSuoritus(oppiaine("KE", "Kemi"), 8, päivä),
    oppiaineenSuoritus(oppiaine("TE", "Hälsokunskap"), 8, päivä),
    oppiaineenSuoritus(oppiaine("RELI", "Religions- och livsåskådningskunskap"), 8, päivä),
    oppiaineenSuoritus(oppiaine("HI", "Historia"), 9, päivä),
    oppiaineenSuoritus(oppiaine("SA", "Samhällskunskap"), 8, päivä),
    oppiaineenSuoritus(oppiaine("MU", "Musik"), 9, päivä),
    oppiaineenSuoritus(oppiaine("KU", "Bildkonst"), 8, päivä),
    oppiaineenSuoritus(oppiaine("TX", "Textilslöjd"), 8, päivä),
    oppiaineenSuoritus(oppiaine("TN", "Teknisk slöjd"), 7, päivä),
    oppiaineenSuoritus(oppiaine("ID", "Idrott"), 9, päivä),
    oppiaineenSuoritus(oppiaine("HEKO", "Hem- och konsumentkunskap"), 8, päivä),
    oppiaineenSuoritus(vierasKieli("A1", "EN"), 9, päivä),
    oppiaineenSuoritus(vierasKieli("B1", "FI").copy(pakollinen = false), 7, päivä),
  )

  // Läsårsbetyg åk 8: viimeinen vuosiluokka, jolta annetaan oma lukuvuositodistus
  // oppiaineiden arvosanoineen. (Ansvar och samarbete -arvio annetaan editorissa.)
  val kahdeksannenLuokanSuoritus = AhvenanmaanPerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = AhvenanmaanPerusopetuksenLuokkaAste(
      tunniste = Koodistokoodiviite("8", "perusopetuksenluokkaaste"),
      perusteenDiaarinumero = Some(ahvenanmaanDiaarinumero)
    ),
    luokka = "8A",
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = ruotsinKieli,
    alkamispäivä = Some(date(2024, 8, 15)),
    vahvistus = vahvistusPaikkakunnalla(date(2025, 6, 4)),
    osasuoritukset = Some(oppiaineet(date(2025, 6, 4))),
  )

  // Päättövuoden (åk 9) vuosiluokan suoritus jää tyhjäksi: lopulliset arvosanat
  // kirjataan päättötodistukselle (avgångsbetyg), kuten manner-Suomessa. Vain
  // luokalle jäävän oppilaan 9. luokan läsårsbetyg saisi omat oppiaineensa.
  val ysiluokanSuoritus = AhvenanmaanPerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = AhvenanmaanPerusopetuksenLuokkaAste(
      tunniste = Koodistokoodiviite("9", "perusopetuksenluokkaaste"),
      perusteenDiaarinumero = Some(ahvenanmaanDiaarinumero)
    ),
    luokka = "9A",
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = ruotsinKieli,
    alkamispäivä = Some(date(2025, 8, 15)),
    vahvistus = vahvistusPaikkakunnalla(date(2026, 6, 4)),
  )

  // Avgångsbetyg: perusopetuksen päättötodistuksen lopulliset arvosanat.
  val päättötodistuksenSuoritus = AhvenanmaanPerusopetuksenOppimääränSuoritus(
    koulutusmoduuli = AhvenanmaanPerusopetus(
      perusteenDiaarinumero = Some(ahvenanmaanDiaarinumero)
    ),
    toimipiste = jyväskylänNormaalikoulu,
    suoritustapa = Koodistokoodiviite("koulutus", "perusopetuksensuoritustapa"),
    suorituskieli = ruotsinKieli,
    vahvistus = vahvistusPaikkakunnalla(date(2026, 6, 4)),
    osasuoritukset = Some(oppiaineet(date(2026, 6, 4))),
  )

  val opiskeluoikeus = AhvenanmaanPerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    tila = AhvenanmaanPerusopetuksenOpiskeluoikeudenTila(
      List(
        AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(date(2017, 8, 15), opiskeluoikeusLäsnä),
        AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(date(2026, 6, 4), opiskeluoikeusValmistunut)
      )
    ),
    suoritukset = List(kahdeksannenLuokanSuoritus, ysiluokanSuoritus, päättötodistuksenSuoritus),
  )
}

object ExamplesAhvenanmaanPerusopetus {
  import AhvenanmaanPerusopetusExampleData._

  val ahvenanmaanPerusoppilas = Oppija(
    asUusiOppija(KoskiSpecificMockOppijat.ahvenanmaanPerusoppilas),
    List(opiskeluoikeus)
  )

  val examples = List(
    Example("ahvenanmaan perusopetus - päättötodistus", "Ahvenanmaalainen oppilas on saanut perusopetuksen päättötodistuksen (avgångsbetyg)", ahvenanmaanPerusoppilas),
  )
}
