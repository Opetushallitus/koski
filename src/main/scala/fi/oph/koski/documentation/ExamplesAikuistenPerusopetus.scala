package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData.{suoritustapaErityinenTutkinto, äidinkieli, _}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.jyväskylänNormaalikoulu
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.schema._
import fi.oph.koski.localization.LocalizedStringImplicits._


object ExamplesAikuistenPerusopetus {
  val examples = List(
    Example("perusopetuksen oppiaineen oppimäärä - päättötodistus", "Aikuisopiskelija on suorittanut peruskoulun äidinkielen oppimäärän", aineopiskelija),
    Example("aikuisten perusopetuksen oppimäärä 2015", "Aikuisopiskelija on suorittanut aikuisten perusopetuksen oppimäärän opetussuunnitelman 2015 mukaisesti", aikuistenPerusopetuksenOppimäärä(aikuistenPerusopetus2015, oppiaineidenSuoritukset2015)),
    Example("aikuisten perusopetuksen oppimäärä 2017", "Aikuisopiskelija on suorittanut aikuisten perusopetuksen oppimäärän opetussuunnitelman 2017 mukaisesti", aikuistenPerusopetuksenOppimäärä(aikuistenPerusopetus2017, oppiaineidenSuoritukset2017))
  )

  lazy val aineopiskelija = Oppija(
    MockOppijat.eero.vainHenkilötiedot,
    List(PerusopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      koulutustoimija = None,
      suoritukset = List(
        PerusopetuksenOppiaineenOppimääränSuoritus(
          koulutusmoduuli = äidinkieli("AI1", diaarinumero = Some("104/011/2014")),
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          arviointi = arviointi(9),
          suoritustapa = suoritustapaErityinenTutkinto,
          vahvistus = vahvistusPaikkakunnalla(),
          suorituskieli = suomenKieli
        )),
      tila = PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )
      )
    ))
  )

  def aikuistenPerusopetuksenOppimäärä(koulutus: AikuistenPerusopetus, oppiaineet: Option[List[AikuistenPerusopetuksenOppiaineenSuoritus]]) = Oppija(
    exampleHenkilö,
    List(PerusopetuksenOpiskeluoikeus(
      alkamispäivä = Some(date(2008, 8, 15)),
      päättymispäivä = Some(date(2016, 6, 4)),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      koulutustoimija = None,
      suoritukset = List(
        AikuistenPerusopetuksenOppimääränSuoritus(
          koulutusmoduuli = koulutus,
          suorituskieli = suomenKieli,
          tila = tilaValmis,
          toimipiste = jyväskylänNormaalikoulu,
          vahvistus = vahvistusPaikkakunnalla(),
          suoritustapa = suoritustapaErityinenTutkinto,
          osasuoritukset = oppiaineet
        )),
      tila = PerusopetuksenOpiskeluoikeudenTila(
        List(
          PerusopetuksenOpiskeluoikeusjakso(date(2008, 8, 15), opiskeluoikeusLäsnä),
          PerusopetuksenOpiskeluoikeusjakso(date(2016, 6, 4), opiskeluoikeusValmistunut)
        )
      )
    ))
  )

  def oppiaineenSuoritus(aine: PerusopetuksenOppiaine) = AikuistenPerusopetuksenOppiaineenSuoritus(
    koulutusmoduuli = aine,
    suorituskieli = None,
    tila = tilaValmis,
    arviointi = None
  )

  lazy val aikuistenPerusopetus2015 = AikuistenPerusopetus(Some("19/011/2015"))
  lazy val aikuistenPerusopetus2017 = AikuistenPerusopetus(Some("OPH-1280-2017"))

  lazy val oppiaineidenSuoritukset2015 = Some(
    List(
      oppiaineenSuoritus(äidinkieli("AI1")).copy(
        arviointi = arviointi(9),
        osasuoritukset = Some(List(
          kurssinSuoritus2015("ÄI1"),
          kurssinSuoritus2015("ÄI2"),
          kurssinSuoritus2015("ÄI3"),
          kurssinSuoritusPaikallinen("ÄI10", "Paikallinen äidinkielen kurssi")
        ))
      ),
      oppiaineenSuoritus(kieli("B1", "SV")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(kieli("B1", "SV").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = hyväksytty),
      oppiaineenSuoritus(kieli("A1", "EN")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(oppiaine("KT")).copy(arviointi = arviointi(10)),
      oppiaineenSuoritus(oppiaine("HI")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(oppiaine("YH")).copy(arviointi = arviointi(10)),
      oppiaineenSuoritus(oppiaine("MA")).copy(arviointi = arviointi(9)),
      oppiaineenSuoritus(oppiaine("KE")).copy(arviointi = arviointi(7)),
      oppiaineenSuoritus(oppiaine("FY")).copy(arviointi = arviointi(9)),
      oppiaineenSuoritus(oppiaine("BI")).copy(arviointi = arviointi(9), yksilöllistettyOppimäärä = true),
      oppiaineenSuoritus(oppiaine("GE")).copy(arviointi = arviointi(9)),
      oppiaineenSuoritus(oppiaine("MU")).copy(arviointi = arviointi(7)),
      oppiaineenSuoritus(oppiaine("KU")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(oppiaine("KO")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(oppiaine("KO").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = hyväksytty),
      oppiaineenSuoritus(oppiaine("TE")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(oppiaine("KS")).copy(arviointi = arviointi(9)),
      oppiaineenSuoritus(oppiaine("LI")).copy(arviointi = arviointi(9), painotettuOpetus = true),
      oppiaineenSuoritus(oppiaine("LI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(0.5))).copy(arviointi = hyväksytty),
      oppiaineenSuoritus(kieli("B2", "DE").copy(pakollinen = false, laajuus = vuosiviikkotuntia(4))).copy(arviointi = arviointi(9)),
      oppiaineenSuoritus(valinnainenOppiaine("TH", "Tietokoneen hyötykäyttö", "Kurssilla tarjotaan yksityiskohtaisempaa tietokoneen, oheislaitteiden sekä käyttöjärjestelmän ja ohjelmien tuntemusta.")).copy(arviointi = arviointi(9))
    ))

  lazy val oppiaineidenSuoritukset2017 = Some(
    List(
      oppiaineenSuoritus(äidinkieli("AI1")).copy(
        arviointi = arviointi(9),
        osasuoritukset = Some(List(
          kurssinSuoritus2017("ÄI1"),
          kurssinSuoritus2017("ÄI2"),
          kurssinSuoritus2017("ÄI3"),
          kurssinSuoritusPaikallinen("ÄI10", "Paikallinen äidinkielen kurssi")
        ))
      ),
      oppiaineenSuoritus(kieli("B1", "SV")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(kieli("B1", "SV").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = hyväksytty),
      oppiaineenSuoritus(kieli("A1", "EN")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(oppiaine("KT")).copy(arviointi = arviointi(10)),
      oppiaineenSuoritus(oppiaine("HI")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(oppiaine("YH")).copy(arviointi = arviointi(10)),
      oppiaineenSuoritus(oppiaine("MA")).copy(arviointi = arviointi(9)),
      oppiaineenSuoritus(oppiaine("KE")).copy(arviointi = arviointi(7)),
      oppiaineenSuoritus(oppiaine("FY")).copy(arviointi = arviointi(9)),
      oppiaineenSuoritus(oppiaine("BI")).copy(arviointi = arviointi(9), yksilöllistettyOppimäärä = true),
      oppiaineenSuoritus(oppiaine("GE")).copy(arviointi = arviointi(9)),
      oppiaineenSuoritus(oppiaine("MU")).copy(arviointi = arviointi(7)),
      oppiaineenSuoritus(oppiaine("KU")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(oppiaine("KO")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(oppiaine("KO").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1))).copy(arviointi = hyväksytty),
      oppiaineenSuoritus(oppiaine("TE")).copy(arviointi = arviointi(8)),
      oppiaineenSuoritus(oppiaine("KS")).copy(arviointi = arviointi(9)),
      oppiaineenSuoritus(oppiaine("LI")).copy(arviointi = arviointi(9), painotettuOpetus = true),
      oppiaineenSuoritus(oppiaine("LI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(0.5))).copy(arviointi = hyväksytty),
      oppiaineenSuoritus(kieli("B2", "DE").copy(pakollinen = false, laajuus = vuosiviikkotuntia(4))).copy(arviointi = arviointi(9)),
      oppiaineenSuoritus(valinnainenOppiaine("TH", "Tietokoneen hyötykäyttö", "Kurssilla tarjotaan yksityiskohtaisempaa tietokoneen, oheislaitteiden sekä käyttöjärjestelmän ja ohjelmien tuntemusta.")).copy(arviointi = arviointi(9))
    ))

  def kurssinSuoritus2015(koodiarvo: String) = AikuistenPerusopetuksenKurssinSuoritus(
    ValtakunnallinenAikuistenPerusopetuksenKurssi2015(Koodistokoodiviite(koodiarvo, "aikuistenperusopetuksenkurssit2015")),
    tilaValmis,
    arviointi = arviointi(9)
  )

  def kurssinSuoritus2017(koodiarvo: String) = AikuistenPerusopetuksenKurssinSuoritus(
    ValtakunnallinenAikuistenPerusopetuksenPäättövaiheenKurssi2017(Koodistokoodiviite(koodiarvo, "aikuistenperusopetuksenpaattovaiheenkurssit2017")),
    tilaValmis,
    arviointi = arviointi(9)
  )

  def kurssinSuoritusPaikallinen(koodiarvo: String, kuvaus: String) = AikuistenPerusopetuksenKurssinSuoritus(
    PaikallinenAikuistenPerusopetuksenKurssi(PaikallinenKoodi(koodiarvo, kuvaus)),
    tilaValmis,
    arviointi = arviointi(9)
  )
}