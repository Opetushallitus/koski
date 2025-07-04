package fi.oph.koski.koskiuser

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.typesafe.config.ConfigFactory
import fi.oph.koski.TestEnvironment
import fi.oph.koski.cache.GlobalCacheManager
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.organisaatio.MockOrganisaatiot.{helsinginKaupunki, lehtikuusentienToimipiste, oppilaitokset}
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot, Opetushallitus, OrganisaatioHierarkia}
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi._
import fi.oph.koski.sso.CasService
import fi.oph.koski.userdirectory.{DirectoryUser, OpintopolkuDirectoryClient}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import org.mockito.Mockito.{mock, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import org.scalatra.servlet.RichRequest
import org.scalatra.util.MultiMapHeadView

import java.net.InetAddress
import java.net.InetAddress.{getByName => inetAddress}

class KoskiSpecificSessionSpec
  extends AnyFreeSpec
    with TestEnvironment
    with Matchers
    with EitherValues
    with OptionValues
    with BeforeAndAfterAll {

  implicit val jsonDefaultFormats = DefaultFormats.preservingEmptyValues
  private val config = ConfigFactory.parseString(
    """
      |authentication-service.useCas = false
      |opintopolku.virkailija.url = "http://localhost:9877"
      |opintopolku.virkailija.username = "foo"
      |opintopolku.virkailija.password = "bar"
      |opintopolku.oppija.url = "http://localhost:9877"
      |otuvaTokenEndpoint="mock"
      |oppijanumerorekisteri.baseUrl="http://localhost:9877"
    """.stripMargin)

  implicit val cacheManager = GlobalCacheManager
  private val wireMockServer = new WireMockServer(wireMockConfig().port(9877))
  private val directoryClient = new OpintopolkuDirectoryClient(config, new CasService(config))
  private val käyttöoikeusRepository = new KäyttöoikeusRepository(MockOrganisaatioRepository, directoryClient)

  "KoskiSession" - {
    "Client ip" - {
      "clientIp contains single address" in {
        mkSession("127.0.0.1").clientIp should be(inetAddress("127.0.0.1"))
      }

      "clientIp contains multiple addresses" in {
        mkSession("127.0.0.1, 192.168.0.1").clientIp should be(inetAddress("127.0.0.1"))
        mkSession("127.0.0.1,192.168.0.1").clientIp should be(inetAddress("127.0.0.1"))
        mkSession(" 127.0.0.1    ,  192.168.0.1  ").clientIp should be(inetAddress("127.0.0.1"))
      }
    }

    "resolves user and käyttöoikeudet" - {
      "usean organisaation tallentaja" in {
        createAndVerifySession("kalle", MockUsers.kalle.ldapUser)
      }
      "pääkäyttäjä" in {
        val session = createAndVerifySession("pää", MockUsers.paakayttaja.ldapUser, isRoot = true)
        session.hasGlobalReadAccess should be(true)
        session.isRoot should be(true)
        session.hasTiedonsiirronMitätöintiAccess(helsinginKaupunki, None)
      }
      "viranomainen" in {
        createAndVerifySession("viranomais", MockUsers.viranomainen.ldapUser)
      }
      "epäluotettava-tallentaja" in {
        createAndVerifySession("epäluotettava-tallentaja", MockUsers.tallentajaEiLuottamuksellinen.ldapUser)
      }
      "palvelukäyttäjä" in {
        createAndVerifySession("omnia-palvelukäyttäjä", MockUsers.omniaPalvelukäyttäjä.ldapUser)
      }
      "vastuukäyttäjä" in {
        createAndVerifySession("stadin-vastuu", MockUsers.stadinVastuukäyttäjä.ldapUser)
      }
      "kahden organisaation palvelukäyttäjä" in {
        createAndVerifySession("palvelu2", MockUsers.kahdenOrganisaatioPalvelukäyttäjä.ldapUser)
      }
      "oppilaitos katselija" in {
        createAndVerifySession("omnia-katselija", MockUsers.omniaKatselija.ldapUser)
      }
      "oppilaitos esiopetuskatselija" in {
        createAndVerifySession("esiopetus", MockUsers.jyväskylänKatselijaEsiopetus.ldapUser)
      }
      "oppilaitos tallentaja" in {
        createAndVerifySession("omnia-tallentaja", MockUsers.omniaTallentaja.ldapUser)
      }
      "oppilaitos ei Koski-oikeuksia" in {
        val session = createAndVerifySession("Otto", MockUsers.eiOikkia.ldapUser)
        session.allowedOpiskeluoikeudetJaPäätasonSuoritukset should be(empty)
        session.hasAnyReadAccess should be(false)
      }
      "kela suppeat oikeudet" in {
        val session = createAndVerifySession("Suppea", MockUsers.kelaSuppeatOikeudet.ldapUser)
        session.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA)) should be(false)
        session.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA)) should be(true)
      }
      "kela laajat oikeudet" in {
        val session = createAndVerifySession("Laaja", MockUsers.kelaLaajatOikeudet.ldapUser)
        session.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)) should be(false)
        session.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA)) should be(true)
      }
      "viranomainen kaikki koulutusmuodot ei arkaluontoisten tietojen oikeuksia" in {
        val session = createAndVerifySession("Eeva", MockUsers.viranomainenGlobaaliKatselija.ldapUser)
        session.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT, Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA, Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA)) should be(false)
      }
      "viranomainen perusopetus" in {
        val session = createAndVerifySession("Pertti", MockUsers.perusopetusViranomainen.ldapUser)
        val expectedOpiskeluoikeustyypit = Set(
          esiopetus,
          perusopetus,
          aikuistenperusopetus,
          perusopetuksenlisaopetus,
          perusopetukseenvalmistavaopetus,
          internationalschool,
          europeanschoolofhelsinki,
        ).flatMap(OoPtsMask.fromKoodistokoodiviite)
        session.allowedOpiskeluoikeudetJaPäätasonSuoritukset should equal(expectedOpiskeluoikeustyypit)
      }
      "viranomainen toinen aste" in {
        val session = createAndVerifySession("Teuvo", MockUsers.toinenAsteViranomainen.ldapUser)
        val expectedOpiskeluoikeustyypit = Set(
          tuva,
          ammatillinenkoulutus,
          ibtutkinto,
          diatutkinto,
          lukiokoulutus,
          luva,
          vapaansivistystyonkoulutus,
          ylioppilastutkinto,
          internationalschool,
          europeanschoolofhelsinki,
          ebtutkinto
        ).flatMap(OoPtsMask.fromKoodistokoodiviite)
        session.allowedOpiskeluoikeudetJaPäätasonSuoritukset should equal(expectedOpiskeluoikeustyypit)
      }
      "viranomainen korkeakoulu" in {
        val session = createAndVerifySession("Kaisa", MockUsers.korkeakouluViranomainen.ldapUser)
        session.allowedOpiskeluoikeudetJaPäätasonSuoritukset should equal(Set(
          OpiskeluoikeudenTyyppi.korkeakoulutus
        ).flatMap(OoPtsMask.fromKoodistokoodiviite))
      }
      "tilastokeskus saa arkaluontoisenkin datan" in {
        val session = createAndVerifySession("Teppo", MockUsers.tilastokeskusKäyttäjä.ldapUser)
        session.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)) should be(true)
      }
      "migrillä ei arkaluontoisten tietojen oikeuksia" in {
        val session = createAndVerifySession("Migri", MockUsers.migriKäyttäjä.ldapUser)
        session.sensitiveDataAllowed(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)) should be(false)
      }
    }
  }

  private def createAndVerifySession(username: String, expected: DirectoryUser, isRoot: Boolean = false) = {
    val authUser = AuthenticationUser.fromDirectoryUser(username, expected)
    val session = KoskiSpecificSession(authUser, req, käyttöoikeusRepository)

    session.lang should be("fi")
    session.clientIp should be(InetAddress.getByName("10.1.2.3"))
    session.userAgent should be("MockUserAgent/1.0")

    session.oid should be(expected.oid)
    session.user.name should be(expected.etunimet + " " + expected.sukunimi)
    session.username should be(username)

    val expectedKäyttöoikeudet = expected.käyttöoikeudet.toSet
    session.orgKäyttöoikeudet should be(expectedOrganisaatioKäyttöoikeudet(expected))
    session.globalKäyttöoikeudet should be(expectedKäyttöoikeudet.collect { case k : KäyttöoikeusGlobal if k.globalAccessType.contains(AccessType.read) => k })
    session.globalViranomaisKäyttöoikeudet should be(expectedKäyttöoikeudet.collect { case k : KäyttöoikeusViranomainen => k})

    val expectedAllowedOpiskeluoikeudenTyypit = expectedKäyttöoikeudet.flatMap(_.allowedOpiskeluoikeusTyypit)
    session.allowedOpiskeluoikeudetJaPäätasonSuoritukset should be(expectedAllowedOpiskeluoikeudenTyypit)

    session.hasKoulutusmuotoRestrictions should be(expectedAllowedOpiskeluoikeudenTyypit != OpiskeluoikeudenTyyppi.kaikkiTyypit(isRoot).map(t => OoPtsMask(t.koodiarvo)))
    session
  }

  private def expectedOrganisaatioKäyttöoikeudet(u: DirectoryUser) = {
    def mockOrganisaatioHierarkia(k: KäyttöoikeusOrg): List[OrganisaatioHierarkia] = {
      OrganisaatioHierarkia.flatten(MockOrganisaatioRepository.getOrganisaatioHierarkia(k.organisaatio.oid).toList)
    }

    u.käyttöoikeudet.collect { case k: KäyttöoikeusOrg if k.organisaatioAccessType.contains(AccessType.read) => k }.flatMap { k =>
      mockOrganisaatioHierarkia(k).map { org =>
        k.copy(organisaatio = org.toOrganisaatio, juuri = org.oid == k.organisaatio.oid, oppilaitostyyppi = org.oppilaitostyyppi)
      }
    }.toSet
  }

  private val req = mock(classOf[RichRequest])

  override protected def beforeAll {
    super.beforeAll()
    when(req.header("User-Agent")).thenReturn(Some("MockUserAgent/1.0"))
    when(req.header("HTTP_X_FORWARDED_FOR")).thenReturn(Some("10.1.2.3"))
    when(req.cookies).thenReturn(MultiMapHeadView.empty[String, String])
    wireMockServer.start()
    mockEndpoints
  }

  override protected def afterAll: Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  private def mockEndpoints = {
    val käyttöoikeusUrl = "/kayttooikeus-service/kayttooikeus/kayttaja"
    def henkilöUrl(username: String) = {
      val oid = MockUsers.users.find(_.username == username).map(_.oid).getOrElse(throw new Exception("No oid found for user " + username))
      s"/oppijanumerorekisteri-service/henkilo/$oid"
    }

    Responses.käyttöoikeusResponse.foreach { case (username, resp) =>
      wireMockServer.stubFor(
        get(urlPathEqualTo(käyttöoikeusUrl))
          .withQueryParam("username", equalTo(username))
          .willReturn(ok(resp))
      )

      wireMockServer.stubFor(
        get(urlPathEqualTo(henkilöUrl(username))).willReturn(ok(Responses.käyttäjäResponse(username)))
      )
    }
  }

  private def mkSession(ipStr: String) = {
    val ip = LogUserContext.toInetAddress(ipStr)
    new KoskiSpecificSession(AuthenticationUser("", "", "", None), "fi", ip, "", Set())
  }
}

