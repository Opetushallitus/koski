package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.api.misc.{OpiskeluoikeusTestMethods, PutOpiskeluoikeusTestMethods}
import fi.oph.koski.db.KoskiTables
import fi.oph.koski.documentation.AmmatillinenExampleData.{ammatillisenTutkinnonOsittainenSuoritus, ammatillisetTutkinnonOsat, arviointiKiitettävä, k3, korotettu, lisätietoOsaamistavoitteet, osittaisenTutkinnonTutkinnonOsanSuoritus, tunnustettu, yhteisenOsittaisenTutkinnonTutkinnonOsansuoritus, yhteisetTutkinnonOsat}
import fi.oph.koski.documentation.ExampleData.{longTimeAgo, opiskeluoikeusLäsnä, valtionosuusRahoitteinen}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.KoskiSpecificSession.SUORITUSJAKO_KATSOMINEN_USER
import fi.oph.koski.koskiuser.MockUsers.stadinAmmattiopistoTallentaja
import fi.oph.koski.koskiuser.Rooli.OPHKATSELIJA
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession, KäyttöoikeusGlobal, MockUsers, Palvelurooli}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.{DatabaseTestMethods, KoskiApplicationForTests, KoskiHttpSpec, schema}
import fi.oph.koski.ytr.MockYrtClient
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot.omnia
import fi.oph.koski.schema.AmmatillinenOpiskeluoikeus
import fi.oph.koski.virta.MockVirtaClient
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._

import java.net.InetAddress
import java.time.LocalDate
import java.time.LocalDate.{of => date}
import scala.reflect.runtime.universe

