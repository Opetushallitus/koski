package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.KoskiSpecificSession.SUORITUSJAKO_KATSOMINEN_USER
import fi.oph.koski.koskiuser.Rooli.OPHKATSELIJA
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession, KäyttöoikeusGlobal, MockUsers, Palvelurooli}
import fi.oph.koski.schema
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, AmmatillisenTutkinnonOsittainenSuoritus, AmmatillisenTutkinnonSuoritus, MuunAmmatillisenKoulutuksenSuoritus, YlioppilastutkinnonOpiskeluoikeus, YlioppilastutkinnonSuoritus}
import fi.oph.koski.ytr.MockYrtClient
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.net.InetAddress

class SuoritetutTutkinnotServiceSpec
  extends AnyFreeSpec
  with KoskiHttpSpec
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with OpiskeluoikeusTestMethods {

  val suoritetutTutkinnotService = KoskiApplicationForTests.suoritetutTutkinnotService

  private val suoritusjakoKatsominenTestUser = new KoskiSpecificSession(
    AuthenticationUser(
      SUORITUSJAKO_KATSOMINEN_USER,
      SUORITUSJAKO_KATSOMINEN_USER,
      SUORITUSJAKO_KATSOMINEN_USER, None
    ),
    "fi",
    InetAddress.getLoopbackAddress,
    "",
    Set(KäyttöoikeusGlobal(List(Palvelurooli(OPHKATSELIJA))))
  )

  implicit val koskiSession = suoritusjakoKatsominenTestUser

  override def afterEach(): Unit = {
    MockYrtClient.reset()
    super.afterEach()
  }

  "Kosken testioppijoiden tiedot voi hakea ilman virheitä" in {
    val oppijaOidit = KoskiSpecificMockOppijat.defaultOppijat.map(_.henkilö.oid)

    oppijaOidit.length should be > 100

    oppijaOidit.foreach(oppijaOid => {
      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppijaOid)
      result.isRight should be(true)
    })
  }

  "Ammatillinen tutkinto" - {

    "vahvistetun tutkinnon tiedot palautetaan" in {
      val oppija = KoskiSpecificMockOppijat.ammattilainen

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
      val expectedSuoritusData = expectedOoData.suoritukset.head

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[SuoritetutTutkinnotAmmatillinenOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritus = actualOo.suoritukset.head

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      })
    }

    "vahvistamattoman tutkinnon tietoja ei palauteta" in {
      verifyEiOpiskeluoikeuksia(KoskiSpecificMockOppijat.amis)
    }

    "tulevaisuuteen merkityn vahvistuksen tietoja ei palauteta" in {
      verifyEiOpiskeluoikeuksia(KoskiSpecificMockOppijat.ammattilainenVahvistettuTulevaisuudessa)
    }

    "vahvistetun osa/osia-tyyppisen suorituksen tiedot palautetaan" in {
      val oppija = KoskiSpecificMockOppijat.osittainenammattitutkinto

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
      val expectedSuoritusData = expectedOoData.suoritukset.head

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[SuoritetutTutkinnotAmmatillinenOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritus = actualOo.suoritukset.head

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      })
    }

    "vahvistetun ja koulutusmoduuliltaan kooditetun muun ammatillisen suorituksen tiedot palautetaan" in {
      val oppija = KoskiSpecificMockOppijat.ammatilliseenTetäväänValmistavaMuuAmmatillinenVahvistettu

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
      val expectedSuoritusData = expectedOoData.suoritukset.head

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[SuoritetutTutkinnotAmmatillinenOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritus = actualOo.suoritukset.head

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      })
    }

    "Muun ammatillisen paikallisella koulutusmoduulilla olevaa suoritusta ei palauteta" in {
      verifyEiOpiskeluoikeuksia(KoskiSpecificMockOppijat.muuAmmatillinenKokonaisuuksilla)
    }
  }

  "YO-tutkinto" - {
    "vahvistetun tutkinnon tiedot palautetaan" in {
      val oppija = KoskiSpecificMockOppijat.ylioppilas

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
      val expectedSuoritusData = expectedOoData.suoritukset.head

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritus = actualOo.suoritukset.head

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      })
    }

    "vahvistamattoman tutkinnon tietoja ei palauteta" in {
      verifyEiOpiskeluoikeuksia(KoskiSpecificMockOppijat.ylioppilasEiValmistunut)
    }
  }

  "Linkitetyt oppijat" - {
    "Masterin ja slaven tutkinnot palautetaan kummankin oideilla" in {
      val oppijaMaster = KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen
      val oppijaSlave = KoskiSpecificMockOppijat.slaveAmmattilainen

      val resultMaster = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppijaMaster.oid)
      val resultSlave = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppijaSlave.henkilö.oid)

      resultMaster.isRight should be(true)
      resultMaster.foreach(o => {
        verifyOppija(oppijaMaster, o)
        o.opiskeluoikeudet should have length 3
      })

      resultMaster should equal(resultSlave)
    }
  }

  "mitätöityjä ei palauteta" in {
    verifyEiOpiskeluoikeuksia(KoskiSpecificMockOppijat.lukiolainen)
  }

  "palautetaan 404-virhe, jos oppijaa ei löydy lainkaan" in {
    val loytymatonOppijaOid = "1.2.246.562.24.54450598999"

    verifyEiLöydyTaiEiKäyttöoikeuksia(loytymatonOppijaOid)
  }

  "Käyttöoikeudet" - {
    val oppija = KoskiSpecificMockOppijat.ammattilainen

    "Globaaleilla lukuoikeuksilla voi hakea" in {
      implicit val koskiSession = MockUsers.ophkatselija.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)
    }

    "Yhden organisaation käyttäjä ei voi hakea" in {
      implicit val koskiSession = MockUsers.stadinAmmattiopistoKatselija.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

      verifyEiLöydyTaiEiKäyttöoikeuksia(oppija.oid)

    }

    "Palvelukäyttäjä ei voi hakea" in {
      implicit val koskiSession = MockUsers.kahdenOrganisaatioPalvelukäyttäjä.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

      verifyEiLöydyTaiEiKäyttöoikeuksia(oppija.oid)
    }
  }

  "jos YTR palauttaa virheen, palautetaan virhe" in {
    val oppija = KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen

    suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid).isRight should be(true)

    KoskiApplicationForTests.cacheManager.invalidateAllCaches
    MockYrtClient.setFailureHetu(oppija.hetu.get)

    val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

    result.isRight should be(false)
    result should equal(Left(KoskiErrorCategory.unavailable.ytr()))
  }

  "jos YTR timeouttaa, palautetaan virhe" in {
    val oppija = KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen

    suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid).isRight should be(true)

    KoskiApplicationForTests.cacheManager.invalidateAllCaches
    MockYrtClient.setTimeoutHetu(oppija.hetu.get)

    val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

    result.isRight should be(false)
    result should equal(Left(KoskiErrorCategory.unavailable()))
  }

  "TODO: Älä palauta kuori-opiskeluoikeuksia, ainoastaan sisältyvät" - {
    // TODO
  }

  "Korkeakoulututkinnot" - {
    // TODO
  }

  "jos Virta palauttaa virheen, palautetaan virhe" in {
    // TODO
  }

  "Muut tutkinnot" - {
    // TODO DIA, EB
  }

  "Audit-lokit" - {
    // TODO
  }

  private def verifyOppija(expected: LaajatOppijaHenkilöTiedot, actual: SuoritetutTutkinnotOppija) = {
    actual.henkilö.oid should be(expected.oid)
    actual.henkilö.etunimet should be(expected.etunimet)
    actual.henkilö.kutsumanimi should be(expected.kutsumanimi)
    actual.henkilö.sukunimi should be(expected.sukunimi)
    actual.henkilö.syntymäaika should be(expected.syntymäaika)
  }

  private def verifyOpiskeluoikeusJaSuoritus(
    actualOo: SuoritetutTutkinnotOpiskeluoikeus,
    actualSuoritus: Suoritus,
    expectedOoData: schema.Opiskeluoikeus,
    expectedSuoritusData: schema.Suoritus
  ): Unit = {
    (actualOo, actualSuoritus, expectedOoData, expectedSuoritusData) match {
      case (
        actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus,
        expectedOoData: schema.AmmatillinenOpiskeluoikeus,
        expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus
      ) => verifyAmmatillinenTutkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus,
        expectedOoData: schema.AmmatillinenOpiskeluoikeus,
        expectedSuoritusData: schema.AmmatillisenTutkinnonOsittainenSuoritus
      ) => verifyAmmatillinenOsittainen(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus,
        expectedOoData: schema.AmmatillinenOpiskeluoikeus,
        expectedSuoritusData: schema.MuunAmmatillisenKoulutuksenSuoritus
      ) => verifyMuuAmmatillinen(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus,
        expectedOoData: schema.YlioppilastutkinnonOpiskeluoikeus,
        expectedSuoritusData: schema.YlioppilastutkinnonSuoritus
      ) => verifyYlioppilastutkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)

      case _ => fail("Palautettiin tunnistamattoman tyyppistä dataa")
    }
  }

  private def verifyAmmatillinenTutkinto(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus,
    expectedOoData: AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: AmmatillisenTutkinnonSuoritus
  ): Unit = {
    verifyAmmatillinenOpiskeluoikeudenKentät(actualOo, expectedOoData)
    verifyAmmatillisenTutkinnonOsittainenTaiKokoSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyAmmatillinenOsittainen(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus,
    expectedOoData: AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: AmmatillisenTutkinnonOsittainenSuoritus
  ): Unit = {
    verifyAmmatillinenOpiskeluoikeudenKentät(actualOo, expectedOoData)
    verifyAmmatillisenTutkinnonOsittainenTaiKokoSuoritus(actualSuoritus, expectedSuoritusData)

    actualSuoritus.toinenOsaamisala should equal(Some(expectedSuoritusData.toinenOsaamisala))
    actualSuoritus.toinenTutkintonimike should equal(Some(expectedSuoritusData.toinenTutkintonimike))
    actualSuoritus.korotettuOpiskeluoikeusOid should equal(expectedSuoritusData.korotettuOpiskeluoikeusOid)
  }

  private def verifyMuuAmmatillinen(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus,
    expectedOoData: AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: MuunAmmatillisenKoulutuksenSuoritus
  ): Unit = {
    verifyAmmatillinenOpiskeluoikeudenKentät(actualOo, expectedOoData)
    verifyMuunAmmatillisenTutkinnonSuoritus(actualSuoritus, expectedSuoritusData)
  }

  private def verifyAmmatillisenTutkinnonOsittainenTaiKokoSuoritus(
    actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenTaiKokoSuoritus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonOsittainenTaiKokoSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    actualSuoritus.koulutusmoduuli.perusteenDiaarinumero should equal(expectedSuoritusData.koulutusmoduuli.perusteenDiaarinumero)
    actualSuoritus.koulutusmoduuli.perusteenNimi should equal(expectedSuoritusData.koulutusmoduuli.perusteenNimi)
    actualSuoritus.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo))

    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.suorituskieli.map(_.koodiarvo) should equal(Some(expectedSuoritusData.suorituskieli.koodiarvo))
    actualSuoritus.osaamisala.map(_.length) should equal(expectedSuoritusData.osaamisala.map(_.length))
    actualSuoritus.järjestämismuodot.map(_.length) should equal(expectedSuoritusData.järjestämismuodot.map(_.length))
    actualSuoritus.osaamisenHankkimistavat.map(_.length) should equal(expectedSuoritusData.osaamisenHankkimistavat.map(_.length))
    actualSuoritus.työssäoppimisjaksot.map(_.length) should equal(expectedSuoritusData.työssäoppimisjaksot.map(_.length))
    actualSuoritus.koulutussopimukset.map(_.length) should equal(expectedSuoritusData.koulutussopimukset.map(_.length))
    actualSuoritus.tutkintonimike.map(_.length) should equal(expectedSuoritusData.tutkintonimike.map(_.length))
  }

  private def verifyMuunAmmatillisenTutkinnonSuoritus(
    actualSuoritus: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus,
    expectedSuoritusData: MuunAmmatillisenKoulutuksenSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    actualSuoritus.koulutusmoduuli.laajuus.map(_.arvo) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.arvo))
    actualSuoritus.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo))
    actualSuoritus.koulutusmoduuli.laajuus.map(_.yksikkö.koodistoUri) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.yksikkö.koodistoUri))
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.suorituskieli.map(_.koodiarvo) should equal(Some(expectedSuoritusData.suorituskieli.koodiarvo))
    actualSuoritus.osaamisenHankkimistavat.map(_.length) should equal(expectedSuoritusData.osaamisenHankkimistavat.map(_.length))
    actualSuoritus.koulutussopimukset.map(_.length) should equal(expectedSuoritusData.koulutussopimukset.map(_.length))

    actualSuoritus.täydentääTutkintoa.map(_.tunniste.koodiarvo) should equal(expectedSuoritusData.täydentääTutkintoa.map(_.tunniste.koodiarvo))
    actualSuoritus.täydentääTutkintoa.map(_.perusteenDiaarinumero) should equal(expectedSuoritusData.täydentääTutkintoa.map(_.perusteenDiaarinumero))
    actualSuoritus.täydentääTutkintoa.map(_.perusteenNimi) should equal(expectedSuoritusData.täydentääTutkintoa.map(_.perusteenNimi))
    actualSuoritus.täydentääTutkintoa.map(_.koulutustyyppi.map(_.koodiarvo)) should equal(expectedSuoritusData.täydentääTutkintoa.map(_.koulutustyyppi.map(_.koodiarvo)))
  }

  private def verifyAmmatillinenOpiskeluoikeudenKentät(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    expectedOoData: AmmatillinenOpiskeluoikeus
  ): Unit = {
    actualOo.oid should be(expectedOoData.oid)
    actualOo.versionumero should be(expectedOoData.versionumero)
    actualOo.aikaleima should be(expectedOoData.aikaleima)
    actualOo.oppilaitos.map(_.oid) should equal(expectedOoData.oppilaitos.map(_.oid))
    actualOo.koulutustoimija.map(_.oid) should equal(expectedOoData.koulutustoimija.map(_.oid))
    actualOo.sisältyyOpiskeluoikeuteen.map(_.oid) should equal(expectedOoData.sisältyyOpiskeluoikeuteen.map(_.oid))
    actualOo.suoritukset.length should equal(expectedOoData.suoritukset.length)
    actualOo.tyyppi.koodiarvo should equal(expectedOoData.tyyppi.koodiarvo)
    actualOo.organisaatiohistoria.map(_.length) should equal(expectedOoData.organisaatiohistoria.map(_.length))
  }

  private def verifyYlioppilastutkinto(
    actualOo: SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus,
    expectedOoData: YlioppilastutkinnonOpiskeluoikeus,
    expectedSuoritusData: YlioppilastutkinnonSuoritus
  ): Unit = {
    actualOo.oppilaitos.map(_.oid) should equal(expectedOoData.oppilaitos.map(_.oid))
    actualOo.koulutustoimija.map(_.oid) should equal(expectedOoData.koulutustoimija.map(_.oid))
    actualOo.suoritukset.length should equal(expectedOoData.suoritukset.length)
    actualOo.tyyppi.koodiarvo should equal(expectedOoData.tyyppi.koodiarvo)

    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo))
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.alkamispäivä should equal(expectedSuoritusData.alkamispäivä)
  }

  private def verifyEiOpiskeluoikeuksia(oppija: LaajatOppijaHenkilöTiedot) = {
    val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

    result.isRight should be(true)

    result.map(o => {
      verifyOppija(oppija, o)

      o.opiskeluoikeudet should have length 0
    })
  }

  private def verifyEiLöydyTaiEiKäyttöoikeuksia(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit = {
    val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppijaOid)(user)

    result.isLeft should be(true)
    result should equal(Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.")))
  }
}
