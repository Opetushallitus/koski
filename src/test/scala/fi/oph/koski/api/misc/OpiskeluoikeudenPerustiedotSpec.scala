package fi.oph.koski.api.misc

import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudet
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.{eerola, koululainen, lukiolainen}
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.schema.LocalizedString.{english, finnish, swedish}
import fi.oph.koski.schema._
import fi.oph.koski.util.Wait
import fi.oph.koski.{DatabaseTestMethods, DirtiesFixtures, KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec

class OpiskeluoikeudenPerustiedotSpec
  extends AnyFreeSpec
    with DirtiesFixtures
    with KoskiHttpSpec
    with DatabaseTestMethods
    with SearchTestMethods
    with MuuAmmatillinenTestMethods[MuunAmmatillisenKoulutuksenSuoritus] {

  override protected def alterFixture(): Unit = createEnglanninkielinenSuoritus

  "Perustiedot" - {
    "Suomenkielinen haku toimii koulutusmoduuleilla jotka on luotu vain englanninkielisellä nimellä" in {
      searchPerustiedot("respect", "fi") should equal(List("Respect: Helping us sustain a harassment free workplace"))
    }

    "Ruotsinkielinen haku toimii koulutusmoduuleilla jotka on luotu vain englanninkielisellä nimellä" in {
      searchPerustiedot("respect", "sv") should equal(List("Respect: Helping us sustain a harassment free workplace"))
    }

    "Suomenkielinen haku toimii koulutusmoduuleilla jotka on luotu vain ruotsinkielisellä nimellä" in {
      searchPerustiedot("respekt", "fi") should equal(List("Respekt: Hjälp oss att upprätthålla en trakasseringsfri arbetsplats"))
    }

    "Ruotsinkielinen haku toimii koulutusmoduuleilla jotka on luotu vain ruotsinkielisellä nimellä" in {
      searchPerustiedot("respekt", "sv") should equal(List("Respekt: Hjälp oss att upprätthålla en trakasseringsfri arbetsplats"))
    }

    "Suomenkielinen haku toimii koulutusmoduuleilla jotka on luotu vain suomenkielisellä nimellä" in {
      searchPerustiedot("kunnioitus", "fi") should equal(List("Kunnioitus: Auta meitä ylläpitämään häirinnätöntä työpaikkaa"))
    }

    "Ruotsinkielinen haku toimii koulutusmoduuleilla jotka on luotu vain suomenkielisellä nimellä" in {
      searchPerustiedot("kunnioitus", "sv") should equal(List("Kunnioitus: Auta meitä ylläpitämään häirinnätöntä työpaikkaa"))
    }
  }

  "Luokka-sarake kun lähdejärjestelmä siirtää vain vanhemman vuosiluokan suorituksen" - {
    // Kaisa Koululaisella on fixtureissa vuosiluokat 7C, 8C ja 9C sekä päättötodistus.
    "näyttää edelleen ylimmän vuosiluokan" in {
      indeksoituLuokka should equal(Some("9C"))

      siirräVainSeitsemännenLuokanSuoritus()

      // OpiskeluoikeusChangeMigrator kopioi valmiit suoritukset tallennettavaan opiskeluoikeuteen,
      // joten 8C ja 9C säilyvät. Siirrossa mukana ollut suoritus päätyy listan viimeiseksi,
      // päättötodistuksen taakse - tästä järjestyksestä osasiirron tunnistaa tuotannossa.
      tallennetutLuokat should equal(List("7C", "8C", "9C"))
      tallennetutSuoritustyypit.last should equal("perusopetuksenvuosiluokka")
      tietokannanLuokka should equal(Some("9C"))

      // Perustiedot indeksoidaan kuitenkin pyynnön payloadista eikä tallennetusta
      // opiskeluoikeudesta, joten indeksiin jää se luokka, joka sattui olemaan siirrossa mukana.
      indeksoituLuokka should equal(Some("9C"))
    }
  }

  private val kaisanPerusopetus = PerusopetusExampleData.päättötodistusOpiskeluoikeus()

  private def siirräVainSeitsemännenLuokanSuoritus(): Unit = {
    val seitsemännenLuokanSuoritus = kaisanPerusopetus.suoritukset.collect {
      case s: PerusopetuksenVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "7" => s
    }
    seitsemännenLuokanSuoritus.map(_.luokka) should equal(List("7C"))

    putOpiskeluoikeus(
      kaisanPerusopetus.copy(
        oid = Some(opiskeluoikeusOid),
        suoritukset = seitsemännenLuokanSuoritus,
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(
          kaisanPerusopetus.tila.opiskeluoikeusjaksot.filter(_.tila.koodiarvo == "lasna")
        )
      ),
      henkilö = koululainen
    )(verifyResponseStatusOk())
  }

  private def tallennettuPerusopetus: PerusopetuksenOpiskeluoikeus =
    getOpiskeluoikeus(koululainen.oid, "perusopetus").asInstanceOf[PerusopetuksenOpiskeluoikeus]

  private def opiskeluoikeusOid: String = tallennettuPerusopetus.oid.get

  private def tallennetutLuokat: List[String] =
    tallennettuPerusopetus.suoritukset.collect { case s: PerusopetuksenVuosiluokanSuoritus => s.luokka }.distinct.sorted

  private def tallennetutSuoritustyypit: List[String] =
    tallennettuPerusopetus.suoritukset.map(_.tyyppi.koodiarvo)

  private def tietokannanLuokka: Option[String] =
    runDbSync(KoskiOpiskeluOikeudet.filter(_.oid === opiskeluoikeusOid).map(_.luokka).result).head

  private def indeksoituLuokka: Option[String] = {
    KoskiApplicationForTests.perustiedotIndexer.sync(refresh = true)
    searchForPerustiedot(Map("nimihaku" -> "Koululainen"))
      .find(pt => pt.henkilöOid.contains(koululainen.oid) && pt.tyyppi.koodiarvo == "perusopetus")
      .flatMap(_.luokka)
  }

  private def searchPerustiedot(tutkintoHakuString: String, lang: String): List[String] = {
    Wait.until(searchForPerustiedot(Map("tutkintohaku" -> tutkintoHakuString)).nonEmpty, timeoutMs = 1000)
    searchForPerustiedot(Map("tutkintohaku" -> tutkintoHakuString)).flatMap(_.suoritukset.flatMap(_.koulutusmoduuli.tunniste.nimi)).map(_.get(lang))
  }

  private def createEnglanninkielinenSuoritus = {
    putAmmatillinenPäätasonSuoritus(suoritus(english("Respect: Helping us sustain a harassment free workplace")))(verifyResponseStatusOk())
    putAmmatillinenPäätasonSuoritus(suoritus(swedish("Respekt: Hjälp oss att upprätthålla en trakasseringsfri arbetsplats")), henkilö = asUusiOppija(eerola))(verifyResponseStatusOk())
    putAmmatillinenPäätasonSuoritus(suoritus(finnish("Kunnioitus: Auta meitä ylläpitämään häirinnätöntä työpaikkaa")), henkilö = asUusiOppija(lukiolainen))(verifyResponseStatusOk())
    KoskiApplicationForTests.perustiedotIndexer.sync(refresh = true)
  }

  def suoritus(tutkinnonTunniste: LocalizedString): MuunAmmatillisenKoulutuksenSuoritus = muunAmmatillisenKoulutuksenSuoritus(
    koulutusmoduuli = PaikallinenMuuAmmatillinenKoulutus(
      tunniste = PaikallinenKoodi(koodiarvo = "RESPECT", tutkinnonTunniste),
      laajuus = None,
      kuvaus = finnish("xyz")
    ),
    toimipiste = stadinToimipiste
  )

  override protected def defaultPäätasonSuoritus: MuunAmmatillisenKoulutuksenSuoritus = suoritus(LocalizedString.empty)
}
