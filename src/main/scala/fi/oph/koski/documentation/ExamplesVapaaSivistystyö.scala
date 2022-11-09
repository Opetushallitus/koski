package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.VapaaSivistystyöExampleData.{muuallaSuoritettujenOpintojenSuoritus, _}
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

object ExamplesVapaaSivistystyö {
  lazy val examples = List(
    Example("vapaa sivistystyö - oppivelvollisille suunnattu koulutus", "Oppija suorittaa oppivelvollisille suunnattua koulutusta kansanopistossa", VapaaSivistystyöExample.oppivelvollisuuskoulutusExample),
    Example("vapaa sivistystyö - maahanmuuttajien kotoutuskoulutus", "Oppija suorittaa maahannmuuttajien kotoutumiskoulutusta kansanopistossa", VapaaSivistystyöExample.maahanmuuttajienkotoutusExample),
    Example("vapaa sivistystyö - lukutaitokoulutus", "Oppija suorittaa lukutaitokoulutusta kansanopistossa", VapaaSivistystyöExample.lukutaitokoulutusExample),
    Example("vapaa sivistystyö - vapaatavoitteinen koulutus", "Oppija suorittaa vapaatavoitteisia koulutuksia kansanopistossa", VapaaSivistystyöExample.vapaatavoitteinenKoulutusExample)
  )
}

object VapaaSivistystyöExample {
  val opiskeluoikeusHyväksytystiSuoritettu = Koodistokoodiviite("hyvaksytystisuoritettu", Some("Hyväksytysti suoritettu"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusKeskeytynyt = Koodistokoodiviite("keskeytynyt", Some("Keskeytynyt"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusMitätöity = Koodistokoodiviite("mitatoity", Some("Mitätöity"), "koskiopiskeluoikeudentila", Some(1))
  val exampleOpintokokonaisuus = Koodistokoodiviite("1138", Some("Kuvallisen ilmaisun perusteet ja välineet"), "opintokokonaisuudet", Some(1))
  lazy val opiskeluoikeusKOPS = VapaanSivistystyönOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
      OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä)
    )),
    lisätiedot = None,
    oppilaitos = Some(varsinaisSuomenKansanopisto),
    suoritukset = List(suoritusKOPS)
  )