object Responses {
  implicit val jsonDefaultFormats = DefaultFormats.preservingEmptyValues
  val kallenOppilaitokset = lehtikuusentienToimipiste :: oppilaitokset
  val käyttöoikeusResponse: Map[String, String] = Map(
    "kalle" -> List(Map(
      "oidHenkilo" -> MockUsers.kalle.oid,
      "organisaatiot" -> kallenOppilaitokset.map(oid => Map(
        "organisaatioOid" -> oid,
        "kayttooikeudet" -> List(Map("palvelu" -> "KOSKI", "oikeus" -> "KAIKKI_OPISKELUOIKEUS_TYYPIT"), Map("palvelu" -> "KOSKI", "oikeus" -> "READ_UPDATE"), Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"))
      )))),
    "pää" -> List(Map(
      "oidHenkilo" -> MockUsers.paakayttaja.oid,
      "organisaatiot" -> List(
        Map(
          "organisaatioOid" -> "1.2.246.562.10.00000000001",
          "kayttooikeudet" -> List(Map("palvelu" -> "KOSKI", "oikeus" -> "OPHPAAKAYTTAJA"), Map("palvelu" -> "KOSKI", "oikeus" -> "YLLAPITAJA"), Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"), Map("palvelu" -> "KOSKI", "oikeus" -> "LAHDEJARJESTELMAKYTKENNAN_PURKAMINEN"))
        ),
        Map(
          "organisaatioOid" -> "1.2.246.562.10.00000000001",
          "kayttooikeudet" -> List(Map("palvelu" -> "LOKALISOINTI", "oikeus" -> "CRUD"))
        ),
        Map(
          "organisaatioOid" -> "1.2.246.562.10.00000000001",
          "kayttooikeudet" -> List(Map("palvelu" -> "OPPIJANUMEROREKISTERI", "oikeus" -> "REKISTERINPITAJA"))
        )
      )
    )),
    "viranomais" -> List(Map(
      "oidHenkilo" -> MockUsers.viranomainen.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> "1.2.246.562.10.00000000001",
        "kayttooikeudet" -> List(Map("palvelu" -> "KOSKI", "oikeus" -> "OPHKATSELIJA"))
      ))
    )),
    "epäluotettava-tallentaja" -> List(Map(
      "oidHenkilo" -> MockUsers.tallentajaEiLuottamuksellinen.oid,
      "organisaatiot" -> List(
        Map(
          "organisaatioOid" -> "1.2.246.562.10.51720121923",
          "kayttooikeudet" -> List(Map("palvelu" -> "KOSKI", "oikeus" -> "KAIKKI_OPISKELUOIKEUS_TYYPIT"), Map("palvelu" -> "KOSKI", "oikeus" -> "READ_UPDATE"))
        ),
        Map(
          "organisaatioOid" -> "1.2.246.562.10.14613773812",
          "kayttooikeudet" -> List(Map("palvelu" -> "KOSKI", "oikeus" -> "KAIKKI_OPISKELUOIKEUS_TYYPIT"), Map("palvelu" -> "KOSKI", "oikeus" -> "READ_UPDATE"))
        )
      )
    )),
    "omnia-palvelukäyttäjä" -> List(Map(
      "oidHenkilo" -> MockUsers.omniaPalvelukäyttäjä.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> "1.2.246.562.10.51720121923",
        "kayttooikeudet" -> List(
          Map("palvelu" -> "KOSKI", "oikeus" -> "READ_UPDATE"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "TIEDONSIIRTO"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT")
        )
      ))
    )),
    "stadin-vastuu" -> List(Map(
      "oidHenkilo" -> MockUsers.stadinVastuukäyttäjä.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> "1.2.246.562.10.346830761110",
        "kayttooikeudet" -> List(Map("palvelu" -> "KOSKI", "oikeus" -> "READ"))
      ))
    )),
    "Otto" -> List(Map(
      "oidHenkilo" -> MockUsers.eiOikkia.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.lehtikuusentienToimipiste,
        "kayttooikeudet" -> List(Map("palvelu" -> "OPPIJANUMEROREKISTERI", "oikeus" -> "READ"))
      ))
    )),
    "palvelu2" -> List(Map(
      "oidHenkilo" -> MockUsers.kahdenOrganisaatioPalvelukäyttäjä.oid,
      "organisaatiot" -> List(
        Map(
          "organisaatioOid" -> "1.2.246.562.10.346830761110",
          "kayttooikeudet" -> List(
            Map("palvelu" -> "KOSKI", "oikeus" -> "READ_UPDATE"),
            Map("palvelu" -> "KOSKI", "oikeus" -> "TIEDONSIIRTO"),
            Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"))
        ),
        Map(
          "organisaatioOid" -> "1.2.246.562.10.51720121923",
          "kayttooikeudet" -> List(
            Map("palvelu" -> "KOSKI", "oikeus" -> "READ_UPDATE"),
            Map("palvelu" -> "KOSKI", "oikeus" -> "TIEDONSIIRTO"),
            Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"))
        ))
    )),
    "omnia-katselija" -> List(Map(
      "oidHenkilo" -> MockUsers.omniaKatselija.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.omnia,
        "kayttooikeudet" -> List(Map("palvelu" -> "KOSKI", "oikeus" -> "READ"), Map("palvelu" -> "KOSKI", "oikeus" -> "KAIKKI_OPISKELUOIKEUS_TYYPIT"), Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"))
      ))
    )),
    "omnia-tallentaja" -> List(Map(
      "oidHenkilo" -> MockUsers.omniaTallentaja.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.omnia,
        "kayttooikeudet" -> List(
          Map("palvelu" -> "KOSKI", "oikeus" -> "KAIKKI_OPISKELUOIKEUS_TYYPIT"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "READ_UPDATE"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"))
      ))
    )),
    "Suppea" -> List(Map(
      "oidHenkilo" -> MockUsers.kelaSuppeatOikeudet.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.kela,
        "kayttooikeudet" -> List(
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_PERUSOPETUS"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TOINEN_ASTE"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_KORKEAKOULU"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_MUU_KUIN_SAANNELTY"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TAITEENPERUSOPETUS"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KELA_SUPPEA"))
      ))
    )),
    "Laaja" -> List(Map(
      "oidHenkilo" -> MockUsers.kelaLaajatOikeudet.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.kela,
        "kayttooikeudet" -> List(
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_PERUSOPETUS"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TOINEN_ASTE"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_KORKEAKOULU"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_MUU_KUIN_SAANNELTY"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TAITEENPERUSOPETUS"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KELA_LAAJA"))
      ))
    )),
    "Eeva" -> List(Map(
      "oidHenkilo" -> MockUsers.viranomainenGlobaaliKatselija.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.evira,
        "kayttooikeudet" -> List(
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_PERUSOPETUS"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TOINEN_ASTE"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_KORKEAKOULU"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_MUU_KUIN_SAANNELTY"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TAITEENPERUSOPETUS"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_KIELITUTKINTO"),
      )))
    )),
    "Pertti" -> List(Map(
      "oidHenkilo" -> MockUsers.perusopetusViranomainen.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.evira,
        "kayttooikeudet" -> List(Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_PERUSOPETUS"))
      ))
    )),
    "Teuvo" -> List(Map(
      "oidHenkilo" -> MockUsers.toinenAsteViranomainen.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.evira,
        "kayttooikeudet" -> List(Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TOINEN_ASTE"))
      ),
      Map(
        "organisaatioOid" -> Opetushallitus.organisaatioOid,
        "kayttooikeudet" -> List(Map("palvelu" -> "OPPIJANUMEROREKISTERI", "oikeus" -> "REKISTERINPITAJA"))
      ))
    )),
    "Teppo" -> List(Map(
      "oidHenkilo" -> MockUsers.tilastokeskusKäyttäjä.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.evira,
        "kayttooikeudet" -> List(
          Map("palvelu" -> "KOSKI", "oikeus" -> "TILASTOKESKUS"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_PERUSOPETUS"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TOINEN_ASTE"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_KORKEAKOULU"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_MUU_KUIN_SAANNELTY"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TAITEENPERUSOPETUS"))
      ))
    )),
    "Kaisa" -> List(Map(
      "oidHenkilo" -> MockUsers.korkeakouluViranomainen.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.evira,
        "kayttooikeudet" -> List(Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_KORKEAKOULU"))
      ),
      Map(
        "organisaatioOid" -> Opetushallitus.organisaatioOid,
        "kayttooikeudet" -> List()
      ))
    )),
    "Migri" -> List(Map(
      "oidHenkilo" -> MockUsers.migriKäyttäjä.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.migri,
        "kayttooikeudet" -> List(
          Map("palvelu" -> "KOSKI", "oikeus" -> "MIGRI"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_PERUSOPETUS"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TOINEN_ASTE"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_KORKEAKOULU"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_MUU_KUIN_SAANNELTY"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "GLOBAALI_LUKU_TAITEENPERUSOPETUS"))
      ))
    )),
    "esiopetus" -> List(Map(
      "oidHenkilo" -> MockUsers.jyväskylänKatselijaEsiopetus.oid,
      "organisaatiot" -> List(Map(
        "organisaatioOid" -> MockOrganisaatiot.jyväskylänNormaalikoulu,
        "kayttooikeudet" -> List(
          Map("palvelu" -> "KOSKI", "oikeus" -> "READ"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "LUKU_ESIOPETUS"),
          Map("palvelu" -> "KOSKI", "oikeus" -> "LUOTTAMUKSELLINEN_KAIKKI_TIEDOT"))
      ))
    ))
  ).map { case (username, data) => (username, write(data)) }

  val käyttäjäResponse: Map[String, String] = MockUsers.users.map { u =>
    (u.username, write(Map(
      "oidHenkilo" -> u.oid,
      "sukunimi" -> u.ldapUser.sukunimi,
      "etunimet" -> u.ldapUser.etunimet,
      "asiointiKieli" -> Map("kieliKoodi" -> u.ldapUser.asiointikieli.getOrElse("fi"))
    )))
  }.toMap
}
