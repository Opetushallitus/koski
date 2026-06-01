package fi.oph.koski.documentation

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

  def numeerinen(arvosana: Int) =
    Some(List(NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi(
      arvosana = Koodistokoodiviite(arvosana.toString, "ahvenanmaanarviointiasteikkoyleissivistava"),
      päivä = Some(date(2026, 6, 4))
    )))

  def sanallinen(arvosana: String = "G") =
    Some(List(SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi(
      arvosana = Koodistokoodiviite(arvosana, "ahvenanmaanarviointiasteikkoyleissivistava"),
      kuvaus = None,
      päivä = Some(date(2026, 6, 4))
    )))

  def oppiaineenSuoritus(oppiaine: AhvenanmaanPerusopetuksenOppiaine, arvosana: Int) =
    AhvenanmaanPerusopetuksenOppiaineenSuoritus(
      koulutusmoduuli = oppiaine,
      mukautettuOppimäärä = false,
      arviointi = numeerinen(arvosana)
    )

  val ysiluokanOppiaineet = List(
    oppiaineenSuoritus(oppiaine("SV", "Svenska"), 9),
    oppiaineenSuoritus(oppiaine("MA", "Matematik"), 8),
    oppiaineenSuoritus(oppiaine("BI", "Biologi"), 8),
    oppiaineenSuoritus(oppiaine("GE", "Geografi"), 7),
    oppiaineenSuoritus(oppiaine("FY", "Fysik"), 7),
    oppiaineenSuoritus(oppiaine("KE", "Kemi"), 8),
    oppiaineenSuoritus(oppiaine("TE", "Hälsokunskap"), 8),
    oppiaineenSuoritus(oppiaine("RELI", "Religions- och livsåskådningskunskap"), 8),
    oppiaineenSuoritus(oppiaine("HI", "Historia"), 9),
    oppiaineenSuoritus(oppiaine("SA", "Samhällskunskap"), 8),
    oppiaineenSuoritus(oppiaine("MU", "Musik"), 9),
    oppiaineenSuoritus(oppiaine("KU", "Bildkonst"), 8),
    oppiaineenSuoritus(oppiaine("KS", "Slöjd"), 7),
    oppiaineenSuoritus(oppiaine("LI", "Idrott"), 9),
    oppiaineenSuoritus(oppiaine("HEKO", "Hem- och konsumentkunskap"), 8),
    oppiaineenSuoritus(oppiaine("OP", "Elevhandledning"), 8),
    oppiaineenSuoritus(vierasKieli("A1", "EN"), 9),
    oppiaineenSuoritus(vierasKieli("B1", "FI"), 7),
  )

  val ysiluokanSuoritus = AhvenanmaanPerusopetuksenVuosiluokanSuoritus(
    koulutusmoduuli = AhvenanmaanPerusopetuksenLuokkaAste(
      tunniste = Koodistokoodiviite("9", "perusopetuksenluokkaaste"),
      perusteenDiaarinumero = Some(ahvenanmaanDiaarinumero)
    ),
    luokka = "9A",
    toimipiste = jyväskylänNormaalikoulu,
    suorituskieli = ruotsinKieli,
    vahvistus = vahvistusPaikkakunnalla(date(2026, 6, 4)),
    osasuoritukset = Some(ysiluokanOppiaineet),
  )

  val päättötodistuksenSuoritus = AhvenanmaanPerusopetuksenOppimääränSuoritus(
    koulutusmoduuli = AhvenanmaanPerusopetus(
      perusteenDiaarinumero = Some(ahvenanmaanDiaarinumero)
    ),
    toimipiste = jyväskylänNormaalikoulu,
    suoritustapa = Koodistokoodiviite("koulutus", "perusopetuksensuoritustapa"),
    suorituskieli = ruotsinKieli,
    vahvistus = vahvistusPaikkakunnalla(date(2026, 6, 4)),
    osasuoritukset = Some(ysiluokanOppiaineet),
  )

  val opiskeluoikeus = AhvenanmaanPerusopetuksenOpiskeluoikeus(
    oppilaitos = Some(jyväskylänNormaalikoulu),
    tila = AhvenanmaanPerusopetuksenOpiskeluoikeudenTila(
      List(
        AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(date(2017, 8, 15), opiskeluoikeusLäsnä),
        AhvenanmaanPerusopetuksenOpiskeluoikeusjakso(date(2026, 6, 4), opiskeluoikeusValmistunut)
      )
    ),
    suoritukset = List(ysiluokanSuoritus, päättötodistuksenSuoritus),
  )
}

object ExamplesAhvenanmaanPerusopetus {
  import AhvenanmaanPerusopetusExampleData._

  val ahvenanmaanPerusoppilas = Oppija(
    asUusiOppija(KoskiSpecificMockOppijat.ahvenanmaanPerusoppilas),
    List(opiskeluoikeus)
  )

  val examples = List(
    Example("ahvenanmaan perusopetus - 9. luokkalainen", "Ahvenanmaalainen oppilas on suorittamassa 9. luokkaa", ahvenanmaanPerusoppilas),
  )
}