  lazy val opiskeluoikeusKOTO = VapaanSivistystyönOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
      OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä)
    )),
    lisätiedot = None,
    oppilaitos = Some(varsinaisSuomenKansanopisto),
    suoritukset = List(suoritusKOTO)
  )

  lazy val opiskeluoikeusLukutaito = VapaanSivistystyönOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
      OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä)
    )),
    lisätiedot = None,
    oppilaitos = Some(varsinaisSuomenKansanopisto),
    suoritukset = List(suoritusLukutaito)
  )

  lazy val opiskeluoikeusVapaatavoitteinen = VapaanSivistystyönOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
      VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusHyväksytystiSuoritettu)
    )),
    lisätiedot = None,
    oppilaitos = Some(varsinaisSuomenKansanopisto),
    suoritukset = List(suoritusVapaatavoitteinenKoulutus)
  )

  lazy val opiskeluoikeusVapaatavoitteinenIlmanOpintokokonaisuutta = VapaanSivistystyönOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
      OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusHyväksytystiSuoritettu)
    )),
    lisätiedot = None,
    oppilaitos = Some(varsinaisSuomenKansanopisto),
    suoritukset = List(suoritusVapaatavoitteinenKoulutusIlmanOpintokokonaisuutta)
  )

  lazy val suoritusKOPS = OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
    toimipiste = varsinaisSuomenKansanopistoToimipiste,
    tyyppi = Koodistokoodiviite("vstoppivelvollisillesuunnattukoulutus", koodistoUri = "suorituksentyyppi"),
    koulutusmoduuli = OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(perusteenDiaarinumero = Some("OPH-58-2021")),
    vahvistus = vahvistus(päivä = date(2022, 5, 31)),
    suorituskieli = suomenKieli,
    todistuksellaNäkyvätLisätiedot = Some("Opinnot suoritettu pandemian vuoksi etäopintoina"),
    osasuoritukset = Some(List(
      osaamiskokonaisuudenSuoritus("1002", List(
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("A01", "Arjen rahankäyttö", "Arjen rahankäyttö", 2.0)
        ),
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("M01", "Mielen liikkeet", "Mielen liikkeet ja niiden havaitseminen", 2.0),
          vstArviointi("Hyväksytty", date(2021, 11, 2))
        )
      )),
      osaamiskokonaisuudenSuoritus("1003", List(
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("OP01", "Oman opiskelutyyli", "Oman opiskelutyylin analysointi ja tavoitteiden asettaminen", 4.0)
        )
      )),
      osaamiskokonaisuudenSuoritus("1005", List(
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("Mat01", "Matematiikka arjessa", "Matematiikan jokapäiväinen käyttö", 2.0)
        ),
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("Mat04", "Geometria", "Geometrian perusteet", 4.0)
        ),
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("Mat06", "Trigonometria", "Trigonometrian perusteet", 2.0),
          vstArviointi("Hyväksytty", date(2021, 12, 10))
        )
      )),
      osaamiskokonaisuudenSuoritus("1006", List(
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("KANS200", "Kansalaisuus", "Kansalaisuuden merkitys moniarvoisess yhteiskunnasa", 4.0)
        )
      )),
      osaamiskokonaisuudenSuoritus("1007", List(
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("CV01", "CV:n laadinta", "CV:n laadinta ja käyttö työnhaussa", 4.0)
        )
      )),
      suuntautumisopintojenSuoritus(List(
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("ATX01", "Tietokoneen huolto", "Nykyaikaisen tietokoneen tyypilliset huoltotoimenpiteet", 5.0),
          vstArviointi("Hyväksytty", date(2021, 11, 12))
        ),
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("VT02", "Valaisintekniikka", "Valaisinlähteet ja niiden toiminta", 6.0)
        ),
        opintokokonaisuudenSuoritus(
          opintokokonaisuus("TAI01", "Taide työkaluna", "Taiteen käyttö työkaluna", 15.0)
        ),
        muuallaSuoritettujenOpintojenSuoritus(
          muuallaSuoritetutOpinnot("Lukion lyhyen matematiikan kurssi M02", 5.0),
          vstArviointi("Hyväksytty", date(2021, 11, 12))
        )
      )),
    ))
  )

  lazy val suoritusKOTO = OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
    toimipiste = OidOrganisaatio(MockOrganisaatiot.itäsuomenYliopisto),
    tyyppi = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutus", koodistoUri = "suorituksentyyppi"),
    koulutusmoduuli = VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus(perusteenDiaarinumero = Some("OPH-123-2021"), laajuus = Some(LaajuusOpintopisteissä(54))),
    vahvistus = vahvistus(päivä = date(2022, 5, 31)),
    suorituskieli = suomenKieli,
    todistuksellaNäkyvätLisätiedot = None,
    osasuoritukset = Some(List(
      vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(),
      vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(),
      vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(),
      vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus()
    ))
  )

  lazy val suoritusLukutaito = VapaanSivistystyönLukutaitokoulutuksenSuoritus(
    toimipiste = OidOrganisaatio(MockOrganisaatiot.itäsuomenYliopisto),
    tyyppi = Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutus", koodistoUri = "suorituksentyyppi"),
    koulutusmoduuli = VapaanSivistystyönLukutaitokoulutus(perusteenDiaarinumero = Some("OPH-2984-2017"), laajuus = Some(LaajuusOpintopisteissä(80))),
    vahvistus = vahvistus(päivä = date(2022, 5, 31)),
    suorituskieli = suomenKieli,
    todistuksellaNäkyvätLisätiedot = None,
    osasuoritukset = Some(List(
      vapaanSivistystyönLukutaitokoulutuksenVuorovaikutustilanteissaToimimisenSuoritus(),
      vapaanSivistystyönLukutaitokoulutuksenTekstienLukeminenJaTulkitseminenSuoritus(),
      vapaanSivistystyönLukutaitokoulutuksenTekstienKirjoittaminenJaTuottaminenToimimisenSuoritus(),
      vapaanSivistystyönLukutaitokoulutuksenNumeeristenTaitojenSuoritus()
    ))
  )

  lazy val suoritusVapaatavoitteinenKoulutus = VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(
    toimipiste = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopisto),
    tyyppi = Koodistokoodiviite(koodiarvo = "vstvapaatavoitteinenkoulutus", koodistoUri = "suorituksentyyppi"),
    koulutusmoduuli = VapaanSivistystyönVapaatavoitteinenKoulutus(laajuus = Some(LaajuusOpintopisteissä(5)), opintokokonaisuus = Some(exampleOpintokokonaisuus)),
    vahvistus = vahvistus(päivä = date(2022, 5, 31)),
    suorituskieli = suomenKieli,
    todistuksellaNäkyvätLisätiedot = None,
    osasuoritukset = Some(List(
      vapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
    ))
  )

  lazy val suoritusVapaatavoitteinenKoulutusIlmanOpintokokonaisuutta = VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(
    toimipiste = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopisto),
    tyyppi = Koodistokoodiviite(koodiarvo = "vstvapaatavoitteinenkoulutus", koodistoUri = "suorituksentyyppi"),
    koulutusmoduuli = VapaanSivistystyönVapaatavoitteinenKoulutus(laajuus = Some(LaajuusOpintopisteissä(5)), opintokokonaisuus = None),
    vahvistus = vahvistus(päivä = date(2022, 5, 31)),
    suorituskieli = suomenKieli,
    todistuksellaNäkyvätLisätiedot = None,
    osasuoritukset = Some(List(
      vapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
    ))
  )

  lazy val oppivelvollisuuskoulutusExample = Oppija(
    VapaaSivistystyöExampleData.exampleHenkilöKOPS,
    List(opiskeluoikeusKOPS)
  )

  lazy val maahanmuuttajienkotoutusExample = Oppija(
    VapaaSivistystyöExampleData.exampleHenkilöKOTO,
    List(opiskeluoikeusKOTO)
  )

  lazy val lukutaitokoulutusExample = Oppija(
    VapaaSivistystyöExampleData.exampleHenkilöLukutaito,
    List(opiskeluoikeusLukutaito)
  )

  lazy val vapaatavoitteinenKoulutusExample = Oppija(
    VapaaSivistystyöExampleData.exampleHenkilöVapaatavoitteinenKoulutus,
    List(opiskeluoikeusVapaatavoitteinen)
  )
}

