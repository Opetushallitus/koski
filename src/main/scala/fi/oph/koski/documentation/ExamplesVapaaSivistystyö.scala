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
    Example("vapaa sivistystyö - maahanmuuttajien kotoutuskoulutus", "Oppija suorittaa maahannmuuttajien kotoutumiskoulutusta kansanopistossa", VapaaSivistystyöExample.maahanmuuttajienkotoutusExample)
  )
}

object VapaaSivistystyöExample {
  lazy val opiskeluoikeusKOPS = VapaanSivistystyönOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
      VapaanSivistystyönOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä)
    )),
    lisätiedot = None,
    oppilaitos = Some(varsinaisSuomenKansanopisto),
    suoritukset = List(suoritusKOPS)
  )

  lazy val opiskeluoikeusKOTO = VapaanSivistystyönOpiskeluoikeus(
    arvioituPäättymispäivä = Some(date(2022, 5, 31)),
    tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
      VapaanSivistystyönOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä)
    )),
    lisätiedot = None,
    oppilaitos = Some(varsinaisSuomenKansanopisto),
    suoritukset = List(suoritusKOTO)
  )

  lazy val suoritusKOPS = OppivelvollisilleSuunnatunVapaanSivistystyönKoulutuksenSuoritus(
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
          vstArviointi("Hylätty", date(2021, 12, 10))
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

  lazy val suoritusKOTO = OppivelvollisilleSuunnatunMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
    toimipiste = OidOrganisaatio(MockOrganisaatiot.itäsuomenYliopisto),
    tyyppi = Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutus", koodistoUri = "suorituksentyyppi"),
    koulutusmoduuli = VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus(perusteenDiaarinumero = Some("OPH-58-2021"), laajuus = Some(LaajuusOpintopisteissä(10))),
    vahvistus = vahvistus(päivä = date(2022, 5, 31)),
    suorituskieli = suomenKieli,
    todistuksellaNäkyvätLisätiedot = None,
    osasuoritukset = Some(List(
      vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus
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
}

object VapaaSivistystyöExampleData {
  val exampleHenkilöKOPS = asUusiOppija(KoskiSpecificMockOppijat.vapaaSivistystyöOppivelvollinen)
  val exampleHenkilöKOTO = asUusiOppija(KoskiSpecificMockOppijat.vapaaSivistystyöMaahanmuuttajienKotoutus)

  lazy val varsinaisSuomenKansanopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.varsinaisSuomenKansanopisto, Some(Koodistokoodiviite("01694", None, "oppilaitosnumero", None)), Some("Varsinais-Suomen kansanopisto"))

  lazy val varsinaisSuomenKansanopistoToimipiste: OidOrganisaatio = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste)

  lazy val tunnustettu: MuuallaSuoritetunVapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen = MuuallaSuoritetunVapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen(
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
      tunnustettu = tunnustettu
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

  def laajuus(arvo: Double): LaajuusOpintopisteissä = LaajuusOpintopisteissä(arvo = arvo, yksikkö = laajuusOpintopisteissä)

  // Maahanmuuttajien kotoutumiskoulutuksen rakenteen osia

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus() = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus(
      koulutusmoduuli = oppivelvollisilleSuunnattuVapaanSivistystyönMaahanmuuttajienKotoutumisKokonaisuus,
      arviointi = Some(List(vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi))
    )
  }

  def oppivelvollisilleSuunnattuVapaanSivistystyönMaahanmuuttajienKotoutumisKokonaisuus() = {
    OppivelvollisilleSuunnattuVapaanSivistystyönMaahanmuuttajienKotoutumisKokonaisuus(
      tunniste =  Koodistokoodiviite(koodiarvo = "vstmaahanmuuttajienkotoutumiskoulutuksenkieliopinnot", koodistoUri = "suorituksentyyppi")
    )
  }

  def vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi() = {
    VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenArviointi(
      kuullunYmmärtämisenTaitotaso = Koodistokoodiviite(koodiarvo = "yli_C1.1", koodistoUri = "arviointiasteikkosuullisenkielitaidonkoetaitotaso"),
      puhumisenTaitotaso = Koodistokoodiviite(koodiarvo = "yli_C1.1", koodistoUri = "arviointiasteikkosuullisenkielitaidonkoetaitotaso"),
      luetunYmmärtämisenTaitotaso = Koodistokoodiviite(koodiarvo = "yli_C1.1", koodistoUri = "arviointiasteikkosuullisenkielitaidonkoetaitotaso"),
      kirjoittamisenTaitotaso = Koodistokoodiviite(koodiarvo = "yli_C1.1", koodistoUri = "arviointiasteikkosuullisenkielitaidonkoetaitotaso"),
      päivä = date(2020, 1, 1)
    )
  }
}
