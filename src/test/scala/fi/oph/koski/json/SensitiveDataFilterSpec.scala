package fi.oph.koski.json

import fi.oph.koski.TestEnvironment
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.PerusopetusExampleData.{oppiaine, suoritus}
import fi.oph.koski.documentation._
import fi.oph.koski.koskiuser.{KäyttöoikeusRepository, MockUsers}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate
import scala.reflect.runtime.universe.TypeTag

class SensitiveDataFilterSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private val application = KoskiApplication.apply
  private val käyttöoikeusRepository: KäyttöoikeusRepository = application.käyttöoikeusRepository

  "Käyttäjä jolla ei ole luottamuksellisia oikeuksia ei näe mitään arkaluontoisia tietoja" in {
    implicit val eiLuottumuksellisiaOikeuksia = MockUsers.evira.toKoskiSpecificSession(käyttöoikeusRepository)
    roundtrip[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot](aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot) should equal(AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(None,None,None,None,None,false,None,None,None,None,None))
    roundtrip[AmmatillisenOpiskeluoikeudenLisätiedot](ammatillisenOpiskeluoikeudenLisätiedot) should equal(AmmatillisenOpiskeluoikeudenLisätiedot(false,None,None,None,None,None,None,None,None,None,None,None,false,None,false))
    roundtrip[DIAOpiskeluoikeudenLisätiedot](diaOpiskeluoikeudenLisätiedot).pidennettyPäättymispäivä should equal(false)
    roundtrip[EsiopetuksenOpiskeluoikeudenLisätiedot](esiopetuksenOpiskeluoikeudenLisätiedot) should equal(EsiopetuksenOpiskeluoikeudenLisätiedot(None,None,None))
    roundtrip[LukionOpiskeluoikeudenLisätiedot](lukionOpiskeluoikeudenLisätiedot) should equal(LukionOpiskeluoikeudenLisätiedot(false,false,None,false,None,None,false,None))
    roundtrip[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot](lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot) should equal(LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(false,false,None,false,None))
    roundtrip[PerusopetuksenOpiskeluoikeudenLisätiedot](perusopetuksenOpiskeluoikeudenLisätiedot) should equal(PerusopetuksenOpiskeluoikeudenLisätiedot(None,false,None,None,None,None,None,None,None,None,None,None,None,false,None,None,None,None,None,None,None))
    roundtrip[PerusopetuksenVuosiluokanSuoritus](perusopetuksenVuosiluokanSuoritus).jääLuokalle should equal(false)
    roundtrip[SanallinenPerusopetuksenOppiaineenArviointi](sanallinenPerusopetuksenOppiaineenArviointi).kuvaus should equal(None)
    roundtrip[PerusopetuksenKäyttäytymisenArviointi](perusopetuksenKäyttäytymisenArviointi).kuvaus should equal(None)
    roundtrip[PerusopetuksenLisäopetuksenOppiaineenSuoritus](perusopetuksenLisäopetuksenOppiaineenSuoritus).yksilöllistettyOppimäärä should equal(false)
    roundtrip[NuortenPerusopetuksenOppiaineenSuoritus](nuortenPerusopetuksenOppiaineenSuoritus).yksilöllistettyOppimäärä should equal(false)
    roundtrip[NuortenPerusopetuksenUskonto](nuortenUskonto).uskonnonOppimäärä should equal(None)
    roundtrip[AikuistenPerusopetuksenUskonto](aikuistenUskonto).uskonnonOppimäärä should equal(None)
    roundtrip[LukionUskonto2015](lukionUskonto).uskonnonOppimäärä should equal(None)
  }

  "Käyttäjä jolla on kaikki luottamuksellisten tietojen oikeudet näkee kaikki arkaluontoiset tiedot" in {
    implicit val kaikkiOikeudet = MockUsers.paakayttaja.toKoskiSpecificSession(käyttöoikeusRepository)
    roundtrip[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot](aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot) should equal(aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot)
    roundtrip[AmmatillisenOpiskeluoikeudenLisätiedot](ammatillisenOpiskeluoikeudenLisätiedot) should equal(ammatillisenOpiskeluoikeudenLisätiedot)
    roundtrip[DIAOpiskeluoikeudenLisätiedot](diaOpiskeluoikeudenLisätiedot) should equal(diaOpiskeluoikeudenLisätiedot)
    roundtrip[EsiopetuksenOpiskeluoikeudenLisätiedot](esiopetuksenOpiskeluoikeudenLisätiedot) should equal(esiopetuksenOpiskeluoikeudenLisätiedot)
    roundtrip[LukionOpiskeluoikeudenLisätiedot](lukionOpiskeluoikeudenLisätiedot) should equal(lukionOpiskeluoikeudenLisätiedot)
    roundtrip[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot](lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot) should equal(lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot)
    roundtrip[PerusopetuksenOpiskeluoikeudenLisätiedot](perusopetuksenOpiskeluoikeudenLisätiedot) should equal(perusopetuksenOpiskeluoikeudenLisätiedot)
    roundtrip[PerusopetuksenVuosiluokanSuoritus](perusopetuksenVuosiluokanSuoritus) should equal(perusopetuksenVuosiluokanSuoritus)
    roundtrip[SanallinenPerusopetuksenOppiaineenArviointi](sanallinenPerusopetuksenOppiaineenArviointi) should equal(sanallinenPerusopetuksenOppiaineenArviointi)
    roundtrip[PerusopetuksenKäyttäytymisenArviointi](perusopetuksenKäyttäytymisenArviointi) should equal(perusopetuksenKäyttäytymisenArviointi)
    roundtrip[PerusopetuksenLisäopetuksenOppiaineenSuoritus](perusopetuksenLisäopetuksenOppiaineenSuoritus) should equal(perusopetuksenLisäopetuksenOppiaineenSuoritus)
    roundtrip[NuortenPerusopetuksenOppiaineenSuoritus](nuortenPerusopetuksenOppiaineenSuoritus) should equal(nuortenPerusopetuksenOppiaineenSuoritus)
    roundtrip[NuortenPerusopetuksenUskonto](nuortenUskonto) should equal(nuortenUskonto)
    roundtrip[AikuistenPerusopetuksenUskonto](aikuistenUskonto) should equal(aikuistenUskonto)
    roundtrip[LukionUskonto2015](lukionUskonto) should equal(lukionUskonto)
  }

  "Käyttäjä jolla on uusi kaikkiin luottamuksellisiin tietoihin oikeuttava käyttöoikeus näkee kaikki arkaluontoiset tiedot" in {
    implicit val kaikkiOikeudet = MockUsers.luovutuspalveluKäyttäjäArkaluontoinen.toKoskiSpecificSession(käyttöoikeusRepository)
    roundtrip[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot](aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot) should equal(aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot)
    roundtrip[AmmatillisenOpiskeluoikeudenLisätiedot](ammatillisenOpiskeluoikeudenLisätiedot) should equal(ammatillisenOpiskeluoikeudenLisätiedot)
    roundtrip[DIAOpiskeluoikeudenLisätiedot](diaOpiskeluoikeudenLisätiedot) should equal(diaOpiskeluoikeudenLisätiedot)
    roundtrip[EsiopetuksenOpiskeluoikeudenLisätiedot](esiopetuksenOpiskeluoikeudenLisätiedot) should equal(esiopetuksenOpiskeluoikeudenLisätiedot)
    roundtrip[LukionOpiskeluoikeudenLisätiedot](lukionOpiskeluoikeudenLisätiedot) should equal(lukionOpiskeluoikeudenLisätiedot)
    roundtrip[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot](lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot) should equal(lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot)
    roundtrip[PerusopetuksenOpiskeluoikeudenLisätiedot](perusopetuksenOpiskeluoikeudenLisätiedot) should equal(perusopetuksenOpiskeluoikeudenLisätiedot)
    roundtrip[PerusopetuksenVuosiluokanSuoritus](perusopetuksenVuosiluokanSuoritus) should equal(perusopetuksenVuosiluokanSuoritus)
    roundtrip[SanallinenPerusopetuksenOppiaineenArviointi](sanallinenPerusopetuksenOppiaineenArviointi) should equal(sanallinenPerusopetuksenOppiaineenArviointi)
    roundtrip[PerusopetuksenKäyttäytymisenArviointi](perusopetuksenKäyttäytymisenArviointi) should equal(perusopetuksenKäyttäytymisenArviointi)
    roundtrip[PerusopetuksenLisäopetuksenOppiaineenSuoritus](perusopetuksenLisäopetuksenOppiaineenSuoritus) should equal(perusopetuksenLisäopetuksenOppiaineenSuoritus)
    roundtrip[NuortenPerusopetuksenOppiaineenSuoritus](nuortenPerusopetuksenOppiaineenSuoritus) should equal(nuortenPerusopetuksenOppiaineenSuoritus)
    roundtrip[NuortenPerusopetuksenUskonto](nuortenUskonto) should equal(nuortenUskonto)
    roundtrip[AikuistenPerusopetuksenUskonto](aikuistenUskonto) should equal(aikuistenUskonto)
    roundtrip[LukionUskonto2015](lukionUskonto) should equal(lukionUskonto)
  }

  "Käyttäjä jolla on suppeat luottamuksellisten tietojen oikeudet näkee suppeiden oikeuksien mukaiset arkaluontoiset tiedot" in {
    implicit val suppeatOikeudet = MockUsers.kelaSuppeatOikeudet.toKoskiSpecificSession(käyttöoikeusRepository)
    roundtrip[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot](aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot) should equal(AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(None,None,None,None,None,false,None,None,None,Some(aikajakso),aikajaksot))
    roundtrip[AmmatillisenOpiskeluoikeudenLisätiedot](ammatillisenOpiskeluoikeudenLisätiedot) should equal(AmmatillisenOpiskeluoikeudenLisätiedot(true,None,aikajaksot,aikajaksot,None,None,None,None,None,None,None,None,false,aikajaksot,false))
    roundtrip[DIAOpiskeluoikeudenLisätiedot](diaOpiskeluoikeudenLisätiedot).pidennettyPäättymispäivä should equal(false)
    roundtrip[EsiopetuksenOpiskeluoikeudenLisätiedot](esiopetuksenOpiskeluoikeudenLisätiedot) should equal(EsiopetuksenOpiskeluoikeudenLisätiedot(None, None, None))
    roundtrip[LukionOpiskeluoikeudenLisätiedot](lukionOpiskeluoikeudenLisätiedot) should equal(LukionOpiskeluoikeudenLisätiedot(false,false,None,true,None,None,true,aikajaksot))
    roundtrip[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot](lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot) should equal(LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(false,false,None,true,aikajaksot))
    roundtrip[PerusopetuksenOpiskeluoikeudenLisätiedot](perusopetuksenOpiskeluoikeudenLisätiedot) should equal(PerusopetuksenOpiskeluoikeudenLisätiedot(None,false,None,None,None,None,None,None,None,None,None,None,None,false,None,None,None,None,Some(aikajakso),aikajaksot,None))
    roundtrip[PerusopetuksenVuosiluokanSuoritus](perusopetuksenVuosiluokanSuoritus).jääLuokalle should equal(false)
    roundtrip[SanallinenPerusopetuksenOppiaineenArviointi](sanallinenPerusopetuksenOppiaineenArviointi).kuvaus should equal(None)
    roundtrip[PerusopetuksenKäyttäytymisenArviointi](perusopetuksenKäyttäytymisenArviointi).kuvaus should equal(None)
    roundtrip[PerusopetuksenLisäopetuksenOppiaineenSuoritus](perusopetuksenLisäopetuksenOppiaineenSuoritus).yksilöllistettyOppimäärä should equal(false)
    roundtrip[NuortenPerusopetuksenOppiaineenSuoritus](nuortenPerusopetuksenOppiaineenSuoritus).yksilöllistettyOppimäärä should equal(false)
    roundtrip[NuortenPerusopetuksenUskonto](nuortenUskonto).uskonnonOppimäärä should equal(None)
    roundtrip[AikuistenPerusopetuksenUskonto](aikuistenUskonto).uskonnonOppimäärä should equal(None)
    roundtrip[LukionUskonto2015](lukionUskonto).uskonnonOppimäärä should equal(None)
  }

  "Käyttäjä jolla on laajat luottamuksellisten tietojen oikeudet näkee laajojen oikeuksien mukaiset arkaluontoiset tiedot" in {
    implicit val laajatOikeudet = MockUsers.kelaLaajatOikeudet.toKoskiSpecificSession(käyttöoikeusRepository)
    roundtrip[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot](aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot) should equal(aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot.copy(vuosiluokkiinSitoutumatonOpetus = false, vammainen = None, vaikeastiVammainen = None, tukimuodot = None))
    roundtrip[AmmatillisenOpiskeluoikeudenLisätiedot](ammatillisenOpiskeluoikeudenLisätiedot) should equal(ammatillisenOpiskeluoikeudenLisätiedot.copy(vaikeastiVammainen = None, vammainenJaAvustaja = None))
    roundtrip[DIAOpiskeluoikeudenLisätiedot](diaOpiskeluoikeudenLisätiedot).pidennettyPäättymispäivä should equal(false)
    roundtrip[EsiopetuksenOpiskeluoikeudenLisätiedot](esiopetuksenOpiskeluoikeudenLisätiedot) should equal(esiopetuksenOpiskeluoikeudenLisätiedot.copy(pidennettyOppivelvollisuus = None, majoitusetu = None, kuljetusetu = None, tukimuodot = None, erityisenTuenPäätös = Some(erityisenTuenPäätös.copy(toteutuspaikka = None)), erityisenTuenPäätökset = Some(List(erityisenTuenPäätös.copy(toteutuspaikka = None)))))
    roundtrip[LukionOpiskeluoikeudenLisätiedot](lukionOpiskeluoikeudenLisätiedot) should equal(lukionOpiskeluoikeudenLisätiedot.copy(pidennettyPäättymispäivä = false))
    roundtrip[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot](lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot) should equal(lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot.copy(pidennettyPäättymispäivä = false))
    roundtrip[PerusopetuksenOpiskeluoikeudenLisätiedot](perusopetuksenOpiskeluoikeudenLisätiedot) should equal(PerusopetuksenOpiskeluoikeudenLisätiedot(None,false,None,None,Some(erityisenTuenPäätös.copy(toteutuspaikka = None)),Some(List(erityisenTuenPäätös.copy(toteutuspaikka = None))),Some(tehostetunTuenPäätös),Some(List(tehostetunTuenPäätös)),Some(aikajakso),None,None,None,None,false,None,None,None,None,Some(aikajakso),aikajaksot,aikajaksot))
    roundtrip[PerusopetuksenVuosiluokanSuoritus](perusopetuksenVuosiluokanSuoritus).jääLuokalle should equal(false)
    roundtrip[SanallinenPerusopetuksenOppiaineenArviointi](sanallinenPerusopetuksenOppiaineenArviointi).kuvaus should equal(None)
    roundtrip[PerusopetuksenKäyttäytymisenArviointi](perusopetuksenKäyttäytymisenArviointi).kuvaus should equal(None)
    roundtrip[PerusopetuksenLisäopetuksenOppiaineenSuoritus](perusopetuksenLisäopetuksenOppiaineenSuoritus).yksilöllistettyOppimäärä should equal(false)
    roundtrip[NuortenPerusopetuksenOppiaineenSuoritus](nuortenPerusopetuksenOppiaineenSuoritus).yksilöllistettyOppimäärä should equal(false)
    roundtrip[NuortenPerusopetuksenUskonto](nuortenUskonto).uskonnonOppimäärä should equal(None)
    roundtrip[AikuistenPerusopetuksenUskonto](aikuistenUskonto).uskonnonOppimäärä should equal(None)
    roundtrip[LukionUskonto2015](lukionUskonto).uskonnonOppimäärä should equal(None)
  }

  private val pvm = LocalDate.of(2001, 1, 1)
  private val aikajakso = Aikajakso(pvm, None)
  private lazy val aikajaksot = Some(List(aikajakso))

  private val tukimuodot = Some(List(Koodistokoodiviite("1", "perusopetuksentukimuoto")))
  private val aikuistenPerusopetuksenOpiskeluoikeudenLisätiedot = AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot(
    tukimuodot = tukimuodot,
    tehostetunTuenPäätökset = aikajaksot,
    tehostetunTuenPäätös = Some(aikajakso),
    vuosiluokkiinSitoutumatonOpetus = true,
    vammainen = aikajaksot,
    vaikeastiVammainen = aikajaksot,
    oikeusMaksuttomaanAsuntolapaikkaan = Some(aikajakso),
    sisäoppilaitosmainenMajoitus = aikajaksot
  )

  private val ammatillisenOpiskeluoikeudenLisätiedot = AmmatillisenOpiskeluoikeudenLisätiedot(
    oikeusMaksuttomaanAsuntolapaikkaan = true,
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

  private val lukionOpiskeluoikeudenLisätiedot = LukionOpiskeluoikeudenLisätiedot(
    pidennettyPäättymispäivä = true,
    yksityisopiskelija = true,
    oikeusMaksuttomaanAsuntolapaikkaan = true,
    sisäoppilaitosmainenMajoitus = aikajaksot
  )

  private val lukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot = LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot(
    pidennettyPäättymispäivä = true,
    oikeusMaksuttomaanAsuntolapaikkaan = true,
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
}