object VapaaSivistystyöExampleData {
  val exampleHenkilöKOPS = asUusiOppija(KoskiSpecificMockOppijat.vapaaSivistystyöOppivelvollinen)
  val exampleHenkilöKOTO = asUusiOppija(KoskiSpecificMockOppijat.vapaaSivistystyöMaahanmuuttajienKotoutus)
  val exampleHenkilöLukutaito = asUusiOppija(KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKotoutus)
  val exampleHenkilöVapaatavoitteinenKoulutus = asUusiOppija(KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKotoutus)

  lazy val varsinaisSuomenAikuiskoulutussäätiö: Koulutustoimija = Koulutustoimija(MockOrganisaatiot.varsinaisSuomenAikuiskoulutussäätiö, Some("Varsinais-Suomen Aikuiskoulutussäätiö sr"), Some("0136193-2"), Some(Koodistokoodiviite("577", None, "kunta", None)))

  lazy val varsinaisSuomenKansanopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.varsinaisSuomenKansanopisto, Some(Koodistokoodiviite("01694", None, "oppilaitosnumero", None)), Some("Varsinais-Suomen kansanopisto"))

  lazy val varsinaisSuomenKansanopistoToimipiste: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste)

  lazy val tunnustettu: VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen = VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen(
    "Tutkinnon osa on tunnustettu Kone- ja metallialan perustutkinnosta"
  )

  def osaamiskokonaisuudenSuoritus(
    osaamiskokonaisuusKoodiarvo: String = "1002",
    opintokokonaisuudet: List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus] = List(opintokokonaisuudenSuoritus())
  ): OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus = {
    osaamiskokonaisuudenSuoritus(osaamiskokonaisuusKoodiarvo, None, Some(opintokokonaisuudet))
  }

  def tyhjäOsaamiskokonaisuudenSuoritus(
    osaamiskokonaisuusKoodiarvo: String = "1002",
    laajuus: Option[LaajuusOpintopisteissä] = None
  ): OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus = {
    osaamiskokonaisuudenSuoritus(osaamiskokonaisuusKoodiarvo, laajuus, None)
  }

  private def osaamiskokonaisuudenSuoritus(
    osaamiskokonaisuusKoodiarvo: String,
    laajuus: Option[LaajuusOpintopisteissä],
    opintokokonaisuudet: Option[List[OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus]]
  ): OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus = {
    OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
      koulutusmoduuli = OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus(
        tunniste = Koodistokoodiviite(osaamiskokonaisuusKoodiarvo, "vstosaamiskokonaisuus"),
        laajuus = laajuus
      ),
      osasuoritukset = opintokokonaisuudet,
    )
  }

  def suuntautumisopintojenSuoritus(
    opintokokonaisuudet: List[VapaanSivistystyönOpintokokonaisuudenSuoritus] = List(opintokokonaisuudenSuoritus())
  ): OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus = {
    suuntautumisopintojenSuoritus(None, Some(opintokokonaisuudet))
  }

  def tyhjäSuuntautumisopintojenSuoritus(
    laajuus: Option[LaajuusOpintopisteissä] = None
  ): OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus = {
    suuntautumisopintojenSuoritus(laajuus, None)
  }

  private def suuntautumisopintojenSuoritus(
    laajuus: Option[LaajuusOpintopisteissä],
    opintokokonaisuudet: Option[List[VapaanSivistystyönOpintokokonaisuudenSuoritus]]
  ): OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus = {
    OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
      koulutusmoduuli = OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot(laajuus = laajuus),
      osasuoritukset = opintokokonaisuudet
    )
  }

  def opintokokonaisuudenSuoritus(
    koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus = opintokokonaisuus(),
    arviointi: OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi = vstArviointi()
  ): OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus = {
    OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(
      tyyppi = Koodistokoodiviite("vstopintokokonaisuus", koodistoUri = "suorituksentyyppi"),
      koulutusmoduuli = koulutusmoduuli,
      arviointi = Some(List(arviointi))
    )
  }

  def muuallaSuoritettujenOpintojenSuoritus(
                                   koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot = muuallaSuoritetutOpinnot(),
                                   arviointi: OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi = vstArviointi()
                                 ): MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus = {
    MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
      tyyppi = Koodistokoodiviite("vstmuuallasuoritetutopinnot", koodistoUri = "suorituksentyyppi"),
      koulutusmoduuli = koulutusmoduuli,
      arviointi = Some(List(arviointi)),
      tunnustettu = Some(tunnustettu)
    )
  }

  def opintokokonaisuus(koodiarvo: String = "A01", nimi: String = "Arjen rahankäyttö" , kuvaus: String = "Arjen rahankäyttö", laajuusArvo: Double = 2.0): OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus = {
    OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus(PaikallinenKoodi(koodiarvo, nimi), kuvaus, laajuus(laajuusArvo))
  }


  def muuallaSuoritetutOpinnot(kuvaus: String = "Lukiossa suoritettuja opintoja", laajuusArvo: Double = 2.0): MuuallaSuoritetutVapaanSivistystyönOpinnot = {
    MuuallaSuoritetutVapaanSivistystyönOpinnot(Koodistokoodiviite("lukioopinnot", "vstmuuallasuoritetutopinnot"), kuvaus, laajuus(laajuusArvo))
  }

  def vstArviointi(arvosana:String = "Hyväksytty", päivä: LocalDate = date(2021, 10, 30)): OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi = {
    OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkovst"), päivä)
  }

  def lukutaitokoulutuksenArviointi(arvosana: String, päivä: LocalDate = date(2021, 10, 30), taitotaso: String = "C2.2") = {
    LukutaitokoulutuksenArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkovst"), päivä, Koodistokoodiviite(taitotaso, "arviointiasteikkokehittyvankielitaidontasot"))
  }

  def laajuus(arvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(arvo = arvo, yksikkö = laajuusOpintopisteissä)

  // Maahanmuuttajien kotoutumiskoulutuksen rakenteen osia

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(laajuus: LaajuusOpintopisteissä = LaajuusOpintopisteissä(30)) = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(
      koulutusmoduuli = VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenKoulutusmoduuli(
        Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenkieliopintojensuoritus", koodistoUri = "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus"),
        Some(laajuus)
      ),
      arviointi = Some(List(vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi))
    )
  }

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi() = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi(
      kuullunYmmärtämisenTaitotaso = Some(
        VSTKehittyvänKielenTaitotasonArviointi(
          Koodistokoodiviite(koodiarvo = "C2.2", koodistoUri = "arviointiasteikkokehittyvankielitaidontasot")
        )
      ),
      puhumisenTaitotaso = Some(
        VSTKehittyvänKielenTaitotasonArviointi(
          Koodistokoodiviite(koodiarvo = "C2.2", koodistoUri = "arviointiasteikkokehittyvankielitaidontasot")
        )
      ),
      luetunYmmärtämisenTaitotaso = Some(
        VSTKehittyvänKielenTaitotasonArviointi(
          Koodistokoodiviite(koodiarvo = "C2.2", koodistoUri = "arviointiasteikkokehittyvankielitaidontasot")
        )
      ),
      kirjoittamisenTaitotaso = Some(
        VSTKehittyvänKielenTaitotasonArviointi(
          Koodistokoodiviite(koodiarvo = "C2.2", koodistoUri = "arviointiasteikkokehittyvankielitaidontasot")
        )
      ),
      päivä = date(2020, 1, 1)
    )
  }

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(laajuus: LaajuusOpintopisteissä = LaajuusOpintopisteissä(5)) = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(
      koulutusmoduuli = VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenKoulutusmoduuli(
        Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenohjauksensuoritus", koodistoUri = "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus"),
        Some(laajuus)
      ),
      arviointi = Some(List(vstArviointi()))
    )
  }

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(laajuus: LaajuusOpintopisteissä = LaajuusOpintopisteissä(15)) = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
      koulutusmoduuli = VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenKoulutusmoduuli(
        Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojenkokonaisuudensuoritus", koodistoUri = "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus"),
        Some(laajuus)
      ),
      arviointi = Some(List(vstArviointi())),
      osasuoritukset = Some(List(
        vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso,
        vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot
      ))
    )
  }

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso() = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso(
      koulutusmoduuli = vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
      arviointi = Some(List(vstArviointi()))
    )
  }

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot() = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataidot(
      koulutusmoduuli = vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
      arviointi = Some(List(vstArviointi()))
    )
  }

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(laajuus: LaajuusOpintopisteissä = LaajuusOpintopisteissä(4)) = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
      koulutusmoduuli = VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenKoulutusmoduuli(
        Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenvalinnaistensuoritus", koodistoUri = "vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus"),
        Some(laajuus)
      ),
      arviointi = Some(List(vstArviointi())),
      osasuoritukset = Some(List(vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus))
    )
  }

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus() = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus(
      koulutusmoduuli = vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus,
      arviointi = Some(List(vstArviointi()))
    )
  }

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus() = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOpintojenOsasuoritus(
      tunniste = PaikallinenKoodi("TALO", "Kiinteistöhuollon perusteita"),
      kuvaus = "Omakotitaloasujan kiinteistöhuollon perusteet",
      laajuus = Some(LaajuusOpintopisteissä(4))
    )
  }

  // Lukutaitokoulutuksen rakenteen osia

  def vapaanSivistystyönLukutaitokoulutuksenVuorovaikutustilanteissaToimimisenSuoritus(laajuus: LaajuusOpintopisteissä = LaajuusOpintopisteissä(20)) = {
    VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus(
      koulutusmoduuli = VapaanSivistystyönLukutaidonKokonaisuus(
        Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutuksenvuorovaikutustilannekokonaisuudensuoritus", koodistoUri = "vstlukutaitokoulutuksenkokonaisuus"),
        Some(laajuus)
      ),
      arviointi = Some(List(lukutaitokoulutuksenArviointi("Hyväksytty")))
    )
  }

  def vapaanSivistystyönLukutaitokoulutuksenTekstienLukeminenJaTulkitseminenSuoritus(laajuus: LaajuusOpintopisteissä = LaajuusOpintopisteissä(20)) = {
    VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus(
      koulutusmoduuli = VapaanSivistystyönLukutaidonKokonaisuus(
        Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutuksentekstienlukemisenkokonaisuudensuoritus", koodistoUri = "vstlukutaitokoulutuksenkokonaisuus"),
        Some(laajuus)
      ),
      arviointi = Some(List(lukutaitokoulutuksenArviointi("Hyväksytty")))
    )
  }

  def vapaanSivistystyönLukutaitokoulutuksenTekstienKirjoittaminenJaTuottaminenToimimisenSuoritus(laajuus: LaajuusOpintopisteissä = LaajuusOpintopisteissä(20)) = {
    VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus(
      koulutusmoduuli = VapaanSivistystyönLukutaidonKokonaisuus(
        Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutuksentekstienkirjoittamisenkokonaisuudensuoritus", koodistoUri = "vstlukutaitokoulutuksenkokonaisuus"),
        Some(laajuus)
      ),
      arviointi = Some(List(lukutaitokoulutuksenArviointi("Hyväksytty")))
    )
  }

  def vapaanSivistystyönLukutaitokoulutuksenNumeeristenTaitojenSuoritus(laajuus: LaajuusOpintopisteissä = LaajuusOpintopisteissä(20)) = {
    VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus(
      koulutusmoduuli = VapaanSivistystyönLukutaidonKokonaisuus(
        Koodistokoodiviite(koodiarvo = "vstlukutaitokoulutuksennumeeristentaitojenkokonaisuudensuoritus", koodistoUri = "vstlukutaitokoulutuksenkokonaisuus"),
        Some(laajuus)
      ),
      arviointi = Some(List(lukutaitokoulutuksenArviointi("Hyväksytty")))
    )
  }

  def vapaanSivistystyönLukutaidonKokonaisuus(tunniste: Koodistokoodiviite, laajuus: Option[LaajuusOpintopisteissä]) = {
    VapaanSivistystyönLukutaidonKokonaisuus(
      tunniste =  tunniste,
      laajuus = laajuus
    )
  }

  // Vapaatavoitteisten koulutusten rakenteiden osia

  def vapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus() = {
    VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
      koulutusmoduuli = vapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus(
        LocalizedString.finnish("Sienestämisen kokonaisuus"),
        PaikallinenKoodi.apply("SK-K", "Sienestämisen kokonaisuus"),
        LaajuusOpintopisteissä(5)
      ),
      arviointi = Some(List(vapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi())),
      osasuoritukset = Some(List(vapaanSivistystyönVapaatavoitteisenKoulutuksenOsaSuorituksenOsaSuorituksenSuoritus))
    )
  }

  def vapaanSivistystyönVapaatavoitteisenKoulutuksenOsaSuorituksenOsaSuorituksenSuoritus() = {
    VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(
      koulutusmoduuli = vapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus(
        LocalizedString.finnish("Sienien tunnistaminen 1"),
        PaikallinenKoodi.apply("ST1", "Sienien tunnistaminen 1"),
        LaajuusOpintopisteissä(5)
      ),
      arviointi = Some(List(vapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi()))
    )
  }

  def vapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi(arvosana:String = "2", päivä: LocalDate = date(2021, 10, 30)): VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi = {
    VapaanSivistystyöVapaatavoitteisenKoulutuksenArviointi(Koodistokoodiviite(arvosana, "arviointiasteikkovstvapaatavoitteinen"), päivä)
  }

  def vapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus(kuvaus: LocalizedString, tunniste: PaikallinenKoodi, laajuus: LaajuusOpintopisteissä) = {
    VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuoritus(
      kuvaus = kuvaus,
      tunniste = tunniste,
      laajuus = laajuus
    )
  }
}
