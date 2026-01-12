package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.api.misc.{OpiskeluoikeusTestMethods, PutOpiskeluoikeusTestMethods}
import fi.oph.koski.db.KoskiTables
import fi.oph.koski.documentation.AmmatillinenExampleData.{ammatillisenTutkinnonOsittainenSuoritus, ammatillisetTutkinnonOsat, arviointiKiitettävä, k3, korotettu, lisätietoOsaamistavoitteet, osittaisenTutkinnonTutkinnonOsanSuoritus, tunnustettu, yhteisenOsittaisenTutkinnonTutkinnonOsansuoritus, yhteisetTutkinnonOsat}
import fi.oph.koski.documentation.ExampleData.{longTimeAgo, opiskeluoikeusLäsnä, valtionosuusRahoitteinen}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.KoskiSpecificSession.SUORITUSJAKO_KATSOMINEN_USER
import fi.oph.koski.koskiuser.MockUsers.stadinAmmattiopistoJaOppisopimuskeskusTallentaja
import fi.oph.koski.koskiuser.Rooli.OPHKATSELIJA
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession, KäyttöoikeusGlobal, MockUsers, Palvelurooli}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.{DatabaseTestMethods, KoskiApplicationForTests, KoskiHttpSpec, schema}
import fi.oph.koski.ytr.MockYtrClient
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot.omnia
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, Koodistokoodiviite}
import fi.oph.koski.virta.MockVirtaClient
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.fixture.AmmatillinenOpiskeluoikeusTestData
import fi.oph.koski.suoritetuttutkinnot.{SuoritetutTutkinnotAmmatillinenOpiskeluoikeus, SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus, SuoritetutTutkinnotDIAOpiskeluoikeus, SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus, SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus, SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus, SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus}
import fi.oph.koski.suoritusjako.SuoritetutTutkinnotOppijaJakolinkillä

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
  with SuoritetutTutkinnotVerifiers
{
  def tag: universe.TypeTag[schema.AmmatillinenOpiskeluoikeus] = implicitly[reflect.runtime.universe.TypeTag[schema.AmmatillinenOpiskeluoikeus]]
  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo, suoritus = ammatillisenTutkinnonOsittainenSuoritus)

  val suoritusjakoService = KoskiApplicationForTests.suoritusjakoService

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

  implicit val koskiSession: KoskiSpecificSession = suoritusjakoKatsominenTestUser

  override def afterEach(): Unit = {
    MockYtrClient.reset()
    super.afterEach()
  }

  "Kosken testioppijoiden tiedot voi hakea, ja ne joko palauttavat tiedot tai 404" in {
    // Tämä testi varmistaa, että mitään yllättäviä 500 tms. virheitä ei tapahdu, ja suuruusluokat on oikein, eli suurin osa testioppijoista löytyy, ja osa palauttaa 404.
    val oppijaOidit = KoskiSpecificMockOppijat.defaultOppijat
      .filter(o => o.henkilö.hetu.isEmpty || o.henkilö.hetu.exists(!KoskiApplicationForTests.virtaClient.asInstanceOf[MockVirtaClient].virheenAiheuttavaHetu(_)))
      .map(_.henkilö.oid)

    oppijaOidit.length should be > 100

    val results = oppijaOidit.map(suoritusjakoService.findSuoritetutTutkinnotOppija)

    val (onnistuu, eiOnnistu) = results.partition(_.isRight)

    val (eiOnnistu404, _) = eiOnnistu.partition(_.swap.toOption.get.statusCode == 404)

    onnistuu.length should be > 100
    eiOnnistu.length should be > 20
    eiOnnistu.length should be < 35
    eiOnnistu.length should be(eiOnnistu404.length)

    onnistuu.foreach(result => {
      result.isRight should be(true)

      def isSuoritusjakoTehty(oid: String): Option[Boolean] = runDbSync(KoskiTables.KoskiOpiskeluOikeudet.filter(_.oid === oid).map(_.suoritusjakoTehty).result).headOption

      result.foreach(
        _.opiskeluoikeudet.collect ({ case koo: SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus => koo })
          .foreach(_.oid.flatMap(isSuoritusjakoTehty).map(_ should be(true)))
      )
    })
  }

  "Oppija, josta ei ole tietoja Koskessa/YTR:ssä/Virrassa, palauttaa 404" in {
    val result = suoritusjakoService.findSuoritetutTutkinnotOppija(KoskiSpecificMockOppijat.vainMitätöityjäOpiskeluoikeuksia.oid)

    result.isLeft should be(true)
    result.swap.toOption.get.statusCode should be(404)
  }

  "Ammatillinen tutkinto" - {

    "vahvistetun tutkinnon tiedot palautetaan" in {
      val oppija = KoskiSpecificMockOppijat.ammattilainen

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo)
      val expectedSuoritusData = expectedOoData.suoritukset.head

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

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

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

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

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

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
      setupOppijaWithOpiskeluoikeus(AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis(), KoskiSpecificMockOppijat.amiksenKorottaja) {
        verifyResponseStatusOk()
      }

      val oppija = KoskiSpecificMockOppijat.amiksenKorottaja

      lisääKorotettuSuoritus(oppija)

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)
        o.opiskeluoikeudet should have length 1

        o.opiskeluoikeudet.head.suoritukset.head.tyyppi.koodiarvo should be("ammatillinentutkinto")
      })
    }

    "Monesta osaamisalasta palautetaan vain uusin" in {
      val oppija = KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

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

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

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

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

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

      val expectedOoData = getOpiskeluoikeus(oppija.oid, schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
      val expectedSuoritusData = expectedOoData.suoritukset.collectFirst { case eb: schema.EBTutkinnonSuoritus => eb }.get

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus]

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

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

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

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

      result.isRight should be(true)

      result.map(o => {
        verifyOppija(oppija, o)

        o.opiskeluoikeudet should have length 1
        o.opiskeluoikeudet.head shouldBe a[SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus]

        val actualOo = o.opiskeluoikeudet.head
        val actualSuoritus = actualOo.suoritukset.head

        verifyOpiskeluoikeusJaSuoritus(actualOo, actualSuoritus, expectedOoData, expectedSuoritusData)

        actualSuoritus.koulutusmoduuli.eurooppalainenTutkintojenViitekehysEQF should equal(Some(Koodistokoodiviite("7", "eqf")))
        actualSuoritus.koulutusmoduuli.kansallinenTutkintojenViitekehysNQF should equal(Some(Koodistokoodiviite("7", "nqf")))

        actualOo.asInstanceOf[SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus].lisätiedot.flatMap(_.opettajanPedagogisetOpinnot.map(_.map(_.koodiarvo))) should equal(Some(List("il")))
        actualOo.asInstanceOf[SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus].lisätiedot.flatMap(_.opetettavanAineenOpinnot.map(_.map(_.koodiarvo))) should equal(Some(List("aa")))
      })
    }
  }

  "Linkitetyt oppijat" - {
    "Masterin ja slaven tutkinnot palautetaan kummankin oideilla" in {
      val oppijaMaster = KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen
      val oppijaSlave = KoskiSpecificMockOppijat.slaveAmmattilainen

      val resultMaster = suoritusjakoService.findSuoritetutTutkinnotOppija(oppijaMaster.oid)
      val resultSlave = suoritusjakoService.findSuoritetutTutkinnotOppija(oppijaSlave.henkilö.oid)

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

      val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

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

    suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid).isRight should be(true)

    KoskiApplicationForTests.cacheManager.invalidateAllCaches()
    MockYtrClient.setFailureHetu(oppija.hetu.get)

    val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

    result.isRight should be(false)
    result should equal(Left(KoskiErrorCategory.unavailable.ytr()))
  }

  "jos Virta palauttaa virheen, palautetaan virhe" in {
    val oppija = KoskiSpecificMockOppijat.virtaEiVastaa

    val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

    result.isRight should be(false)
    result should equal(Left(KoskiErrorCategory.unavailable.virta()))
  }

  "jos YTR timeouttaa, palautetaan virhe" in {
    val oppija = KoskiSpecificMockOppijat.masterYlioppilasJaAmmattilainen

    suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid).isRight should be(true)

    KoskiApplicationForTests.cacheManager.invalidateAllCaches()
    MockYtrClient.setTimeoutHetu(oppija.hetu.get)

    val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

    result.isRight should be(false)
    result should equal(Left(KoskiErrorCategory.unavailable()))
  }

  "Älä palauta kuori-opiskeluoikeuksia, ainoastaan sisältyvät" in {
    val oppija = KoskiSpecificMockOppijat.eero

    setupOppijaWithOpiskeluoikeus(AmmatillinenOpiskeluoikeusTestData.opiskeluoikeus(MockOrganisaatiot.stadinAmmattiopisto, versio = Some(11)), KoskiSpecificMockOppijat.eero) {
      verifyResponseStatusOk()
    }

    val kuori: AmmatillinenOpiskeluoikeus = createOpiskeluoikeus(oppija, defaultOpiskeluoikeus, user = stadinAmmattiopistoJaOppisopimuskeskusTallentaja)

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

    val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

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
        koulutusmoduuli = schema.PaikallinenAmmatillisenTutkinnonOsanOsaAlue(schema.PaikallinenKoodi("MA", "Matematiikka"), "Matematiikan opinnot", pakollinen = false, Some(schema.LaajuusOsaamispisteissä(3))),
        arviointi = Some(List(arviointiKiitettävä)),
        tunnustettu = Some(tunnustettu)
      ),
      schema.YhteisenTutkinnonOsanOsaAlueenSuoritus(
        koulutusmoduuli = schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(schema.Koodistokoodiviite("FK", "ammatillisenoppiaineet"), pakollinen = true, Some(schema.LaajuusOsaamispisteissä(2))),
        arviointi = Some(List(arviointiKiitettävä)),
        tunnustettu = Some(tunnustettu)
      ),
      schema.YhteisenTutkinnonOsanOsaAlueenSuoritus(koulutusmoduuli = schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(schema.Koodistokoodiviite("FK", "ammatillisenoppiaineet"), pakollinen = false, Some(schema.LaajuusOsaamispisteissä(3))),
        arviointi = Some(List(arviointiKiitettävä)),
        tunnustettu = Some(tunnustettu)
      ),
      schema.YhteisenTutkinnonOsanOsaAlueenSuoritus(
        koulutusmoduuli = schema.ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(schema.Koodistokoodiviite("TVT", "ammatillisenoppiaineet"), pakollinen = true, Some(schema.LaajuusOsaamispisteissä(1))),
        arviointi = Some(List(arviointiKiitettävä.copy(päivä = date(2015, 1, 1)))),
        alkamispäivä = Some(date(2014, 1, 1)),
        tunnustettu = Some(tunnustettu),
        lisätiedot = Some(List(lisätietoOsaamistavoitteet))
      )
    )
    lazy val korotettuYhteisenTutkinnonOsanSuoritus = yhteisenOsittaisenTutkinnonTutkinnonOsansuoritus(k3, yhteisetTutkinnonOsat, "101054", "Matemaattis-luonnontieteellinen osaaminen", 12).copy(
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

  private def verifyOppija(expected: LaajatOppijaHenkilöTiedot, actual: SuoritetutTutkinnotOppijaJakolinkillä) = {
    actual.henkilö.oid should be(expected.oid)
    actual.henkilö.etunimet should be(expected.etunimet)
    actual.henkilö.kutsumanimi should be(expected.kutsumanimi)
    actual.henkilö.sukunimi should be(expected.sukunimi)
    actual.henkilö.syntymäaika should be(expected.syntymäaika)
  }

  private def verifyEiOpiskeluoikeuksia(oppija: LaajatOppijaHenkilöTiedot) = {
    val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppija.oid)

    result.isRight should be(true)

    result.map(o => {
      verifyOppija(oppija, o)

      o.opiskeluoikeudet should have length 0
    })
  }

  private def verifyEiLöydyTaiEiKäyttöoikeuksia(oppijaOid: String)(implicit user: KoskiSpecificSession): Unit = {
    val result = suoritusjakoService.findSuoritetutTutkinnotOppija(oppijaOid)(user)

    result.isLeft should be(true)
    result should equal(Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.")))
  }
}
