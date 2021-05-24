package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.Lukio2019ExampleData._
import fi.oph.koski.documentation.LukioExampleData.{nuortenOpetussuunnitelma, opiskeluoikeusAktiivinen, opiskeluoikeusPäättynyt, laajuus => _}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{uusiLukio, uusiLukionAineopiskelija}
import fi.oph.koski.localization.LocalizedStringImplicits.str2localized
import fi.oph.koski.schema._

object ExamplesLukio2019 {
  val lops2019perusteenDiaarinumero = Some("OPH-2263-2019")
  val lops2019AikuistenPerusteenDiaarinumero = Some("OPH-2267-2019")

  val lukionOppimäärä2019: LukionOppimäärä = LukionOppimäärä(perusteenDiaarinumero = lops2019perusteenDiaarinumero)

  val oppiainesuoritukset = oppiainesuorituksetRiittääValmistumiseenNuorilla

  val oppiaineSuorituksetJoissaMuitaSuorituksiaJaVastaavia =
    oppiainesuoritukset ::: List(lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
      moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
      moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KÄLD3", 2.0f)).copy(arviointi = numeerinenArviointi(9))
    ))),
    muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
      moduulinSuoritusMuissaOpinnoissa(muuModuuliMuissaOpinnoissa("KE3")).copy(arviointi = numeerinenArviointi(10)),
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("HAI765", "Kansanmusiikki haitarilla", "Kansamusiikkia 2-rivisellä haitarilla")).copy(arviointi = sanallinenArviointi("S")),
      moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("VKA1", 2, "RU")).copy(arviointi = numeerinenArviointi(10)),
      moduulinSuoritusMuissaOpinnoissa(vieraanKielenModuuliMuissaOpinnoissa("ENA1", 2, "EN")).copy(arviointi = numeerinenArviointi(10))
    ))),
    temaattistenOpintojenSuoritus().copy(osasuoritukset = Some(List(
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("KAN200", "Kanteleensoiton perusteet", "Itäsuomalaisen kanteleensoiton perusteet")).copy(arviointi = sanallinenArviointi("S"))
    ))))

  lazy val oppimääränSuoritus = LukionOppimääränSuoritus2019(
    koulutusmoduuli = lukionOppimäärä2019,
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    vahvistus = vahvistusPaikkakunnalla(päivä = date(2020, 5, 15)),
    toimipiste = jyväskylänNormaalikoulu,
    omanÄidinkielenOpinnot = omanÄidinkielenOpinnotSaame,
    puhviKoe = puhviKoe,
    suullisenKielitaidonKokeet = Some(List(suullisenKielitaidonKoeEnglanti, suullisenKielitaidonKoeEspanja)),
    todistuksellaNäkyvätLisätiedot = Some("Osallistunut kansalliseen etäopetuskokeiluun"),
    osasuoritukset = Some(oppiaineSuorituksetJoissaMuitaSuorituksiaJaVastaavia),
    ryhmä = Some("AH")
  )

  lazy val vahvistamatonOppimääränSuoritus = oppimääränSuoritus.copy(vahvistus = None)

  lazy val oppiaineidenOppimäärienSuoritus = LukionOppiaineidenOppimäärienSuoritus2019(
    koulutusmoduuli = LukionOppiaineidenOppimäärät2019(perusteenDiaarinumero = lops2019perusteenDiaarinumero),
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    toimipiste = jyväskylänNormaalikoulu,
    suullisenKielitaidonKokeet = Some(List(suullisenKielitaidonKoeEspanja)),
    osasuoritukset = Some(oppiainesuoritukset)
  )

  lazy val opiskeluoikeus: LukionOpiskeluoikeus =
    LukionOpiskeluoikeus(
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
          LukionOpiskeluoikeusjakso(alku = date(2020, 5, 15), tila = opiskeluoikeusPäättynyt, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen))
        )
      ),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(oppimääränSuoritus)
    )

  lazy val aktiivinenOpiskeluoikeus: LukionOpiskeluoikeus =
    LukionOpiskeluoikeus(
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        )
      ),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(vahvistamatonOppimääränSuoritus)
    )

  lazy val oppiaineenOppimääräOpiskeluoikeus: LukionOpiskeluoikeus = opiskeluoikeus.copy(suoritukset = List(oppiaineidenOppimäärienSuoritus))

  lazy val aktiivinenOppiaineenOppimääräOpiskeluoikeus: LukionOpiskeluoikeus =
    LukionOpiskeluoikeus(
      tila = LukionOpiskeluoikeudenTila(
        List(
          LukionOpiskeluoikeusjakso(alku = date(2019, 8, 1), tila = opiskeluoikeusAktiivinen, opintojenRahoitus = Some(ExampleData.valtionosuusRahoitteinen)),
        )
      ),
      oppilaitos = Some(jyväskylänNormaalikoulu),
      suoritukset = List(oppiaineidenOppimäärienSuoritus.copy(vahvistus = None))
    )


  lazy val oppija = Oppija(asUusiOppija(uusiLukio), List(opiskeluoikeus))
  lazy val oppiaineidenOppimäärienOppija = Oppija(asUusiOppija(uusiLukionAineopiskelija), List(oppiaineenOppimääräOpiskeluoikeus))

  val examples = List(
    Example("lukio ops 2019 - oppimäärä", "Uuden 2019 opetussuunnitelman mukainen oppija, lukion oppimäärä", oppija),
    Example("lukio ops 2019 - oppiaineiden oppimäärä", "Uuden 2019 opetussuunnitelman mukainen oppija, lukion oppiaineiden oppimäärä", oppiaineidenOppimäärienOppija)
  )
}

