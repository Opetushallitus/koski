package fi.oph.koski.json

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.{KoskiHttpSpec, TestEnvironment}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenExampleData.{k3, lisätietoOsaamistavoitteet, yhteisenTutkinnonOsanSuoritus}
import fi.oph.koski.documentation.PerusopetusExampleData.{oppiaine, suoritus}
import fi.oph.koski.documentation._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.{KäyttöoikeusRepository, MockUsers}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import scala.reflect.runtime.universe.TypeTag

class SensitiveAndRedundantDataFilterSpec extends AnyFreeSpec with TestEnvironment with Matchers with OpiskeluoikeusTestMethods with KoskiHttpSpec {
  private val application = KoskiApplication.apply
  private val käyttöoikeusRepository: KäyttöoikeusRepository = application.käyttöoikeusRepository

  val ammatillinenPiilotettavaLisätieto = yhteisenTutkinnonOsanSuoritus("101054", "Matemaattis-luonnontieteellinen osaaminen", k3, 9).copy(
    lisätiedot = Some(List(lisätietoOsaamistavoitteet)))

  "Käyttäjä jolla ei ole luottamuksellisia oikeuksia ei näe mitään arkaluontoisia tietoja" in {
    implicit val eiLuottumuksellisiaOikeuksia = MockUsers.viranomainenGlobaaliKatselija.toKoskiSpecificSession(käyttöoikeusRepository)
    roundtrip[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot](aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot) should equal(AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot())
    roundtrip[AmmatillisenOpiskeluoikeudenLisätiedot](ammatillisenOpiskeluoikeudenLisätiedot) should equal(AmmatillisenOpiskeluoikeudenLisätiedot(hojks = None))
    roundtrip[DIAOpiskeluoikeudenLisätiedot](diaOpiskeluoikeudenLisätiedot).pidennettyPäättymispäivä should equal(true)
    roundtrip[EsiopetuksenOpiskeluoikeudenLisätiedot](esiopetuksenOpiskeluoikeudenLisätiedot) should equal(EsiopetuksenOpiskeluoikeudenLisätiedot(majoitusetu = Some(aikajakso)))
    roundtrip[LukionOpiskeluoikeudenLisätiedot](lukionOpiskeluoikeudenLisätiedot) should equal(LukionOpiskeluoikeudenLisätiedot(pidennettyPäättymispäivä=true))
    roundtrip[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot](lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot) should equal(LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(pidennettyPäättymispäivä=true))
    roundtrip[PerusopetuksenOpiskeluoikeudenLisätiedot](perusopetuksenOpiskeluoikeudenLisätiedot) should equal(PerusopetuksenOpiskeluoikeudenLisätiedot(majoitusetu = Some(aikajakso)))
    roundtrip[PerusopetuksenVuosiluokanSuoritus](perusopetuksenVuosiluokanSuoritus).jääLuokalle should equal(false)
    roundtrip[SanallinenPerusopetuksenOppiaineenArviointi](sanallinenPerusopetuksenOppiaineenArviointi).kuvaus should equal(None)
    roundtrip[PerusopetuksenKäyttäytymisenArviointi](perusopetuksenKäyttäytymisenArviointi).kuvaus should equal(None)
    roundtrip[PerusopetuksenLisäopetuksenOppiaineenSuoritus](perusopetuksenLisäopetuksenOppiaineenSuoritus).yksilöllistettyOppimäärä should equal(false)
    roundtrip[NuortenPerusopetuksenOppiaineenSuoritus](nuortenPerusopetuksenOppiaineenSuoritus).yksilöllistettyOppimäärä should equal(false)
    roundtrip[NuortenPerusopetuksenUskonto](nuortenUskonto).uskonnonOppimäärä should equal(None)
    roundtrip[AikuistenPerusopetuksenUskonto](aikuistenUskonto).uskonnonOppimäärä should equal(None)
    roundtrip[LukionUskonto2015](lukionUskonto).uskonnonOppimäärä should equal(None)

    val ammatillinenJossaVoisiOllaMukautettujaArvosanoja = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ammatillisenOsittainenRapsa.oid, MockUsers.viranomainenGlobaaliKatselija)
    existsLisätietoMukautetustaArvioinnista(ammatillinenJossaVoisiOllaMukautettujaArvosanoja) should equal (false)