class SuoritetutTutkinnotServiceSpec
  extends AnyFreeSpec
  with KoskiHttpSpec
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with OpiskeluoikeusTestMethods
  with PutOpiskeluoikeusTestMethods[schema.AmmatillinenOpiskeluoikeus]
  with DatabaseTestMethods
{
  def tag: universe.TypeTag[schema.AmmatillinenOpiskeluoikeus] = implicitly[reflect.runtime.universe.TypeTag[schema.AmmatillinenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo, suoritus = ammatillisenTutkinnonOsittainenSuoritus)

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
    val oppijaOidit = KoskiSpecificMockOppijat.defaultOppijat
      .filter(o => o.henkilö.hetu.isEmpty || o.henkilö.hetu.exists(!MockVirtaClient.virheenAiheuttavaHetu(_)))
      .map(_.henkilö.oid)

    oppijaOidit.length should be > 100

    oppijaOidit.foreach(oppijaOid => {
      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppijaOid)
      result.isRight should be(true)

      def isSuoritusjakoTehty(oid: String): Option[Boolean] = runDbSync(KoskiTables.KoskiOpiskeluOikeudet.filter(_.oid === oid).map(_.suoritusjakoTehty).result).headOption

      result.foreach(
        _.opiskeluoikeudet.collect ({ case koo: SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus => koo })
          .foreach(_.oid.flatMap(isSuoritusjakoTehty).map(_ should be(true)))
      )
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

    "Korotussuoritusta ei palauteta" in {
      val oppija = KoskiSpecificMockOppijat.amiksenKorottaja

      lisääKorotettuSuoritus(oppija)

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)
        o.opiskeluoikeudet should have length 1

        o.opiskeluoikeudet.head.suoritukset.head.tyyppi.koodiarvo should be("ammatillinentutkinto")
      })
    }

    "Monesta osaamisalasta palautetaan vain uusin" in {
      val oppija = KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)
        o.opiskeluoikeudet should have length 3

        val moniOsaamisalainenSuoritus =
          o.opiskeluoikeudet
            .collect { case oo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus => oo }
            .find(_.oppilaitos.map(_.oid) == Some(MockOrganisaatiot.stadinAmmattiopisto))
            .flatMap(_.suoritukset.collectFirst { case s: SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus => s})
            .get


        moniOsaamisalainenSuoritus.osaamisala.get should have length 1
        moniOsaamisalainenSuoritus.osaamisala.get.head.osaamisala.koodiarvo should be("1590")
      })
    }

    "Monesta osaamisalasta tulkitaan uusimmaksi alkupäivämäärätön" in {
      val oppija = KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)
        o.opiskeluoikeudet should have length 3

        val moniOsaamisalainenSuoritus =
          o.opiskeluoikeudet
            .collect { case oo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus => oo }
            .find(_.oppilaitos.map(_.oid) == Some(MockOrganisaatiot.kiipulanAmmattiopisto))
            .flatMap(_.suoritukset.collectFirst { case s: SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus => s})
            .get


        moniOsaamisalainenSuoritus.osaamisala.get should have length 1
        moniOsaamisalainenSuoritus.osaamisala.get.head.osaamisala.koodiarvo should be("1592")
      })
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

  "EB-tutkinto" - {
    "vahvistetun tutkinnon tiedot palautetaan" in {
      val oppija = KoskiSpecificMockOppijat.europeanSchoolOfHelsinki

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
      val expectedSuoritusData = expectedOoData.suoritukset.collectFirst { case eb: schema.EBTutkinnonSuoritus => eb }.get

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritus = actualOo.suoritukset.head

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      })
    }
  }

  "DIA-tutkinto" - {
    "vahvistetun tutkinnon tiedot palautetaan" in {
      val oppija = KoskiSpecificMockOppijat.dia

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
      val expectedSuoritusData = expectedOoData.suoritukset.collectFirst { case dia: schema.DIATutkinnonSuoritus => dia }.get

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[SuoritetutTutkinnotDIAOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritus = actualOo.suoritukset.head

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      })
    }
  }

  "Korkeakoulututkinnot" - {
    "vahvistetun tutkinnon tiedot palautetaan" in {
      val oppija = KoskiSpecificMockOppijat.dippainssi

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
      val expectedSuoritusData = expectedOoData.suoritukset.collectFirst { case korkeakoulu: schema.KorkeakoulututkinnonSuoritus => korkeakoulu }.get

      val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritus = actualOo.suoritukset.head

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      })
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

  "jos Virta palauttaa virheen, palautetaan virhe" in {
    val oppija = KoskiSpecificMockOppijat.virtaEiVastaa

    val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

    result.isRight should be(false)
    result should equal(Left(KoskiErrorCategory.unavailable.virta()))
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

  "Älä palauta kuori-opiskeluoikeuksia, ainoastaan sisältyvät" in {
    val oppija = KoskiSpecificMockOppijat.eero

    val kuori: AmmatillinenOpiskeluoikeus = createOpiskeluoikeus(oppija, defaultOpiskeluoikeus, user = stadinAmmattiopistoTallentaja)

    val sisältyväInput: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
      oppilaitos = Some(schema.Oppilaitos(omnia)),
      sisältyyOpiskeluoikeuteen = Some(schema.SisältäväOpiskeluoikeus(kuori.oppilaitos.get, kuori.oid.get)),
      suoritukset = List(
        defaultOpiskeluoikeus.suoritukset.head.asInstanceOf[schema.AmmatillisenTutkinnonOsittainenSuoritus].copy(
          toimipiste = schema.OidOrganisaatio(omnia)
        )
      )
    )

    val sisältyvä = createOpiskeluoikeus(oppija, sisältyväInput, user = MockUsers.omniaTallentaja)

    val result = suoritetutTutkinnotService.findSuoritetutTutkinnotOppija(oppija.oid)

    result.isRight should be(true)

    result.map(o => {
      verifyOppija(oppija, o)
      o.opiskeluoikeudet should have length 1

      o.opiskeluoikeudet.head.oppilaitos.map(_.oid) should equal(Some(MockOrganisaatiot.omnia))
      o.opiskeluoikeudet.head match {
        case koo: SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus => koo.oid should equal(Some(sisältyvä.oid.get))
      }
    })
  }

  private def lisääKorotettuSuoritus(oppija: LaajatOppijaHenkilöTiedot) = {
    val alkamispäivä = LocalDate.of(2023, 7, 1)
    val korotettuTutkinnonOsanSuoritus = osittaisenTutkinnonTutkinnonOsanSuoritus(k3, ammatillisetTutkinnonOsat, "100432", "Ympäristön hoitaminen", 35).copy(
      korotettu = Some(korotettu)
    )
    val korotettuYhteisenOsanOsaAlueenSuoritus = schema.YhteisenTutkinnonOsanOsaAlueenSuoritus(
      koulutusmoduuli = schema.PaikallinenAmmatillisenTutkinnonOsanOsaAlue(
        schema.PaikallinenKoodi("MA", "Matematiikka"), "Matematiikan opinnot", pakollinen = true, Some(schema.LaajuusOsaamispisteissä(3))
      ),
      arviointi = Some(List(arviointiKiitettävä)),
      korotettu = Some(korotettu)
    )
    val defaultOsanOsaAlueenSuoritukset = List(
      schema.YhteisenTutkinnonOsanOsaAlueenSuoritus(
        koulutusmoduuli = schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(schema.Koodistokoodiviite("FK", "ammatillisenoppiaineet"), pakollinen = true, Some(schema.LaajuusOsaamispisteissä(3))),
        arviointi = Some(List(arviointiKiitettävä)),
        tunnustettu = Some(tunnustettu)
      ),
      schema.YhteisenTutkinnonOsanOsaAlueenSuoritus(
        koulutusmoduuli = schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(schema.Koodistokoodiviite("TVT", "ammatillisenoppiaineet"), pakollinen = true, Some(schema.LaajuusOsaamispisteissä(3))),
        arviointi = Some(List(arviointiKiitettävä.copy(päivä = date(2015, 1, 1)))),
        alkamispäivä = Some(date(2014, 1, 1)),
        tunnustettu = Some(tunnustettu),
        lisätiedot = Some(List(lisätietoOsaamistavoitteet))
      )
    )
    lazy val korotettuYhteisenTutkinnonOsanSuoritus = yhteisenOsittaisenTutkinnonTutkinnonOsansuoritus(k3, yhteisetTutkinnonOsat, "101054", "Matemaattis-luonnontieteellinen osaaminen", 9).copy(
      osasuoritukset = Some(List(
        korotettuYhteisenOsanOsaAlueenSuoritus,
      ) ++ defaultOsanOsaAlueenSuoritukset)
    )

    def getAlkuperäinen: AmmatillinenOpiskeluoikeus = getOpiskeluoikeudet(oppija.oid).find(_.suoritukset.headOption.exists(_.tyyppi.koodiarvo == "ammatillinentutkinto")).map {
      case a: AmmatillinenOpiskeluoikeus => a
    }.get

    val korotettuSuoritus = ammatillisenTutkinnonOsittainenSuoritus.copy(
      korotettuOpiskeluoikeusOid = getAlkuperäinen.oid,
      korotettuKeskiarvo = Some(4.5),
      korotettuKeskiarvoSisältääMukautettujaArvosanoja = Some(false),
      osasuoritukset = Some(List(
        korotettuTutkinnonOsanSuoritus,
        korotettuYhteisenTutkinnonOsanSuoritus
      ))
    )

    val korotettuOo = makeOpiskeluoikeus(suoritus = korotettuSuoritus, alkamispäivä = alkamispäivä)

    putOpiskeluoikeus(korotettuOo, oppija) {
      verifyResponseStatusOk()
    }
  }

  private def makeOpiskeluoikeus(
    alkamispäivä: LocalDate = longTimeAgo,
    oppilaitos: schema.Oppilaitos = schema.Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto),
    suoritus: schema.AmmatillinenPäätasonSuoritus,
    tila: Option[schema.Koodistokoodiviite] = None
  ) = schema.AmmatillinenOpiskeluoikeus(
    tila = schema.AmmatillinenOpiskeluoikeudenTila(
      List(
        schema.AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen))
      ) ++ tila.map(t => schema.AmmatillinenOpiskeluoikeusjakso(date(2023, 12, 31), t, Some(valtionosuusRahoitteinen))).toList
    ),
    oppilaitos = Some(oppilaitos),
    suoritukset = List(suoritus)
  )

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
        actualOo: SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotEBTutkinnonSuoritus,
        expectedOoData: schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus,
        expectedSuoritusData: schema.EBTutkinnonSuoritus
        ) => verifyEBTutkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotDIAOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotDIATutkinnonSuoritus,
        expectedOoData: schema.DIAOpiskeluoikeus,
        expectedSuoritusData: schema.DIATutkinnonSuoritus
        ) => verifyDIATutkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus,
        expectedOoData: schema.YlioppilastutkinnonOpiskeluoikeus,
        expectedSuoritusData: schema.YlioppilastutkinnonSuoritus
      ) => verifyYlioppilastutkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case (
        actualOo: SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus,
        actualSuoritus: SuoritetutTutkinnotKorkeakoulututkinnonSuoritus,
        expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
        expectedSuoritusData: schema.KorkeakoulututkinnonSuoritus
        ) => verifyKorkeakoulututkinto(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)
      case _ => fail("Palautettiin tunnistamattoman tyyppistä dataa")
    }
  }

  private def verifyAmmatillinenTutkinto(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonSuoritus
  ): Unit = {
    verifyAmmatillinenOpiskeluoikeudenKentät(actualOo, expectedOoData)
    verifyAmmatillisenTutkinnonOsittainenTaiKokoSuoritus(actualSuoritus, expectedSuoritusData)

    actualSuoritus.osaamisala.map(_.length) should equal(expectedSuoritusData.osaamisala.map(_.length))
    actualSuoritus.tutkintonimike.map(_.length) should equal(expectedSuoritusData.tutkintonimike.map(_.length))
  }

  private def verifyAmmatillinenOsittainen(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: schema.AmmatillisenTutkinnonOsittainenSuoritus
  ): Unit = {
    verifyAmmatillinenOpiskeluoikeudenKentät(actualOo, expectedOoData)
    verifyAmmatillisenTutkinnonOsittainenTaiKokoSuoritus(actualSuoritus, expectedSuoritusData)

    actualSuoritus.toinenOsaamisala should equal(Some(expectedSuoritusData.toinenOsaamisala))
    actualSuoritus.toinenTutkintonimike should equal(Some(expectedSuoritusData.toinenTutkintonimike))
    actualSuoritus.korotettuOpiskeluoikeusOid should equal(expectedSuoritusData.korotettuOpiskeluoikeusOid)

    actualSuoritus.osaamisala.map(_.length) should equal(
      if (expectedSuoritusData.toinenOsaamisala) {
        expectedSuoritusData.osaamisala.map(_.length)
      } else {
        None
      }
    )
    actualSuoritus.tutkintonimike.map(_.length) should equal(
      if (expectedSuoritusData.toinenTutkintonimike) {
        expectedSuoritusData.tutkintonimike.map(_.length)
      } else {
        None
      }
    )
  }

  private def verifyMuuAmmatillinen(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus,
    expectedSuoritusData: schema.MuunAmmatillisenKoulutuksenSuoritus
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
  }

  private def verifyMuunAmmatillisenTutkinnonSuoritus(
    actualSuoritus: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus,
    expectedSuoritusData: schema.MuunAmmatillisenKoulutuksenSuoritus
  ): Unit = {
    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)

    actualSuoritus.koulutusmoduuli.laajuus.map(_.arvo) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.arvo))
    actualSuoritus.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo))
    actualSuoritus.koulutusmoduuli.laajuus.map(_.yksikkö.koodistoUri) should equal(expectedSuoritusData.koulutusmoduuli.laajuus.map(_.yksikkö.koodistoUri))
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
    actualSuoritus.suorituskieli.map(_.koodiarvo) should equal(Some(expectedSuoritusData.suorituskieli.koodiarvo))
  }

  private def verifyAmmatillinenOpiskeluoikeudenKentät(
    actualOo: SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
    expectedOoData: schema.AmmatillinenOpiskeluoikeus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.suoritukset.length should equal(1)
  }

  private def verifyYlioppilastutkinto(
    actualOo: SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus,
    expectedOoData: schema.YlioppilastutkinnonOpiskeluoikeus,
    expectedSuoritusData: schema.YlioppilastutkinnonSuoritus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.suoritukset.length should equal(1)

    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
  }

  private def verifyKorkeakoulututkinto(
    actualOo: SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotKorkeakoulututkinnonSuoritus,
    expectedOoData: schema.KorkeakoulunOpiskeluoikeus,
    expectedSuoritusData: schema.KorkeakoulututkinnonSuoritus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)

    actualOo.lisätiedot.map(_.virtaOpiskeluoikeudenTyyppi.map(_.koodiarvo)) should be(expectedOoData.lisätiedot.map(_.virtaOpiskeluoikeudenTyyppi.map(_.koodiarvo)))

    actualOo.suoritukset.length should equal(1)

    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo) should equal(expectedSuoritusData.koulutusmoduuli.koulutustyyppi.map(_.koodiarvo))
    actualSuoritus.koulutusmoduuli.virtaNimi should equal(expectedSuoritusData.koulutusmoduuli.virtaNimi)
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
  }

  private def verifyEBTutkinto(
    actualOo: SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotEBTutkinnonSuoritus,
    expectedOoData: schema.EuropeanSchoolOfHelsinkiOpiskeluoikeus,
    expectedSuoritusData: schema.EBTutkinnonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.suoritukset.length should equal(1)

    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.koulutusmoduuli.curriculum.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.curriculum.koodiarvo)
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
  }

  private def verifyDIATutkinto(
    actualOo: SuoritetutTutkinnotDIAOpiskeluoikeus,
    actualSuoritus: SuoritetutTutkinnotDIATutkinnonSuoritus,
    expectedOoData: schema.DIAOpiskeluoikeus,
    expectedSuoritusData: schema.DIATutkinnonSuoritus
  ): Unit = {
    verifyKoskiOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.suoritukset.length should equal(1)

    actualSuoritus.koulutusmoduuli.tunniste.koodiarvo should equal(expectedSuoritusData.koulutusmoduuli.tunniste.koodiarvo)
    actualSuoritus.toimipiste.map(_.oid) should equal(Some(expectedSuoritusData.toimipiste.oid))
    actualSuoritus.vahvistus.map(_.päivä) should equal(expectedSuoritusData.vahvistus.map(_.päivä))
    actualSuoritus.suorituskieli.map(_.koodiarvo) should equal(Some(expectedSuoritusData.suorituskieli.koodiarvo))
    actualSuoritus.tyyppi.koodiarvo should equal(expectedSuoritusData.tyyppi.koodiarvo)
  }

  private def verifyKoskiOpiskeluoikeudenKentät(
    actualOo: SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus,
    expectedOoData: schema.KoskeenTallennettavaOpiskeluoikeus
  ): Unit = {
    verifyOpiskeluoikeudenKentät(actualOo, expectedOoData)
    actualOo.oid should be(expectedOoData.oid)
    actualOo.versionumero should be(expectedOoData.versionumero)
    actualOo.sisältyyOpiskeluoikeuteen.map(_.oid) should equal(None)
  }

  private def verifyOpiskeluoikeudenKentät(
    actualOo: SuoritetutTutkinnotOpiskeluoikeus,
    expectedOoData: schema.Opiskeluoikeus
  ): Unit = {
    actualOo.oppilaitos.map(_.oid) should equal(expectedOoData.oppilaitos.map(_.oid))
    actualOo.koulutustoimija.map(_.oid) should equal(expectedOoData.koulutustoimija.map(_.oid))
    actualOo.tyyppi.koodiarvo should equal(expectedOoData.tyyppi.koodiarvo)
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
