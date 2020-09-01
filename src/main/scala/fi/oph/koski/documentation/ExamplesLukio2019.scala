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
    oppiaineenSuoritus(Lukio2019ExampleData.lukionÄidinkieli("AI1", true)).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("OÄI1")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritus(moduuli("OÄI2")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritus(moduuli("OÄI3").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(8))
    ))),
    oppiaineenSuoritus(Lukio2019ExampleData.matematiikka("MAA")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("MAB2")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritus(moduuli("MAB3")).copy(arviointi = numeerinenArviointi(8)),
      moduulinSuoritus(moduuli("MAB4")).copy(arviointi = numeerinenArviointi(8))
    ))),
    oppiaineenSuoritus(Lukio2019ExampleData.lukionUskonto(Some("MU"))).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("UE1").copy(laajuus = laajuus(1.5))).copy(arviointi = numeerinenArviointi(7))
    ))),
    oppiaineenSuoritus(lukionKieli2019("AOM", "SV")).copy(arviointi = arviointi("9")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("RUA4").copy(laajuus = laajuus(1))).copy(arviointi = numeerinenArviointi(7))
    ))),
    oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("FY")).copy(arviointi = arviointi("10")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("FY1")).copy(arviointi = numeerinenArviointi(10)),
      moduulinSuoritus(moduuli("FY2")).copy(arviointi = numeerinenArviointi(10)),
      moduulinSuoritus(moduuli("FY3")).copy(arviointi = numeerinenArviointi(10)),
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY123", "Keittiöfysiikka", "Keittiöfysiikan kokeelliset perusteet, kiehumisreaktiot")).copy(arviointi = numeerinenArviointi(10)),
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("FY124", "Keittiöfysiikka 2", "Haastava kokeellinen keittiöfysiikka, liekitys ja lämpöreaktiot").copy(pakollinen = false)).copy(arviointi = numeerinenArviointi(9))
    ))),
    oppiaineenSuoritus(Lukio2019ExampleData.lukionOppiaine("KE")).copy(arviointi = arviointi("4")),
    oppiaineenSuoritus(PaikallinenLukionOppiaine2019(PaikallinenKoodi("ITT", "Tanssi ja liike"), "Tanssi ja liike", pakollinen = false)).copy(arviointi = arviointi("8")).copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("LI5")).copy(arviointi = numeerinenArviointi(7)),
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("ITT234", "Tanssin taito", "Perinteiset suomalaiset tanssit, valssi jne")).copy(arviointi = numeerinenArviointi(10))
    )))
  )

  val oppiaineSuorituksetJoissaMuitaSuorituksiaJaVastaavia =
    oppiainesuoritukset ::: List(lukioDiplomienSuoritus().copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("MELD5", 2.0f)).copy(arviointi = numeerinenArviointi(7)),
      moduulinSuoritus(moduuli("KÄLD3", 2.0f)).copy(arviointi = numeerinenArviointi(9))
    ))),
    muidenLukioOpintojenSuoritus().copy(osasuoritukset = Some(List(
      moduulinSuoritus(moduuli("KE3")).copy(arviointi = numeerinenArviointi(10)),
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("HAI765", "Kansanmusiikki haitarilla", "Kansamusiikkia 2-rivisellä haitarilla")).copy(arviointi = sanallinenArviointi("S"))
    ))),
    temaattistenOpintojenSuoritus().copy(osasuoritukset = Some(List(
      paikallisenOpintojaksonSuoritus(paikallinenOpintojakso("KAN200", "Kanteleensoiton perusteet", "Itäsuomalaisen kanteleensoiton perusteet")).copy(arviointi = sanallinenArviointi("S"))
    ))))

  val omanÄidinkielenOpinnotSaame = Some(OmanÄidinkielenOpinnotLaajuusOpintopisteinä(
    arvosana = Koodistokoodiviite(koodiarvo = "8", koodistoUri = "arviointiasteikkoyleissivistava"),
    arviointipäivä = None,
    kieli = Kielivalikoima.saame,
    laajuus = LaajuusOpintopisteissä(3)
  ))

  val puhviKoe = Some(PuhviKoe2019(
    arvosana = Koodistokoodiviite(koodiarvo = "7", koodistoUri = "arviointiasteikkoyleissivistava"),
    päivä = date(2019, 8, 30),
    kuvaus = None
  ))

  val suullisenKielitaidonKoeEnglanti = SuullisenKielitaidonKoe2019(
    kieli = Koodistokoodiviite("EN", Some("englanti"), "kieli", None),
    arvosana = Koodistokoodiviite("6", "arviointiasteikkoyleissivistava"),
    taitotaso = Koodistokoodiviite(koodiarvo = "B1.1", koodistoUri = "arviointiasteikkosuullisenkielitaidonkoetaitotaso"),
    kuvaus = None,
    päivä = date(2019, 9, 3)
  )

  val suullisenKielitaidonKoeEspanja = SuullisenKielitaidonKoe2019(
    kieli = Koodistokoodiviite("ES", Some("espanja"), "kieli", None),
    arvosana = Koodistokoodiviite("S", "arviointiasteikkoyleissivistava"),
    taitotaso = Koodistokoodiviite(koodiarvo = "yli_C1.1", koodistoUri = "arviointiasteikkosuullisenkielitaidonkoetaitotaso"),
    kuvaus = Some("Puhetaito äidinkielen tasolla"),
    päivä = date(2019, 9, 3)
  )

  lazy val oppimääränSuoritus = LukionOppimääränSuoritus2019(
    koulutusmoduuli = lukionOppimäärä2019,
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    vahvistus = vahvistusPaikkakunnalla(päivä = date(2020, 5, 15)),
    toimipiste = jyväskylänNormaalikoulu,
    suoritettuErityisenäTutkintona = true,
    omanÄidinkielenOpinnot = omanÄidinkielenOpinnotSaame,
    puhviKoe = puhviKoe,
    suullisenKielitaidonKokeet = Some(List(suullisenKielitaidonKoeEnglanti, suullisenKielitaidonKoeEspanja)),
    todistuksellaNäkyvätLisätiedot = Some("Osallistunut kansalliseen etäopetuskokeiluun"),
    osasuoritukset = Some(oppiaineSuorituksetJoissaMuitaSuorituksiaJaVastaavia),
    ryhmä = Some("AH")
  )

  lazy val oppiaineidenOppimäärienSuoritus = LukionOppiaineidenOppimäärienSuoritus2019(
    koulutusmoduuli = LukionOppiaineidenOppimäärät2019(perusteenDiaarinumero = lops2019perusteenDiaarinumero),
    oppimäärä = nuortenOpetussuunnitelma,
    suorituskieli = suomenKieli,
    toimipiste = jyväskylänNormaalikoulu,
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

  def moduulinSuoritus(moduuli: LukionModuuli2019) = LukionModuulinSuoritus2019(
    koulutusmoduuli = moduuli,
    suorituskieli = None
  )

  def paikallisenOpintojaksonSuoritus(opintojakso: LukionPaikallinenOpintojakso2019) = LukionPaikallisenOpintojaksonSuoritus2019(
    koulutusmoduuli = opintojakso,
    suorituskieli = None
  )

  def moduuli(moduuli: String, laajuusOpintopisteinä: Double = 2.0f) = LukionModuuli2019(
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

  def muidenLukioOpintojenSuoritus(): MuidenLukioOpintojenSuoritus2019 = muidenLukioOpintojenSuoritus(MuutLukionSuoritukset2019(Koodistokoodiviite(koodistoUri = "lukionmuutopinnot", koodiarvo= "MS"), None))

  def lukioDiplomienSuoritus(): MuidenLukioOpintojenSuoritus2019 = muidenLukioOpintojenSuoritus(Lukiodiplomit2019(Koodistokoodiviite(koodistoUri = "lukionmuutopinnot", koodiarvo= "LD"), None))

  def temaattistenOpintojenSuoritus(): MuidenLukioOpintojenSuoritus2019 = muidenLukioOpintojenSuoritus(TemaattisetOpinnot2019(Koodistokoodiviite(koodistoUri = "lukionmuutopinnot", koodiarvo= "TO"), None))

  private def muidenLukioOpintojenSuoritus(koulutusmoduuli: MuutSuorituksetTaiVastaavat2019): MuidenLukioOpintojenSuoritus2019 = MuidenLukioOpintojenSuoritus2019(
    koulutusmoduuli = koulutusmoduuli,
    osasuoritukset = None
  )

  def laajuus(arvo: Double) = LaajuusOpintopisteissä(arvo = arvo, yksikkö = laajuusOpintopisteissä)
}