    val perusopetuksenOpiskeluoikeusJossaVoisiOllaToimintaAlueenSuoritus = lastOpiskeluoikeus(KoskiSpecificMockOppijat.toimintaAlueittainOpiskelija.oid, MockUsers.viranomainenGlobaaliKatselija)
    perusopetuksenOpiskeluoikeusJossaVoisiOllaToimintaAlueenSuoritus.suoritukset.exists(_.osasuoritusLista.exists{
      case _: PerusopetuksenToiminta_AlueenSuoritus => true
      case _ => false
    }) should equal (false)

    // Tarkistetaan, että vain toiminta-alueen osasuoritukset jää pois
    val perusopetuksenOpiskeluoikeusMuillaOsasuorituksilla = getOpiskeluoikeudet(KoskiSpecificMockOppijat.koululainen.oid, MockUsers.viranomainenGlobaaliKatselija).find(
      _.tyyppi.koodiarvo == OpiskeluoikeudenTyyppi.perusopetus.koodiarvo
    ).get
    perusopetuksenOpiskeluoikeusMuillaOsasuorituksilla.suoritukset.head.osasuoritusLista.length should equal (18)
  }

  "Käyttäjä jolla on kaikki luottamuksellisten tietojen oikeudet näkee kaikki arkaluontoiset tiedot" in {
    implicit val kaikkiOikeudet = MockUsers.paakayttaja.toKoskiSpecificSession(käyttöoikeusRepository)
    roundtrip[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot](aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot) should equal(AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(sisäoppilaitosmainenMajoitus = aikajaksot))
    roundtrip[AmmatillisenOpiskeluoikeudenLisätiedot](ammatillisenOpiskeluoikeudenLisätiedot) should equal(ammatillisenOpiskeluoikeudenLisätiedot.copy(oikeusMaksuttomaanAsuntolapaikkaan = None))
    roundtrip[DIAOpiskeluoikeudenLisätiedot](diaOpiskeluoikeudenLisätiedot) should equal(diaOpiskeluoikeudenLisätiedot)
    roundtrip[EsiopetuksenOpiskeluoikeudenLisätiedot](esiopetuksenOpiskeluoikeudenLisätiedot) should equal(esiopetuksenOpiskeluoikeudenLisätiedotIlmanRedundanttejaKenttiä)
    roundtrip[LukionOpiskeluoikeudenLisätiedot](lukionOpiskeluoikeudenLisätiedot) should equal(lukionOpiskeluoikeudenLisätiedotIlmanRedundanttejaKenttiä.copy(oikeusMaksuttomaanAsuntolapaikkaan = None))
    roundtrip[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot](lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot) should equal(lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot.copy(oikeusMaksuttomaanAsuntolapaikkaan = None))
    roundtrip[PerusopetuksenOpiskeluoikeudenLisätiedot](perusopetuksenOpiskeluoikeudenLisätiedot) should equal(perusopetuksenOpiskeluoikeudenLisätiedotIlmanRedundanttejaKenttiä)
    roundtrip[PerusopetuksenVuosiluokanSuoritus](perusopetuksenVuosiluokanSuoritus) should equal(perusopetuksenVuosiluokanSuoritus)
    roundtrip[SanallinenPerusopetuksenOppiaineenArviointi](sanallinenPerusopetuksenOppiaineenArviointi) should equal(sanallinenPerusopetuksenOppiaineenArviointi)
    roundtrip[PerusopetuksenKäyttäytymisenArviointi](perusopetuksenKäyttäytymisenArviointi) should equal(perusopetuksenKäyttäytymisenArviointi)
    roundtrip[PerusopetuksenLisäopetuksenOppiaineenSuoritus](perusopetuksenLisäopetuksenOppiaineenSuoritus) should equal(perusopetuksenLisäopetuksenOppiaineenSuoritus)
    roundtrip[NuortenPerusopetuksenOppiaineenSuoritus](nuortenPerusopetuksenOppiaineenSuoritus) should equal(nuortenPerusopetuksenOppiaineenSuoritus)
    roundtrip[NuortenPerusopetuksenUskonto](nuortenUskonto) should equal(nuortenUskonto)
    roundtrip[AikuistenPerusopetuksenUskonto](aikuistenUskonto) should equal(aikuistenUskonto)
    roundtrip[LukionUskonto2015](lukionUskonto) should equal(lukionUskonto)

    val ammatillinenJossaVoisiOllaMukautettujaArvosanoja = lastOpiskeluoikeus(KoskiSpecificMockOppijat.ammatillisenOsittainenRapsa.oid, MockUsers.paakayttaja)
    existsLisätietoMukautetustaArvioinnista(ammatillinenJossaVoisiOllaMukautettujaArvosanoja) should equal (true)

    val perusopetuksenOpiskeluoikeusJossaVoisiOllaToimintaAlueenSuoritus = lastOpiskeluoikeus(KoskiSpecificMockOppijat.toimintaAlueittainOpiskelija.oid, MockUsers.paakayttaja)
    perusopetuksenOpiskeluoikeusJossaVoisiOllaToimintaAlueenSuoritus.suoritukset.exists(_.osasuoritusLista.exists{
      case _: PerusopetuksenToiminta_AlueenSuoritus => true
      case _ => false
    }) should equal (true)
  }

  "Käyttäjä jolla on uusi kaikkiin luottamuksellisiin tietoihin oikeuttava käyttöoikeus näkee kaikki arkaluontoiset tiedot" in {
    implicit val kaikkiOikeudet = MockUsers.luovutuspalveluKäyttäjäArkaluontoinen.toKoskiSpecificSession(käyttöoikeusRepository)
    roundtrip[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot](aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot) should equal(AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(sisäoppilaitosmainenMajoitus = aikajaksot))
    roundtrip[AmmatillisenOpiskeluoikeudenLisätiedot](ammatillisenOpiskeluoikeudenLisätiedot) should equal(ammatillisenOpiskeluoikeudenLisätiedot.copy(oikeusMaksuttomaanAsuntolapaikkaan = None))
    roundtrip[DIAOpiskeluoikeudenLisätiedot](diaOpiskeluoikeudenLisätiedot) should equal(diaOpiskeluoikeudenLisätiedot)
    roundtrip[EsiopetuksenOpiskeluoikeudenLisätiedot](esiopetuksenOpiskeluoikeudenLisätiedot) should equal(esiopetuksenOpiskeluoikeudenLisätiedotIlmanRedundanttejaKenttiä)
    roundtrip[LukionOpiskeluoikeudenLisätiedot](lukionOpiskeluoikeudenLisätiedot) should equal(lukionOpiskeluoikeudenLisätiedotIlmanRedundanttejaKenttiä.copy(oikeusMaksuttomaanAsuntolapaikkaan = None))
    roundtrip[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot](lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot) should equal(lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot.copy(oikeusMaksuttomaanAsuntolapaikkaan = None))
    roundtrip[PerusopetuksenOpiskeluoikeudenLisätiedot](perusopetuksenOpiskeluoikeudenLisätiedot) should equal(perusopetuksenOpiskeluoikeudenLisätiedotIlmanRedundanttejaKenttiä)
    roundtrip[PerusopetuksenVuosiluokanSuoritus](perusopetuksenVuosiluokanSuoritus) should equal(perusopetuksenVuosiluokanSuoritus)
    roundtrip[SanallinenPerusopetuksenOppiaineenArviointi](sanallinenPerusopetuksenOppiaineenArviointi) should equal(sanallinenPerusopetuksenOppiaineenArviointi)
    roundtrip[PerusopetuksenKäyttäytymisenArviointi](perusopetuksenKäyttäytymisenArviointi) should equal(perusopetuksenKäyttäytymisenArviointi)
    roundtrip[PerusopetuksenLisäopetuksenOppiaineenSuoritus](perusopetuksenLisäopetuksenOppiaineenSuoritus) should equal(perusopetuksenLisäopetuksenOppiaineenSuoritus)
    roundtrip[NuortenPerusopetuksenOppiaineenSuoritus](nuortenPerusopetuksenOppiaineenSuoritus) should equal(nuortenPerusopetuksenOppiaineenSuoritus)
    roundtrip[NuortenPerusopetuksenUskonto](nuortenUskonto) should equal(nuortenUskonto)
    roundtrip[AikuistenPerusopetuksenUskonto](aikuistenUskonto) should equal(aikuistenUskonto)
    roundtrip[LukionUskonto2015](lukionUskonto) should equal(lukionUskonto)
  }

  private val pvm = LocalDate.of(2001, 1, 1)
  private val aikajakso = Aikajakso(pvm, None)
  private lazy val aikajaksot = Some(List(aikajakso))

  private val tukimuodot = Some(List(Koodistokoodiviite("1", "perusopetuksentukimuoto")))
  private val aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot = AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(
    tukimuodot = tukimuodot,
    tehostetunTuenPäätökset = aikajaksot,
    tehostetunTuenPäätös = Some(aikajakso),
    vuosiluokkiinSitoutumatonOpetus = Some(true),
    vammainen = aikajaksot,
    vaikeastiVammainen = aikajaksot,
    oikeusMaksuttomaanAsuntolapaikkaan = Some(aikajakso),
    sisäoppilaitosmainenMajoitus = aikajaksot
  )

  private val ammatillisenOpiskeluoikeudenLisätiedot = AmmatillisenOpiskeluoikeudenLisätiedot(
    oikeusMaksuttomaanAsuntolapaikkaan = Some(true),
    sisäoppilaitosmainenMajoitus = aikajaksot,
    vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = aikajaksot,
    erityinenTuki = aikajaksot,
    vaativanErityisenTuenErityinenTehtävä = aikajaksot,
    hojks = Some(Hojks(Koodistokoodiviite("2", "opetusryhma"))),
    vaikeastiVammainen = aikajaksot,
    vammainenJaAvustaja = aikajaksot,
    opiskeluvalmiuksiaTukevatOpinnot = Some(List(OpiskeluvalmiuksiaTukevienOpintojenJakso(pvm, pvm, "tukevia opintoja"))),
    vankilaopetuksessa = aikajaksot
  )

  private val diaOpiskeluoikeudenLisätiedot = DIAOpiskeluoikeudenLisätiedot(
    pidennettyPäättymispäivä = true
  )

  private val erityisenTuenPäätös = ErityisenTuenPäätös(alku = Some(pvm), loppu = None, opiskeleeToimintaAlueittain = true, erityisryhmässä = None, toteutuspaikka = Some(Koodistokoodiviite("1", "erityisopetuksentoteutuspaikka")))
  private val tehostetunTuenPäätös = TehostetunTuenPäätös(alku = pvm, loppu = None, None)
  private val esiopetuksenOpiskeluoikeudenLisätiedot = EsiopetuksenOpiskeluoikeudenLisätiedot(
    pidennettyOppivelvollisuus = Some(aikajakso),
    erityisenTuenPäätös = Some(erityisenTuenPäätös),
    erityisenTuenPäätökset = Some(List(erityisenTuenPäätös)),
    koulukoti = Some(List(aikajakso)),
    majoitusetu = Some(aikajakso),
    kuljetusetu = Some(aikajakso),
    tukimuodot = tukimuodot
  )
  private val esiopetuksenOpiskeluoikeudenLisätiedotIlmanRedundanttejaKenttiä = esiopetuksenOpiskeluoikeudenLisätiedot.copy(
    erityisenTuenPäätös = Some(esiopetuksenOpiskeluoikeudenLisätiedot.erityisenTuenPäätös.head.copy(
      tukimuodot = None,
      toteutuspaikka = None
    )),
    erityisenTuenPäätökset = Some(List(esiopetuksenOpiskeluoikeudenLisätiedot.erityisenTuenPäätökset.head.head.copy(
      tukimuodot = None,
      toteutuspaikka = None
    ))),
    tukimuodot = None
  )

  private val lukionOpiskeluoikeudenLisätiedot = LukionOpiskeluoikeudenLisätiedot(
    alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy = Some(LocalizedString.finnish("Testisyy")),
    pidennettyPäättymispäivä = true,
    yksityisopiskelija = Some(true),
    oikeusMaksuttomaanAsuntolapaikkaan = Some(true),
    sisäoppilaitosmainenMajoitus = aikajaksot
  )
  private val lukionOpiskeluoikeudenLisätiedotIlmanRedundanttejaKenttiä = LukionOpiskeluoikeudenLisätiedot(
    alle18vuotiaanAikuistenLukiokoulutuksenAloittamisenSyy = None,
    pidennettyPäättymispäivä = true,
    yksityisopiskelija = None,
    oikeusMaksuttomaanAsuntolapaikkaan = Some(true),
    sisäoppilaitosmainenMajoitus = aikajaksot
  )

  private val lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot = LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(
    pidennettyPäättymispäivä = true,
    oikeusMaksuttomaanAsuntolapaikkaan = Some(true),
    sisäoppilaitosmainenMajoitus = aikajaksot
  )

  private val perusopetuksenOpiskeluoikeudenLisätiedot = PerusopetuksenOpiskeluoikeudenLisätiedot(
    perusopetuksenAloittamistaLykätty = None,
    pidennettyOppivelvollisuus = Some(aikajakso),
    tukimuodot = tukimuodot,
    erityisenTuenPäätös = Some(erityisenTuenPäätös),
    erityisenTuenPäätökset = Some(List(erityisenTuenPäätös)),
    tehostetunTuenPäätös = Some(tehostetunTuenPäätös),
    tehostetunTuenPäätökset = Some(List(tehostetunTuenPäätös)),
    joustavaPerusopetus = Some(aikajakso),
    vuosiluokkiinSitoutumatonOpetus = true,
    vammainen = aikajaksot,
    vaikeastiVammainen = aikajaksot,
    majoitusetu = Some(aikajakso),
    kuljetusetu = Some(aikajakso),
    oikeusMaksuttomaanAsuntolapaikkaan = Some(aikajakso),
    sisäoppilaitosmainenMajoitus = aikajaksot,
    koulukoti = aikajaksot
  )
  private val perusopetuksenOpiskeluoikeudenLisätiedotIlmanRedundanttejaKenttiä = perusopetuksenOpiskeluoikeudenLisätiedot.copy(
    erityisenTuenPäätös = Some(esiopetuksenOpiskeluoikeudenLisätiedot.erityisenTuenPäätös.head.copy(
      tukimuodot = None,
      toteutuspaikka = None
    )),
    erityisenTuenPäätökset = Some(List(esiopetuksenOpiskeluoikeudenLisätiedot.erityisenTuenPäätökset.head.head.copy(
      tukimuodot = None,
      toteutuspaikka = None
    ))),
    oikeusMaksuttomaanAsuntolapaikkaan = None,
    tehostetunTuenPäätös = None,
    tehostetunTuenPäätökset = None,
    tukimuodot = None
  )

  private val perusopetuksenVuosiluokanSuoritus = PerusopetusExampleData.yhdeksännenLuokanSuoritus.copy(jääLuokalle = true)

  private val sanallinenPerusopetuksenOppiaineenArviointi = SanallinenPerusopetuksenOppiaineenArviointi(
    kuvaus = Some("hyvin sujuu")
  )

  private val perusopetuksenKäyttäytymisenArviointi = PerusopetuksenKäyttäytymisenArviointi(
    kuvaus = Some("hyvin sujuu")
  )

  private val perusopetuksenLisäopetuksenOppiaineenSuoritus = ExamplesPerusopetuksenLisaopetus.suoritus(PerusopetusExampleData.oppiaine("HI")).copy(yksilöllistettyOppimäärä = true)
  private val nuortenPerusopetuksenOppiaineenSuoritus = suoritus(oppiaine("MA")).copy(yksilöllistettyOppimäärä = true)
  private val nuortenUskonto = PerusopetusExampleData.uskonto(Some("EV"))
  private val aikuistenUskonto = ExamplesAikuistenPerusopetus.aikuistenUskonto(Some("OR"))
  private val lukionUskonto: LukionUskonto2015 = LukioExampleData.lukionUskonto(uskonto = Some("IS"), diaarinumero = None)

  private def roundtrip[T: TypeTag](input: T)(implicit user: SensitiveDataAllowed): T =
    JsonSerializer.extract[T](JsonSerializer.serialize(input))

  def existsLisätietoMukautetustaArvioinnista(oo: Opiskeluoikeus) = {
    oo.suoritukset.exists(
      _.osasuoritusLista.exists{
        case lisätiedollinen: AmmatillisenTutkinnonOsanLisätiedollinen =>
          lisätiedollinen.lisätiedot.toList.flatten.exists(_.tunniste.koodiarvo == "mukautettu") ||
            lisätiedollinen.osasuoritukset.toList.flatten.exists{
              case lisätiedollinen: AmmatillisenTutkinnonOsanLisätiedollinen =>
                lisätiedollinen.lisätiedot.toList.flatten.exists(_.tunniste.koodiarvo == "mukautettu")
              case _ => false
            }
        case _ => false
      }
    )
  }
}