object Lukio2019ExampleData {
  def oppiainesuorituksetEiRiitäValmistumiseen: List[LukionOppiaineenSuoritus2019] = List(
    oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI1")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI2")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("ÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8))
    ))),
    oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB2")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB3")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("MAB4")).copy(arviointi = numeerinenArviointi(9))
    ))),
    oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("OP")).copy(arviointi = sanallinenLukionOppiaineenArviointi("H")).copy(osasuoritukset = Some(List(
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP1")).copy(arviointi = sanallinenArviointi("H")),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("OP2")).copy(arviointi = sanallinenArviointi("S"))
    ))),
    oppiaineenSuoritus(Lukio2019ExampleData.lukionUskonto(Some("MU"))).copy(arviointi = numeerinenLukionOppiaineenArviointi(4)).copy(osasuoritukset = Some(List(
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("UE1").copy(laajuus = laajuus(1.5))).copy(arviointi = numeerinenArviointi(4))
    ))),
    oppiaineenSuoritus(lukionKieli2019("AOM", "SV")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
      moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("RUA4").copy(laajuus = laajuus(1))).copy(arviointi = numeerinenArviointi(7))
    ))),
    oppiaineenSuoritus(lukionKieli2019("A", "ES")).copy(arviointi = numeerinenLukionOppiaineenArviointi(9)).copy(osasuoritukset = Some(List(
      moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA1").copy(laajuus = laajuus(1))).copy(arviointi = numeerinenArviointi(7)),
      moduulinSuoritusOppiaineissa(vieraanKielenModuuliOppiaineissa("VKA8").copy(laajuus = laajuus(1))).copy(arviointi = numeerinenArviointi(7))
    )))
  )

  def oppiainesuorituksetRiittääValmistumiseenAikuisilla: List[LukionOppiaineenSuoritus2019] = oppiainesuorituksetEiRiitäValmistumiseen ::: List(
    oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("FY")).copy(arviointi = numeerinenLukionOppiaineenArviointi(10)).copy(osasuoritukset = Some(List(
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("FY1")).copy(arviointi = numeerinenArviointi(10)),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("FY2")).copy(arviointi = numeerinenArviointi(10)),
      moduulinSuoritusOppiaineissa(muuModuuliOppiaineissa("FY3")).copy(arviointi = numeerinenArviointi(10)),
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY123", "Keittiöfysiikka", "Keittiöfysiikan kokeelliset perusteet, kiehumisreaktiot").copy(laajuus = laajuus(80))).copy(arviointi = numeerinenArviointi(10)),
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY124", "Keittiöfysiikka 2", "Haastava kokeellinen keittiöfysiikka, liekitys ja lämpöreaktiot").copy(pakollinen = false)).copy(arviointi = sanallinenArviointi("S"))
    ))),
  )

  def oppiainesuorituksetRiittääValmistumiseenNuorilla: List[LukionOppiaineenSuoritus2019] = oppiainesuorituksetRiittääValmistumiseenAikuisilla ::: List(
    oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KE")).copy(arviointi = numeerinenLukionOppiaineenArviointi(4)),
    oppiaineenSuoritus(PaikallinenLukionOppiaine2019(PaikallinenKoodi("ITT", "Tanssi ja liike"), "Tanssi ja liike", pakollinen = false)).copy(arviointi = numeerinenLukionOppiaineenArviointi(8)).copy(osasuoritukset = Some(List(
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT234", "Tanssin taito", "Perinteiset suomalaiset tanssit, valssi jne").copy(laajuus = laajuus(50), pakollinen = false)).copy(arviointi = numeerinenArviointi(10)),
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT235", "Tanssin taito 2", "Uudemmat suomalaiset tanssit").copy(laajuus = laajuus(2))).copy(arviointi = numeerinenArviointi(10))
    )))
  )

  def numeerinenArviointi(arvosana: Int, päivä: LocalDate = date(2021, 9, 4)): Some[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] = {
    Some(List(new NumeerinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019(arvosana = Koodistokoodiviite(koodiarvo = arvosana.toString, koodistoUri = "arviointiasteikkoyleissivistava"), päivä)))
  }

  def sanallinenArviointi(arvosana: String, kuvaus: Option[String] = None, päivä: LocalDate = date(2021, 9, 4)): Some[List[LukionModuulinTaiPaikallisenOpintojaksonArviointi2019]] =
    (Arviointi.numeerinen(arvosana), kuvaus) match {
      case (Some(numero), None) => numeerinenArviointi(numero, päivä)
      case _ => Some(List(new SanallinenLukionModuulinTaiPaikallisenOpintojaksonArviointi2019(
        arvosana = Koodistokoodiviite(koodiarvo = arvosana, koodistoUri = "arviointiasteikkoyleissivistava"), kuvaus.map(LocalizedString.finnish), päivä)))
    }

  def numeerinenLukionOppiaineenArviointi(arvosana: Int): Some[List[NumeerinenLukionOppiaineenArviointi2019]] = {
    Some(List(NumeerinenLukionOppiaineenArviointi2019(arvosana.toString)))
  }

  def sanallinenLukionOppiaineenArviointi(arvosana: String): Some[List[SanallinenLukionOppiaineenArviointi2019]] = {
    Some(List(SanallinenLukionOppiaineenArviointi2019(arvosana)))
  }

  def oppiaineenSuoritus(aine: LukionOppiaine2019): LukionOppiaineenSuoritus2019 = LukionOppiaineenSuoritus2019(
    koulutusmoduuli = aine,
    suorituskieli = None,
    osasuoritukset = None
  )

  def matematiikka(matematiikka: String, laajuus: LaajuusOpintopisteissä) =
    LukionMatematiikka2019(oppimäärä = Koodistokoodiviite(koodiarvo = matematiikka, koodistoUri = "oppiainematematiikka"), laajuus = Some(laajuus))

  def matematiikka(matematiikka: String) =
    LukionMatematiikka2019(oppimäärä = Koodistokoodiviite(koodiarvo = matematiikka, koodistoUri = "oppiainematematiikka"))

  def lukionUskonto(uskonto: Option[String], laajuus: LaajuusOpintopisteissä): LukionUskonto2019 =
    LukionUskonto2019(
      tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = "KT"),
      uskonnonOppimäärä = uskonto.map(u => Koodistokoodiviite(koodistoUri = "uskonnonoppimaara", koodiarvo = u)),
      laajuus = Some(laajuus)
    )

  def lukionUskonto(uskonto: Option[String]): LukionUskonto2019 =
    LukionUskonto2019(
      tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = "KT"),
      uskonnonOppimäärä = uskonto.map(u => Koodistokoodiviite(koodistoUri = "uskonnonoppimaara", koodiarvo = u))
    )

  def lukionÄidinkieli(kieli: String, laajuus: LaajuusOpintopisteissä, pakollinen: Boolean) =
    LukionÄidinkieliJaKirjallisuus2019(kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus"), laajuus = Some(laajuus), pakollinen = pakollinen)

  def lukionÄidinkieli(kieli: String, pakollinen: Boolean) =
    LukionÄidinkieliJaKirjallisuus2019(kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "oppiaineaidinkielijakirjallisuus"), pakollinen = pakollinen)

  def lukionKieli2019(oppiaine: String, kieli: String) = {
    VierasTaiToinenKotimainenKieli2019(
      tunniste = Koodistokoodiviite(koodiarvo = oppiaine, koodistoUri = "koskioppiaineetyleissivistava"),
      kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima"))
  }

  def lukionOppiaine(aine: String, laajuus: LaajuusOpintopisteissä) =
    LukionMuuValtakunnallinenOppiaine2019(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = aine), laajuus = Some(laajuus))

  def lukionOppiaine(aine: String) =
    LukionMuuValtakunnallinenOppiaine2019(tunniste = Koodistokoodiviite(koodistoUri = "koskioppiaineetyleissivistava", koodiarvo = aine))

  def moduulinSuoritusOppiaineissa(moduuli: LukionModuuliOppiaineissa2019) = LukionModuulinSuoritusOppiaineissa2019(
    koulutusmoduuli = moduuli,
    suorituskieli = None
  )

  def moduulinSuoritusMuissaOpinnoissa(moduuli: LukionModuuliMuissaOpinnoissa2019) = LukionModuulinSuoritusMuissaOpinnoissa2019(
    koulutusmoduuli = moduuli,
    suorituskieli = None
  )

  def paikallisenOpintojaksonSuoritus(opintojakso: LukionPaikallinenOpintojakso2019) = LukionPaikallisenOpintojaksonSuoritus2019(
    koulutusmoduuli = opintojakso,
    suorituskieli = None
  )

  def vieraanKielenModuuliOppiaineissa(moduuli: String, laajuusOpintopisteinä: Double = 2.0f, kieli: Option[String] = None) =
    LukionVieraanKielenModuuliOppiaineissa2019(
      tunniste = Koodistokoodiviite(koodistoUri = "moduulikoodistolops2021", koodiarvo = moduuli),
      laajuus = laajuus(laajuusOpintopisteinä),
      pakollinen = true,
      kieli = kieli.map(k => Koodistokoodiviite(koodiarvo = k, koodistoUri = "kielivalikoima"))
    )

  def vieraanKielenModuuliMuissaOpinnoissa(moduuli: String, laajuusOpintopisteinä: Double = 2.0f, kieli: String) =
    LukionVieraanKielenModuuliMuissaOpinnoissa2019(
      tunniste = Koodistokoodiviite(koodistoUri = "moduulikoodistolops2021", koodiarvo = moduuli),
      laajuus = laajuus(laajuusOpintopisteinä),
      pakollinen = true,
      kieli = Koodistokoodiviite(koodiarvo = kieli, koodistoUri = "kielivalikoima")
    )

  def muuModuuliOppiaineissa(moduuli: String, laajuusOpintopisteinä: Double = 2.0f) = LukionMuuModuuliOppiaineissa2019(
    tunniste = Koodistokoodiviite(koodistoUri = "moduulikoodistolops2021", koodiarvo = moduuli),
    laajuus = laajuus(laajuusOpintopisteinä),
    pakollinen = true
  )

  def muuModuuliMuissaOpinnoissa(moduuli: String, laajuusOpintopisteinä: Double = 2.0f) = LukionMuuModuuliMuissaOpinnoissa2019(
    tunniste = Koodistokoodiviite(koodistoUri = "moduulikoodistolops2021", koodiarvo = moduuli),
    laajuus = laajuus(laajuusOpintopisteinä),
    pakollinen = true
  )

  def paikallinenOpintojakso(koodi: String, nimi: String, kuvaus: String) = LukionPaikallinenOpintojakso2019(
    tunniste = PaikallinenKoodi(koodi, nimi),
    laajuus = laajuus(1),
    kuvaus = kuvaus,
    pakollinen = true
  )

  def muidenLukioOpintojenSuoritus(): MuidenLukioOpintojenSuoritus2019 = muidenLukioOpintojenSuoritus(muutSuoritukset)

  def lukioDiplomienSuoritus(): MuidenLukioOpintojenSuoritus2019 = muidenLukioOpintojenSuoritus(lukiodiplomit)

  def temaattistenOpintojenSuoritus(): MuidenLukioOpintojenSuoritus2019 = muidenLukioOpintojenSuoritus(temaattisetOpinnot)

  def muutSuoritukset():MuutLukionSuoritukset2019 = MuutLukionSuoritukset2019(Koodistokoodiviite(koodistoUri = "lukionmuutopinnot", koodiarvo= "MS"), None)
  def lukiodiplomit(): Lukiodiplomit2019 = Lukiodiplomit2019(Koodistokoodiviite(koodistoUri = "lukionmuutopinnot", koodiarvo= "LD"), None)
  def temaattisetOpinnot(): TemaattisetOpinnot2019 = TemaattisetOpinnot2019(Koodistokoodiviite(koodistoUri = "lukionmuutopinnot", koodiarvo= "TO"), None)

  def muidenLukioOpintojenSuoritus(koulutusmoduuli: MuutSuorituksetTaiVastaavat2019): MuidenLukioOpintojenSuoritus2019 = MuidenLukioOpintojenSuoritus2019(
    koulutusmoduuli = koulutusmoduuli,
    osasuoritukset = None
  )

  def laajuus(arvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(arvo = arvo, yksikkö = laajuusOpintopisteissä)

  def omanÄidinkielenOpinnotSaame(): Some[OmanÄidinkielenOpinnotLaajuusOpintopisteinä] = Some(OmanÄidinkielenOpinnotLaajuusOpintopisteinä(
    arvosana = Koodistokoodiviite(koodiarvo = "8", koodistoUri = "arviointiasteikkoyleissivistava"),
    arviointipäivä = None,
    kieli = Kielivalikoima.saame,
    laajuus = LaajuusOpintopisteissä(3)
  ))

  def puhviKoe(): Some[PuhviKoe2019] = Some(PuhviKoe2019(
    arvosana = Koodistokoodiviite(koodiarvo = "7", koodistoUri = "arviointiasteikkoyleissivistava"),
    päivä = date(2019, 8, 30),
    kuvaus = None
  ))

  def suullisenKielitaidonKoeEnglanti(): SuullisenKielitaidonKoe2019 = SuullisenKielitaidonKoe2019(
    kieli = Koodistokoodiviite("EN", Some("englanti"), "kielivalikoima", None),
    arvosana = Koodistokoodiviite("6", "arviointiasteikkoyleissivistava"),
    taitotaso = Koodistokoodiviite(koodiarvo = "B1.1", koodistoUri = "arviointiasteikkokehittyvankielitaidontasot"),
    kuvaus = None,
    päivä = date(2019, 9, 3)
  )

  def suullisenKielitaidonKoeEspanja(): SuullisenKielitaidonKoe2019 = SuullisenKielitaidonKoe2019(
    kieli = Koodistokoodiviite("ES", Some("espanja"), "kielivalikoima", None),
    arvosana = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
    taitotaso = Koodistokoodiviite(koodiarvo = "yli_C1.1", koodistoUri = "arviointiasteikkokehittyvankielitaidontasot"),
    kuvaus = Some("Puhetaito äidinkielen tasolla"),
    päivä = date(2019, 9, 3)
  )

}

