package fi.oph.koski.kela

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.ExampleData.opiskeluoikeusMitätöity
import fi.oph.koski.documentation.{EuropeanSchoolOfHelsinkiExampleData, ExamplesIB, ExamplesPerusopetus, ExamplesVapaaSivistystyöKotoutuskoulutus2022}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import fi.oph.koski.organisaatio.MockOrganisaatiot.{EuropeanSchoolOfHelsinki, MuuKuinSäänneltyKoulutusToimija}
import fi.oph.koski.schema.LocalizedString.finnish
import fi.oph.koski.schema
import fi.oph.koski.ytr.MockYtrClient
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.time.LocalDate
import scala.language.reflectiveCalls

class KelaSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethodsAmmatillinen
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach {

  override def afterEach(): Unit = {
    super.afterEach()
    MockYtrClient.reset()
  }

  "Kelan yhden oppijan rajapinta" - {
    "Yhden oppijan hakeminen onnistuu ja tuottaa auditlog viestin" in {
      AuditLogTester.clearMessages
      postHetu(KoskiSpecificMockOppijat.amis.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
      }
    }
    "Palautetaan 400 jos pyyntö tehdään epävalidilla hetulla" in {
      postHetu("230305A015A") {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Virheellinen tarkistusmerkki hetussa: 230305A015A"))
      }
    }
    "Palautetaan 404 jos opiskelijalla ei ole ollenkaan Kelaa kiinnostavia opiskeluoikeuksia" in {
      postHetu(KoskiSpecificMockOppijat.monimutkainenKorkeakoululainen.hetu.get) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
    "Oppijan opiskeluoikeuksista filtteröidään pois sellaiset opiskeluoikeuden tyypit jotka ei kiinnosta Kelaa" in {
      postHetu(KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[KelaOppija](body)

        response.henkilö.hetu should equal(KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia.hetu)
        response.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List(schema.OpiskeluoikeudenTyyppi.perusopetus.koodiarvo, schema.OpiskeluoikeudenTyyppi.perusopetus.koodiarvo, schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo))
      }
    }
    "Palauttaa TUVA opiskeluoikeuden tiedot" in {
      postHetu(KoskiSpecificMockOppijat.tuva.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        oppija.opiskeluoikeudet.length should be(1)

        val tuvaOpiskeluoikeus = oppija.opiskeluoikeudet.last match {
          case x: KelaTutkintokoulutukseenValmentavanOpiskeluoikeus => x
        }
        tuvaOpiskeluoikeus.oppilaitos.get.oid shouldBe "1.2.246.562.10.52251087186"
        tuvaOpiskeluoikeus.koulutustoimija.get.oid shouldBe "1.2.246.562.10.346830761110"
        tuvaOpiskeluoikeus.järjestämislupa.koodiarvo shouldBe "ammatillinen"
        tuvaOpiskeluoikeus.tila.opiskeluoikeusjaksot.last.tila.koodiarvo shouldBe "valmistunut"
        tuvaOpiskeluoikeus.suoritukset.length shouldBe 1
        tuvaOpiskeluoikeus.suoritukset.head.koulutusmoduuli.tunniste.koodiarvo shouldBe "999908"
        tuvaOpiskeluoikeus.suoritukset.head.koulutusmoduuli.perusteenDiaarinumero.get shouldBe "OPH-1488-2021"
        tuvaOpiskeluoikeus.suoritukset.head.koulutusmoduuli.laajuus.get.arvo shouldBe 12.0
        tuvaOpiskeluoikeus.suoritukset.head.osasuoritukset.get.length shouldBe 7
      }
    }

    "Palauttaa VST lukutaitokoulutuksen tiedot" in {
      postHetu(KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKoulutus.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)

        val vstOpiskeluoikeus = oppija.opiskeluoikeudet.collectFirst {
          case x: KelaVapaanSivistystyönOpiskeluoikeus => x
        }

        vstOpiskeluoikeus.get.oppilaitos.get.oid shouldBe "1.2.246.562.10.31915273374"
      }
    }

    "Palauttaa oppijan, jolla pelkkä YO-opiskeluoikeus, tiedot" in {
      val pelkkäYo = KoskiSpecificMockOppijat.ylioppilas
      postHetu(pelkkäYo.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        oppija.opiskeluoikeudet.length should be(1)
        val opiskeluoikeus = oppija.opiskeluoikeudet.collectFirst { case oo: KelaYlioppilastutkinnonOpiskeluoikeus => oo }.get

        opiskeluoikeus.oid should be (None)
        opiskeluoikeus.tyyppi should be (schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto)
        opiskeluoikeus.suoritukset.length shouldBe 1
      }
    }

    "Palauttaa TUVA-perusopetuksen erityisen tuen jaksot" in {
      postHetu(KoskiSpecificMockOppijat.tuvaPerus.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        oppija.opiskeluoikeudet.length should be(1)

        val tuvaOpiskeluoikeus = oppija.opiskeluoikeudet.last match {
          case x: KelaTutkintokoulutukseenValmentavanOpiskeluoikeus => x
        }
        tuvaOpiskeluoikeus.järjestämislupa.koodiarvo shouldBe "perusopetus"
        tuvaOpiskeluoikeus.lisätiedot.get.erityisenTuenPäätökset.get should have length (1)
      }
    }

    "Palauttaa perusopetuksen erityisen tuen jaksot" in {
      postHetu(KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)

        val perusopetus = oppija.opiskeluoikeudet collectFirst  {
          case x: KelaPerusopetuksenOpiskeluoikeus if x.lisätiedot.isDefined => x
        }

        perusopetus.get.lisätiedot.get.erityisenTuenPäätökset.get should have length (1)
        perusopetus.get.lisätiedot.get.erityisenTuenPäätökset.get.head.opiskeleeToimintaAlueittain should be (true)
      }
    }

    "Palauttaa tiedon 'täydentääTutkintoa' kun kyseessä on Muu ammatillinen koulutus" in {
      postHetu(KoskiSpecificMockOppijat.muuAmmatillinen.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        oppija.opiskeluoikeudet.length.shouldBe(1)
        oppija.opiskeluoikeudet.head.suoritukset.length.shouldBe(1)
        val suoritus = oppija.opiskeluoikeudet.head.suoritukset.head.asInstanceOf[KelaAmmatillinenPäätasonSuoritus]
        suoritus.täydentääTutkintoa.isEmpty.shouldEqual(false)
        suoritus.tyyppi.koodiarvo.shouldEqual("muuammatillinenkoulutus")
      }
    }

    "Palauttaa tiedot 'tutkinto' ja 'päättymispäivä' kun kyseessä on Näyttötutkintoon valmistava" in {
      postHetu(KoskiSpecificMockOppijat.erikoisammattitutkinto.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        oppija.opiskeluoikeudet.length.shouldBe(1)
        oppija.opiskeluoikeudet.head.suoritukset.length.shouldBe(2)
        val suoritus = oppija.opiskeluoikeudet.head.suoritukset.head.asInstanceOf[KelaAmmatillinenPäätasonSuoritus]
        suoritus.tutkinto.isEmpty.shouldEqual(false)
        suoritus.tyyppi.koodiarvo.shouldEqual("nayttotutkintoonvalmistavakoulutus")
        suoritus.päättymispäivä.isEmpty.shouldEqual(false)
      }
    }

    "Palauttaa tiedon oppisopimuksen purkamisesta" in {
      postHetu(KoskiSpecificMockOppijat.reformitutkinto.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        oppija.opiskeluoikeudet.length.shouldBe(1)
        oppija.opiskeluoikeudet.head.suoritukset.length.shouldBe(1)
        val suoritus = oppija.opiskeluoikeudet.head.suoritukset.head.asInstanceOf[KelaAmmatillinenPäätasonSuoritus]
        suoritus.osaamisenHankkimistavat.map(oht => oht.length).shouldBe(Some(2))
        suoritus.osaamisenHankkimistavat.map(oht => oht.last.osaamisenHankkimistapa.isInstanceOf[OppisopimuksellinenOsaamisenHankkimistapa]).shouldBe(Some(true))
        val hankkimistapa = suoritus.osaamisenHankkimistavat.map(oht => oht.last.osaamisenHankkimistapa.asInstanceOf[OppisopimuksellinenOsaamisenHankkimistapa])
        hankkimistapa.get.oppisopimus.oppisopimuksenPurkaminen.exists(_.päivä.equals(LocalDate.of(2013, 3, 20))).shouldBe(true)

        suoritus.koulutussopimukset.get.head.työssäoppimispaikanYTunnus shouldBe Some("1572860-0")
      }
    }

    "Palauttaa vaativan erityisen tuen yhteydessä järjestettävä majoitus -tiedon" - {
      "Kelan laajoilla oikeksilla" in {
        postHetu(KoskiSpecificMockOppijat.amis.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)
          oppija.opiskeluoikeudet.length.shouldBe(1)
          oppija.opiskeluoikeudet.head.suoritukset.length.shouldBe(1)
          oppija.opiskeluoikeudet.head.lisätiedot.get.asInstanceOf[KelaAmmatillisenOpiskeluoikeudenLisätiedot].vaativanErityisenTuenYhteydessäJärjestettäväMajoitus shouldBe (Some(
            List(KelaAikajakso(LocalDate.of(2012, 9, 1), Some(LocalDate.of(2013, 9, 1))))
          ))
        }
      }

      "Kelan suppeilla oikeksilla" in {
        postHetu(KoskiSpecificMockOppijat.amis.hetu.get, user = MockUsers.kelaSuppeatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)
          oppija.opiskeluoikeudet.length.shouldBe(1)
          oppija.opiskeluoikeudet.head.suoritukset.length.shouldBe(1)
          oppija.opiskeluoikeudet.head.lisätiedot.get.asInstanceOf[KelaAmmatillisenOpiskeluoikeudenLisätiedot].vaativanErityisenTuenYhteydessäJärjestettäväMajoitus shouldBe (Some(
            List(KelaAikajakso(LocalDate.of(2012, 9, 1), Some(LocalDate.of(2013, 9, 1))))
          ))
        }
      }
    }

    "Ei palauta mitätöityä opiskeluoikeutta" in {
      postHetu(KoskiSpecificMockOppijat.lukiolainen.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        oppija.opiskeluoikeudet.length should be(2)
      }
    }

    "Palauttaa rikkinäisen opiskeluoikeuden" in {
      postHetu(KoskiSpecificMockOppijat.kelaRikkinäinenOpiskeluoikeus.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        oppija.opiskeluoikeudet.length should be(1)

        val opiskeluoikeus = oppija.opiskeluoikeudet.last match {
          case x: KelaAmmatillinenOpiskeluoikeus => x
        }
        opiskeluoikeus.lisätiedot.get.majoitus.get.head shouldBe KelaAikajakso(
          alku = LocalDate.of(2022, 1, 1),
          loppu = Some(LocalDate.of(2020, 12, 31))
        )
        opiskeluoikeus.suoritukset.head.suoritustapa.get.koodiarvo shouldBe "rikkinäinenKoodi"
        opiskeluoikeus.suoritukset.head.osasuoritukset.get.head.lisätiedot.get.size shouldBe 1
        opiskeluoikeus.suoritukset.head.osasuoritukset.get.head.lisätiedot.get.head.tunniste.koodiarvo shouldBe "mukautettu"
      }
    }
    "Jos YTR-rajapinta palauttaa virheen, ei palauteta oppijan tietoja lainkaan" in {
      val hetu = KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia.hetu.get

      KoskiApplicationForTests.cacheManager.invalidateAllCaches
      MockYtrClient.setFailureHetu(hetu)

      postHetu(hetu) {
        verifyResponseStatus(500)
      }
    }
    "Jos YTR-rajapinta timeouttaa, ei palauteta oppijan tietoja lainkaan" in {
      val hetu = KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia.hetu.get

      KoskiApplicationForTests.cacheManager.invalidateAllCaches
      MockYtrClient.setTimeoutHetu(hetu)

      postHetu(hetu) {
        verifyResponseStatus(500)
      }
    }
    "Palauttaa koulutusvientitiedon" in {
      postHetu(KoskiSpecificMockOppijat.amisKoulutusvienti.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        val lisätiedot = oppija.opiskeluoikeudet.head.lisätiedot.get.asInstanceOf[KelaAmmatillisenOpiskeluoikeudenLisätiedot]
        lisätiedot.koulutusvienti shouldBe Some(true)
      }
    }
    "Palauttaa näytön arviointipäivän" in {
      postHetu(KoskiSpecificMockOppijat.ammatillisenOsittainenRapsa.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        val suoritus = oppija.opiskeluoikeudet.head.suoritukset.head.asInstanceOf[KelaAmmatillinenPäätasonSuoritus]
        val näyttöjenArviointipäivät = suoritus.osasuoritukset.get.flatMap(_.näyttö).map(_.arviointi.map(_.päivä))
        näyttöjenArviointipäivät shouldBe List(Some(LocalDate.of(2014, 10, 20)))
      }
    }
    "Palauttaa IB:n predicted grade -arvioinnin" in {
      postHetu(KoskiSpecificMockOppijat.ibPredicted.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        val suoritus = oppija.opiskeluoikeudet.head.suoritukset
          .collect { case s: KelaIBPäätasonSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "301102" => s }
          .head
        val osasuoritus = suoritus.osasuoritukset.get.head.asInstanceOf[KelaIBOppiaineenSuoritus]
        osasuoritus.predictedArviointi.get.head.päivä shouldBe Some(LocalDate.of(2016, 6, 4))
      }
    }
    "Palauttaa IB:n extendEssayn koulutusmoduulissa oppiaineen tiedot" in {
      postHetu(KoskiSpecificMockOppijat.ibFinal.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[KelaOppija](body)
        val suoritus = oppija.opiskeluoikeudet.head.suoritukset
          .collect { case s: KelaIBPäätasonSuoritus if s.tyyppi.koodiarvo == "ibtutkinto" => s }
          .head
        val actualOppiaine =
          suoritus.extendedEssay.map(_.koulutusmoduuli).map(_.aine).get

        val expectedOppiaine =
          ExamplesIB.opiskeluoikeus.suoritukset.collectFirst { case s: schema.IBTutkinnonSuoritus => s}.map(_.extendedEssay).get.map(_.koulutusmoduuli).map(_.aine).get
            .asInstanceOf[schema.IBOppiaineLanguage]

        actualOppiaine.tunniste.koodiarvo should equal(expectedOppiaine.tunniste.koodiarvo)
        actualOppiaine.kieli.map(_.koodiarvo) should equal(Some(expectedOppiaine.kieli.koodiarvo))
      }
    }
  }

  "Usean oppijan rajapinta" - {
    "Voidaan hakea usea oppija, jos jollain oppijalla ei löydy Kosken kantaan tallennettuja opintoja tai hetu on epävalidi, se puuttuu vastauksesta" in {
      val epävalidiHetu = "230305A015A"
      val hetut = List(
        KoskiSpecificMockOppijat.amis,
        KoskiSpecificMockOppijat.ibFinal,
        KoskiSpecificMockOppijat.koululainen,
        KoskiSpecificMockOppijat.ylioppilas
      ).map(_.hetu.get) ++ List(epävalidiHetu)

      postHetut(hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[KelaOppija]](body)
        response.map(_.henkilö.hetu.get).sorted should equal(hetut.sorted.filterNot(List(KoskiSpecificMockOppijat.ylioppilas.hetu.get, epävalidiHetu).contains))
      }
    }
    "Luo AuditLogin" in {
      AuditLogTester.clearMessages
      postHetut(List(KoskiSpecificMockOppijat.amis.hetu.get)) {
        verifyResponseStatusOk()
        AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
      }
    }
    "Ei luo AuditLogia jos hetulla löytyvä oppija puuttuu vastauksesta" in {
      AuditLogTester.clearMessages
      postHetut(List(KoskiSpecificMockOppijat.korkeakoululainen.hetu.get)) {
        verifyResponseStatusOk()
        AuditLogTester.getLogMessages.length should equal(0)
      }
    }
    "Sallitaan 1000 hetua" in {
      val hetut = List.fill(1000)(KoskiSpecificMockOppijat.amis.hetu.get)
      postHetut(hetut) {
        verifyResponseStatusOk()
      }
    }
    "Ei sallita yli 1000 hetua" in {
      val hetut = List.fill(1001)(KoskiSpecificMockOppijat.amis.hetu.get)
      postHetut(hetut) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest("Liian monta hetua, enintään 1000 sallittu"))
      }
    }
    "Palauttaa TUVA opiskeluoikeuden tiedot" in {
      postHetut(List(KoskiSpecificMockOppijat.tuvaPerus.hetu.get), user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[List[KelaOppija]](body).head
        oppija.opiskeluoikeudet.length should be(1)

        val tuvaOpiskeluoikeus = oppija.opiskeluoikeudet.last match {
          case x: KelaTutkintokoulutukseenValmentavanOpiskeluoikeus => x
        }
        tuvaOpiskeluoikeus.oppilaitos.get.oid shouldBe "1.2.246.562.10.52251087186"
        tuvaOpiskeluoikeus.koulutustoimija.get.oid shouldBe "1.2.246.562.10.346830761110"
        tuvaOpiskeluoikeus.järjestämislupa.koodiarvo shouldBe "perusopetus"
        tuvaOpiskeluoikeus.tila.opiskeluoikeusjaksot.last.tila.koodiarvo shouldBe "lasna"
        tuvaOpiskeluoikeus.suoritukset.length shouldBe 1
        tuvaOpiskeluoikeus.suoritukset.head.koulutusmoduuli.tunniste.koodiarvo shouldBe "999908"
        tuvaOpiskeluoikeus.suoritukset.head.koulutusmoduuli.perusteenDiaarinumero.get shouldBe "OPH-1488-2021"
        tuvaOpiskeluoikeus.suoritukset.head.koulutusmoduuli.laajuus shouldBe None
        tuvaOpiskeluoikeus.suoritukset.head.osasuoritukset.get.length shouldBe 3
      }
    }
    "Palauttaa rikkinäisen opiskeluoikeuden" in {
      postHetut(List(KoskiSpecificMockOppijat.kelaRikkinäinenOpiskeluoikeus.hetu.get), user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val oppija = JsonSerializer.parse[List[KelaOppija]](body).head
        oppija.opiskeluoikeudet.length should be(1)

        val opiskeluoikeus = oppija.opiskeluoikeudet.last match {
          case x: KelaAmmatillinenOpiskeluoikeus => x
        }
        opiskeluoikeus.lisätiedot.get.majoitus.get.head shouldBe KelaAikajakso(
          alku = LocalDate.of(2022, 1, 1),
          loppu = Some(LocalDate.of(2020, 12, 31))
        )
        opiskeluoikeus.suoritukset.head.suoritustapa.get.koodiarvo shouldBe "rikkinäinenKoodi"
        opiskeluoikeus.suoritukset.head.osasuoritukset.get.head.lisätiedot.get.size shouldBe 1
        opiskeluoikeus.suoritukset.head.osasuoritukset.get.head.lisätiedot.get.head.tunniste.koodiarvo shouldBe "mukautettu"
      }
    }
  }

  "Kelan käyttöoikeudet" - {
    "Suppeilla Kelan käyttöoikeuksilla ei näe kaikkia lisätietoja" in {
      postHetu(KoskiSpecificMockOppijat.amis.hetu.get, user = MockUsers.kelaSuppeatOikeudet) {
        verifyResponseStatusOk()
        val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
        val lisatiedot = opiskeluoikeudet.head.lisätiedot.get match {
          case l: KelaAmmatillisenOpiskeluoikeudenLisätiedot => l
        }

        lisatiedot.hojks should equal(None)

        opiskeluoikeudet.length should be(1)
      }
    }
    "Suppeilla Kelan käyttöoikeuksilla ei näe kaikkia lisätietoja historian kautta" in {
      val ooOid = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis).oid.get

      val ooVersio = getOpiskeluoikeudenVersio(ooOid, 1, user = MockUsers.kelaSuppeatOikeudet) {
        verifyResponseStatusOk()
        JsonSerializer.parse[KelaOppija](body)
      }

      val lisatiedot = ooVersio.opiskeluoikeudet.head.lisätiedot.get match {
        case l: KelaAmmatillisenOpiskeluoikeudenLisätiedot => l
      }

      lisatiedot.hojks should equal(None)

      ooVersio.opiskeluoikeudet.length should be(1)
    }

    "Laajoilla Kelan käyttöoikeuksilla näkee kaikki KelaSchema:n lisätiedot" in {
      postHetu(KoskiSpecificMockOppijat.amis.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
        val lisatiedot = opiskeluoikeudet.head.lisätiedot.get match {
          case l: KelaAmmatillisenOpiskeluoikeudenLisätiedot => l
        }

        lisatiedot.hojks shouldBe(defined)

        opiskeluoikeudet.length should be(1)
      }
    }
    "Osasuorituksen yksilöllistetty oppimäärä" - {
      def verify(user: MockUser, yksilöllistettyOppimääräShouldShow: Boolean): Unit = {
        postHetu(KoskiSpecificMockOppijat.koululainen.hetu.get, user = user) {
          verifyResponseStatusOk()
          val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
          val osasuoritukset = opiskeluoikeudet.flatMap(_.suoritukset.flatMap(_.osasuoritukset)).flatten.flatMap {
            case os: YksilöllistettyOppimäärä => Some(os)
            case _ => None
          }
          osasuoritukset.exists(_.yksilöllistettyOppimäärä.isDefined) shouldBe(yksilöllistettyOppimääräShouldShow)
        }
      }
      "Näkyy laajoilla käyttöoikeuksilla" in {
        verify(MockUsers.kelaLaajatOikeudet, yksilöllistettyOppimääräShouldShow = true)
      }
      "Ei näy suppeilla käyttöoikeuksilla" in {
        verify(MockUsers.kelaSuppeatOikeudet, yksilöllistettyOppimääräShouldShow = false)
      }
    }
    "Osasuoritusten lisätiedot" - {
      "Ei näy suppeilla käyttöoikeuksilla" in {
        postHetu(KoskiSpecificMockOppijat.ammattilainen.hetu.get, MockUsers.kelaSuppeatOikeudet) {
          verifyResponseStatusOk()
          val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
          val osasuoritukset = opiskeluoikeudet.flatMap(_.suoritukset.flatMap(_.osasuoritukset)).flatten.map{
            case os: KelaAmmatillinenOsasuoritus => os
          }
          osasuoritukset.exists(_.lisätiedot.isDefined) shouldBe(false)
        }
      }
      "Näkyy laajoilla käyttöoikeuksilla vain jos lisätietojen tunnisteen koodiarvo on 'mukautettu'" in {
        postHetu(KoskiSpecificMockOppijat.ammattilainen.hetu.get, MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
          val osasuoritukset = opiskeluoikeudet.flatMap(_.suoritukset.flatMap(_.osasuoritukset)).flatten.map{
            case os: KelaAmmatillinenOsasuoritus => os
          }
          osasuoritukset.flatMap(_.lisätiedot).flatten.map(_.tunniste.koodiarvo) should equal(List("mukautettu"))
        }
      }
    }
  }

  "Vapaan sivistystyön opiskeluoikeuksista ei välitetä vapaatavoitteisin koulutuksen suorituksia" in {
    postHetu(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.hetu.get) {
      verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
    }
  }

  "Arvosanadataa sisältäviltä vaikuttavia kenttiä ei palauteta" in {
    var iteraatioLkm = 0

    val kaikkiHetut: Seq[String] = KoskiSpecificMockOppijat.defaultOppijat
      .map(_.henkilö.hetu)
      .filterNot(_.isEmpty)
      .map(_.get)
      .toSet
      .toSeq

    kaikkiHetut
      .foreach(hetu => {
        postHetu(hetu, user = MockUsers.kelaLaajatOikeudet) {
          if (response.status == 200) {
            val oppija = JsonSerializer.parse[KelaOppija](body)
            withClue(s"${hetu} ${oppija.henkilö.sukunimi} ${oppija.henkilö.etunimet} ${oppija.opiskeluoikeudet.map(_.tyyppi.koodiarvo).mkString(",")}") {
              iteraatioLkm = iteraatioLkm + 1
              body.toLowerCase.contains("arvosana") should be(false)
              body.toLowerCase.contains("taitotaso") should be(false)
            }
          }
        }
      })

    iteraatioLkm should be > (120)
  }

  "Opiskeluoikeushistoria" - {
    lazy val historiaFixture = new {
      resetFixtures

      val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis)

      luoVersiohistoriaanRivi(KoskiSpecificMockOppijat.amis, opiskeluoikeus.asInstanceOf[schema.AmmatillinenOpiskeluoikeus])

      val slaveOppijanOpiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.slave.henkilö)

      val mitätöitäväOo = putAndGetOpiskeluoikeus(ExamplesPerusopetus.ysinOpiskeluoikeusKesken, KoskiSpecificMockOppijat.tyhjä)
    }

    "Luovutuspalvelu-API" - {

      "Opiskeluoikeuden versiohistorian haku tuottaa AuditLogin" in {

        AuditLogTester.clearMessages

        getVersiohistoria(historiaFixture.opiskeluoikeus.oid.get) {
          verifyResponseStatusOk()
          val history = JsonSerializer.parse[List[KelaOpiskeluoikeusHistoryPatch]](body)

          history.length should equal(2)
          AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "MUUTOSHISTORIA_KATSOMINEN", "target" -> Map("opiskeluoikeusOid" -> historiaFixture.opiskeluoikeus.oid.get)))
        }
      }

      "Tietyn version haku opiskeluoikeudesta tuottaa AuditLogin" in {
        AuditLogTester.clearMessages

        getOpiskeluoikeudenVersio(historiaFixture.opiskeluoikeus.oid.get, 1) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[KelaOppija](body)

          response.opiskeluoikeudet.headOption.flatMap(_.versionumero) should equal(Some(1))
          AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
        }
      }

      "Timestampit ovat samat historialistauksessa ja opiskeluoikeudessa" in {
        val historia = getVersiohistoria(historiaFixture.opiskeluoikeus.oid.get) {
          verifyResponseStatusOk()
          JsonSerializer.parse[List[KelaOpiskeluoikeusHistoryPatch]](body)
        }

        val ooVersio = getOpiskeluoikeudenVersio(historiaFixture.opiskeluoikeus.oid.get, 1) {
          verifyResponseStatusOk()
          JsonSerializer.parse[KelaOppija](body)
        }

        ooVersio.opiskeluoikeudet(0).aikaleima should equal(Some(historia(0).aikaleima))
      }

      "Slave-oppijalle tallennetun opiskeluoikeuden versiohistoria voidaan hakea ja palautuu master-oppijan tietojen kanssa" in {
        val historia = getVersiohistoria(historiaFixture.slaveOppijanOpiskeluoikeus.oid.get) {
          verifyResponseStatusOk()
          JsonSerializer.parse[List[KelaOpiskeluoikeusHistoryPatch]](body)
        }

        val ooVersio = getOpiskeluoikeudenVersio(historiaFixture.slaveOppijanOpiskeluoikeus.oid.get, historia.last.versionumero) {
          verifyResponseStatusOk()
          JsonSerializer.parse[KelaOppija](body)
        }

        val masterOppija = KoskiSpecificMockOppijat.master

        val expectedHenkilo = Henkilo(
          oid = masterOppija.oid,
          hetu = masterOppija.hetu,
          syntymäaika = masterOppija.syntymäaika,
          etunimet = masterOppija.etunimet,
          sukunimi = masterOppija.sukunimi,
          kutsumanimi = masterOppija.kutsumanimi
        )

        ooVersio.henkilö should equal(expectedHenkilo)
      }

      "Ei palauta mitätöidyn opiskeluoikeuden tietoja" in {
        getVersiohistoria(historiaFixture.mitätöitäväOo.oid.get) {
          verifyResponseStatusOk()
        }

        getOpiskeluoikeudenVersio(historiaFixture.mitätöitäväOo.oid.get, 1) {
          verifyResponseStatusOk()
        }

        val mitätöitynä = historiaFixture.mitätöitäväOo.copy(tila =
          historiaFixture.mitätöitäväOo.tila.copy(opiskeluoikeusjaksot =
            historiaFixture.mitätöitäväOo.tila.opiskeluoikeusjaksot :+ schema.NuortenPerusopetuksenOpiskeluoikeusjakso(alku = LocalDate.now, opiskeluoikeusMitätöity)
          )
        )

        putOpiskeluoikeus(mitätöitynä, KoskiSpecificMockOppijat.tyhjä) {
          verifyResponseStatusOk()
        }

        getVersiohistoria(historiaFixture.mitätöitäväOo.oid.get) {
          verifyResponseStatus(404, Nil)
        }

        getOpiskeluoikeudenVersio(historiaFixture.mitätöitäväOo.oid.get, 1) {
          verifyResponseStatus(404, Nil)
        }
       }

      "Jos opiskeluoikeus voidaan hakea hetulla, saadaan sama opiskeluoikeus myös historian kautta" in {
        var iteraatioLkm = 0

        val kaikkiHetut: Seq[String] = KoskiSpecificMockOppijat.defaultOppijat
          .map(_.henkilö.hetu)
          .filterNot(_.isEmpty)
          .map(_.get)
          .toSet
          .toSeq

        kaikkiHetut
          .foreach(hetu => {
            withClue(s"${hetu}") {
              val oppija = postHetu(hetu, user = MockUsers.kelaLaajatOikeudet) {
                if (response.status == 200) {
                  Some(JsonSerializer.parse[KelaOppija](body))
                } else {
                  None
                }
              }

              oppija.map(_.opiskeluoikeudet.filterNot(_.oid.isEmpty).map(oo => {
                val oppijaHistorianKautta = getOpiskeluoikeudenVersio(oo.oid.get, oo.versionumero.get) {
                  verifyResponseStatusOk()
                  JsonSerializer.parse[KelaOppija](body)
                }

                val ooHistorianKautta = oppijaHistorianKautta.opiskeluoikeudet(0)

                ooHistorianKautta should equal(oo)

                iteraatioLkm = iteraatioLkm + 1
              }))
            }
          })

        iteraatioLkm should be >(130)
      }

      "Jos opiskeluoikeuden historia voidaan hakea, saadaan sama opiskeluoikeus haettua myös Kelan oppija-API:lla" in {
        var iteraatioLkm = 0
        koskeenTallennetutOppijat
          .foreach(oppija => {
            val henkilö = oppija.henkilö.asInstanceOf[schema.Henkilötiedot]

            oppija.opiskeluoikeudet.filter(_.oid.isDefined).foreach(oo => {
              val henkilöTunniste = henkilö.hetu.getOrElse(henkilö.sukunimi + henkilö.etunimet)
              val ooOid = oo.oid.get
              val ooVersio = oo.versionumero.get
              val ooTunniste = oo.tyyppi.koodiarvo + " - " + ooOid

              withClue(s"${henkilöTunniste} - ${ooTunniste}") {
                val maybeOppijaHistorianKautta = getOpiskeluoikeudenVersio(oo.oid.get, ooVersio) {
                  if (response.status == 200) {
                    Some(JsonSerializer.parse[KelaOppija](body))
                  } else {
                    None
                  }
                }

                maybeOppijaHistorianKautta match {
                  case Some(oppijaHistorianKautta) => {
                    // Kela-API:n kautta pystyy hakemaan vain hetullisia oppijoita, joten historiankaan kautta ei muun pitäisi onnistua
                    oppijaHistorianKautta.henkilö.hetu.isDefined should be(true)

                    val ooHistorianKautta = oppijaHistorianKautta.opiskeluoikeudet(0)

                    val oo = postHetu(oppijaHistorianKautta.henkilö.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
                      verifyResponseStatusOk()
                      JsonSerializer.parse[KelaOppija](body)
                    }
                      .opiskeluoikeudet
                      .find(_.oid == ooHistorianKautta.oid)
                      .get

                    oo should equal(ooHistorianKautta)

                    iteraatioLkm = iteraatioLkm + 1
                  }
                  case _ =>
                }
              }
            })
          })

        iteraatioLkm should be >(130)
      }
    }

    "UI" - {
      "Opiskeluoikeuden versiohistorian haku tuottaa AuditLogin" in {
        AuditLogTester.clearMessages

        getVersiohistoriaUI(historiaFixture.opiskeluoikeus.oid.get) {
          verifyResponseStatusOk()
          val history = JsonSerializer.parse[List[OpiskeluoikeusHistoryPatch]](body)

          history.length should equal(2)
          AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "MUUTOSHISTORIA_KATSOMINEN", "target" -> Map("opiskeluoikeusOid" -> historiaFixture.opiskeluoikeus.oid.get)))
        }
      }

      "Tietyn version haku opiskeluoikeudesta tuottaa AuditLogin" in {
        AuditLogTester.clearMessages

        getOpiskeluoikeudenVersioUI(KoskiSpecificMockOppijat.amis.oid, historiaFixture.opiskeluoikeus.oid.get, 1) {
          verifyResponseStatusOk()
          val response = JsonSerializer.parse[KelaOppija](body)

          response.opiskeluoikeudet.headOption.flatMap(_.versionumero) should equal(Some(1))
          AuditLogTester.verifyLastAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
        }
      }
    }
  }

  "Hetu ei päädy lokiin" in {
    AccessLogTester.clearMessages
    val maskedHetu = "******-****"
    getHetu(KoskiSpecificMockOppijat.amis.hetu.get) {
      verifyResponseStatusOk()
      AccessLogTester.getLatestMatchingAccessLog("/koski/kela") should include(maskedHetu)
    }
  }

  "Palauttaa muun kuin säännellyn koulutuksen opiskeluoikeuden" in {
    postHetu(KoskiSpecificMockOppijat.jotpaMuuKuinSäänneltySuoritettu.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
      verifyResponseStatusOk()

      val oppija = JsonSerializer.parse[KelaOppija](body)
      oppija.opiskeluoikeudet.length should be(1)

      val muksOpiskeluoikeus = oppija.opiskeluoikeudet.last match {
        case x: KelaMUKSOpiskeluoikeus => x
      }

      muksOpiskeluoikeus.oppilaitos.get.oid shouldBe MuuKuinSäänneltyKoulutusToimija.oppilaitos
      muksOpiskeluoikeus.koulutustoimija.get.oid shouldBe MuuKuinSäänneltyKoulutusToimija.koulutustoimija
      muksOpiskeluoikeus.tila.opiskeluoikeusjaksot.last.tila.koodiarvo shouldBe "hyvaksytystisuoritettu"
      muksOpiskeluoikeus.suoritukset.length shouldBe 1

      val päätasonSuoritus = muksOpiskeluoikeus.suoritukset.head
      päätasonSuoritus.koulutusmoduuli.tunniste.koodiarvo shouldBe "999951"
      päätasonSuoritus.koulutusmoduuli.opintokokonaisuus.koodiarvo shouldBe "1138"
      päätasonSuoritus.koulutusmoduuli.opintokokonaisuus.nimi shouldBe Some(schema.Finnish("Kuvallisen ilmaisun perusteet ja välineet"))

      päätasonSuoritus.arviointi.foreach(_.foreach(a => {
        a.arvosana should be (None)
        a.hyväksytty should be (Some(true))
      }))

      päätasonSuoritus.osasuoritukset shouldNot be(None)
      päätasonSuoritus.osasuoritukset.get.length shouldBe 3

      val osasuoritusMaalaus = päätasonSuoritus.osasuoritukset.get.find(_.koulutusmoduuli.tunniste.koodiarvo == "Maalaus").get
      osasuoritusMaalaus.koulutusmoduuli.tunniste.nimi shouldBe Some(finnish("Maalaus"))
      osasuoritusMaalaus.arviointi.foreach(_.foreach(a => {
        a.arvosana should be (None)
        a.hyväksytty should be (Some(true))
      }))

      val osasuoritusGrafiikka = päätasonSuoritus.osasuoritukset.get.find(_.koulutusmoduuli.tunniste.koodiarvo == "Grafiikka").get
      osasuoritusGrafiikka.koulutusmoduuli.tunniste.nimi shouldBe Some(finnish("Grafiikka"))
      osasuoritusGrafiikka.arviointi.foreach(_.foreach(a => {
        a.arvosana should be (None)
        a.hyväksytty should be (Some(true))
      }))

      val osasuoritusValokuvaus = päätasonSuoritus.osasuoritukset.get.find(_.koulutusmoduuli.tunniste.koodiarvo == "Valokuvaus").get
      osasuoritusValokuvaus.koulutusmoduuli.tunniste.nimi shouldBe Some(finnish("Valokuvaus"))
      osasuoritusValokuvaus.arviointi.foreach(_.foreach(a => {
        a.arvosana should be (None)
        a.hyväksytty should be (Some(false))
      }))
    }
  }

  "Palauttaa European School of Helsinki -opiskeluoikeuden" in {
    postHetu(KoskiSpecificMockOppijat.europeanSchoolOfHelsinki.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
      val oppija = JsonSerializer.parse[KelaOppija](body)
      val eshOpiskeluoikeus = oppija.opiskeluoikeudet.collectFirst { case x: KelaESHOpiskeluoikeus => x }.get

      eshOpiskeluoikeus.oppilaitos.get.oid shouldBe EuropeanSchoolOfHelsinki.oppilaitos
      eshOpiskeluoikeus.koulutustoimija.get.oid shouldBe EuropeanSchoolOfHelsinki.koulutustoimija

      val s5 = eshOpiskeluoikeus.suoritukset.collectFirst {
        case s: KelaESHSecondaryLowerVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "S5" => s
      }
      s5 shouldNot be(None)
      s5.get.osasuoritukset.get.length shouldBe EuropeanSchoolOfHelsinkiExampleData.secondaryLowerSuoritus45("S5", LocalDate.now(), false).osasuoritukset.get.length

      val s6 = eshOpiskeluoikeus.suoritukset.collectFirst {
        case s: KelaESHSecondaryUpperVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "S6" => s
      }
      s6 shouldNot be(None)
      s6.get.osasuoritukset.get.length shouldBe EuropeanSchoolOfHelsinkiExampleData.secondaryUpperSuoritusS6("S6", LocalDate.now(), false).osasuoritukset.get.length

      val s7 = eshOpiskeluoikeus.suoritukset.collectFirst {
        case s: KelaESHSecondaryUpperVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "S7" => s
      }
      s7 shouldNot be(None)
      val expectedS7Osasuoritukset = EuropeanSchoolOfHelsinkiExampleData.secondaryUpperSuoritusS7("S7", LocalDate.now(), false).osasuoritukset.get
      s7.get.osasuoritukset.get.length shouldBe expectedS7Osasuoritukset.length
      expectedS7Osasuoritukset.zip(s7.get.osasuoritukset.get).foreach {
        case (expected :schema.SecondaryUpperOppiaineenSuoritus, actual: KelaESHSecondaryUpperOppiaineenSuoritusS7) =>
          actual.tyyppi shouldBe expected.tyyppi
          expected.osasuoritukset.fold({ actual.osasuoritukset shouldBe None; () }) { osasuoritukset =>
            osasuoritukset.length shouldBe actual.osasuoritukset.get.length
            osasuoritukset.zip(actual.osasuoritukset.get).foreach {
              case (expectedOsasuoritukset, actualOsasuoritukset) => expectedOsasuoritukset.tyyppi shouldBe actualOsasuoritukset.tyyppi
            }
          }
        case (_, actual) => throw new Error(s"Unexpected type: $actual")
      }

      s5.foreach(_.osasuoritukset.foreach(_.foreach(os =>{
        os.arviointi.foreach(_.foreach(a => {
          a.arvosana should be (None)
          a.hyväksytty.isDefined should be (true)
        }))
      })))

      s6.foreach(_.osasuoritukset.foreach(_.foreach(os => {
        os.asInstanceOf[KelaESHSecondaryUpperOppiaineenSuoritusS6].arviointi.foreach(_.foreach(a => {
          a.arvosana should be(None)
          a.hyväksytty.isDefined should be(true)
        }))
      })))

      s7.foreach(_.osasuoritukset.foreach(_.foreach(os => {
        os.asInstanceOf[KelaESHSecondaryUpperOppiaineenSuoritusS7].arviointi.foreach(_.foreach(a => {
          a.arvosana should be(None)
          a.hyväksytty.isDefined should be(true)
        }))
        os.asInstanceOf[KelaESHSecondaryUpperOppiaineenSuoritusS7].osasuoritukset.foreach(_.foreach(_.arviointi.foreach(_.foreach( a => {
          a.arvosana should be(None)
          a.hyväksytty.isDefined should be(true)
        }))))
      })))


    }
  }

  "Palauttaa muun vapaan sivistystyön koulutuksen kuin KOTO 2022 opiskeuoikeuden" in {
    postHetu(KoskiSpecificMockOppijat.vapaaSivistystyöOppivelvollinen.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
      verifyResponseStatusOk()
      val oppija = JsonSerializer.parse[KelaOppija](body)
      oppija.opiskeluoikeudet.length should be(1)
    }
  }

  "Palauttaa VST KOTO 2022 opiskeluoikeus" in {
    postHetu(KoskiSpecificMockOppijat.vstKoto2022Suorittanut.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
      verifyResponseStatusOk()

      val oppija = JsonSerializer.parse[KelaOppija](body)
      oppija.opiskeluoikeudet.length shouldBe 1

      val opiskeluoikeus = oppija.opiskeluoikeudet.last match { case x: KelaVapaanSivistystyönOpiskeluoikeus => x }

      opiskeluoikeus.suoritukset.length shouldBe 1
      val päätasonSuoritus = opiskeluoikeus.suoritukset.head match { case x: KelaVSTKOTO2022Suoritus => x }

      päätasonSuoritus.osasuoritukset shouldNot be(None)

      val expectedOsasuoritukset = ExamplesVapaaSivistystyöKotoutuskoulutus2022.PäätasonSuoritus.suoritettu.osasuoritukset.get
      päätasonSuoritus.osasuoritukset.get.length shouldBe expectedOsasuoritukset.length
      päätasonSuoritus.osasuoritukset.get.zip(expectedOsasuoritukset).foreach {
        case (actualOsasuoritus, expectedOsasuoritus) =>
          actualOsasuoritus.tyyppi shouldBe expectedOsasuoritus.tyyppi
          actualOsasuoritus.osasuoritukset.foreach(_.foreach { osasuoritus =>
            osasuoritus.arviointi.foreach(_.foreach { arviointi =>
              arviointi.arvosana shouldBe None
            })
          })
      }
    }
  }

  "Palauttaa lukio-opintoihin liittyvät rahoitustiedot, puhvit yms, ilman tarkkoja arvosanoja" in {
    postHetu(KoskiSpecificMockOppijat.uusiLukio.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
      verifyResponseStatusOk()

      val oppija = JsonSerializer.parse[KelaOppija](body)
      oppija.opiskeluoikeudet.length should be(1)

      val lukioOpiskeluoikeus = oppija.opiskeluoikeudet.collectFirst {
        case x: KelaLukionOpiskeluoikeus => x
      }.head

      lukioOpiskeluoikeus.tila.opiskeluoikeusjaksot.length should be >(0)
      lukioOpiskeluoikeus.tila.opiskeluoikeusjaksot.foreach(jakso => {
        jakso.opintojenRahoitus should not be (None)
      })

      val päätasonSuoritus = lukioOpiskeluoikeus.suoritukset.head

      päätasonSuoritus.puhviKoe should not be (None)
      päätasonSuoritus.puhviKoe.get.arvosana should be (None)
      päätasonSuoritus.puhviKoe.get.päivä should not be (None)
      päätasonSuoritus.puhviKoe.get.hyväksytty.isDefined should be (true)
      päätasonSuoritus.omanÄidinkielenOpinnot should not be (None)
      päätasonSuoritus.omanÄidinkielenOpinnot.get.osasuoritukset should not be (None)
      päätasonSuoritus.omanÄidinkielenOpinnot.get.osasuoritukset.get.length should not equal(0)
      päätasonSuoritus.omanÄidinkielenOpinnot.get.osasuoritukset.get.foreach { osasuoritus =>
        osasuoritus.arviointi.foreach(_.foreach { arviointi =>
          arviointi.arvosana should be (None)
          arviointi.hyväksytty.isDefined should be (true)
        })}
      päätasonSuoritus.suullisenKielitaidonKokeet should not be (None)
      päätasonSuoritus.suullisenKielitaidonKokeet.get.length should not equal(0)
      päätasonSuoritus.suullisenKielitaidonKokeet.get.foreach { koe =>
        koe.arvosana should be (None)
        koe.hyväksytty.isDefined should be (true)
      }

    }
  }

  "Palauttaa rahoitustiedon international schoolin -tutkinnolle" in {
    postHetu(KoskiSpecificMockOppijat.internationalschool.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
      verifyResponseStatusOk()

      val oppija = JsonSerializer.parse[KelaOppija](body)
      oppija.opiskeluoikeudet.length should be(1)

      val internationalSchool = oppija.opiskeluoikeudet.collectFirst {
        case x: KelaInternationalSchoolOpiskeluoikeus => x
      }.get

      internationalSchool.tila.opiskeluoikeusjaksot.length should be > (0)
      internationalSchool.tila.opiskeluoikeusjaksot.exists(jakso => {
        jakso.opintojenRahoitus.isDefined
      }) should be (true)
    }
  }

  "Palauttaa rahoitustiedon IB -tutkinnolle" in {
    postHetu(KoskiSpecificMockOppijat.ibFinal.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
      verifyResponseStatusOk()

      val oppija = JsonSerializer.parse[KelaOppija](body)
      oppija.opiskeluoikeudet.length should be(1)

      val ibtutkinto = oppija.opiskeluoikeudet.collectFirst {
        case x: KelaIBOpiskeluoikeus => x
      }.get

      ibtutkinto.tila.opiskeluoikeusjaksot.length should be > (0)
      ibtutkinto.tila.opiskeluoikeusjaksot.exists(jakso => {
        jakso.opintojenRahoitus.isDefined
      }) should be(true)
    }
  }

  "Palauttaa perusopetuksen kentän omanÄidinkielenOpinnot" in {
    postHetu(KoskiSpecificMockOppijat.ysiluokkalainen.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
      verifyResponseStatusOk()

      val oppija = JsonSerializer.parse[KelaOppija](body)
      oppija.opiskeluoikeudet.length should be(1)

      val lukioOpiskeluoikeus = oppija.opiskeluoikeudet.collectFirst {
        case x: KelaPerusopetuksenOpiskeluoikeus => x
      }.head

      val pts = lukioOpiskeluoikeus.suoritukset.find(p => p.tyyppi.koodiarvo == "perusopetuksenoppimaara").get

      pts.omanÄidinkielenOpinnot should not be (None)
    }
  }

  "Palauttaa vst-jotpan opiskeluoikeuden" in {
    postHetu(KoskiSpecificMockOppijat.vstJotpaKeskenOppija.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
      verifyResponseStatusOk()

      val oppija = JsonSerializer.parse[KelaOppija](body)
      oppija.opiskeluoikeudet.length should be(1)

      val jotpaOpiskeluoikeus = oppija.opiskeluoikeudet.last match {
        case x: KelaVapaanSivistystyönOpiskeluoikeus => x
      }

      jotpaOpiskeluoikeus.suoritukset.length shouldBe 1
      jotpaOpiskeluoikeus.suoritukset.head.tyyppi.koodiarvo should be("vstjotpakoulutus")
    }
  }

  "Palauttaa EB-tutkinnon" in {
    postHetu(KoskiSpecificMockOppijat.europeanSchoolOfHelsinki.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
      verifyResponseStatusOk()

      val oppija = JsonSerializer.parse[KelaOppija](body)
      oppija.opiskeluoikeudet.length should be(2) // ESH & EB

      val ebTutkinnot = oppija.opiskeluoikeudet collect {
        case x: KelaEBOpiskeluoikeus => x
      }

      ebTutkinnot.length shouldBe 1
      val ebTutkinto = ebTutkinnot.head
      ebTutkinto.suoritukset.length shouldBe 1
      val suoritus = ebTutkinto.suoritukset.head
      suoritus.tyyppi.koodiarvo shouldBe "ebtutkinto"
      suoritus.osasuoritukset.get.length shouldBe 3
      suoritus.osasuoritukset.foreach(_.foreach(_.osasuoritukset.foreach(_.foreach(os => {
        os.arviointi.foreach(_.foreach(a => {
          a.arvosana should be (None)
          a.hyväksytty.isDefined should be (true)
        }))
      }))))
    }
  }

  private def getHetu[A](hetu: String, user: MockUser = MockUsers.kelaSuppeatOikeudet)(f: => A)= {
    authGet(s"kela/$hetu", user)(f)
  }

  private def postHetu[A](hetu: String, user: MockUser = MockUsers.kelaLaajatOikeudet)(f: => A): A = {
    post(
      "api/luovutuspalvelu/kela/hetu",
      JsonSerializer.writeWithRoot(KelaRequest(hetu)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def postHetut[A](hetut: List[String], user: MockUser = MockUsers.kelaLaajatOikeudet)(f: => A): A = {
    post(
      "api/luovutuspalvelu/kela/hetut",
      JsonSerializer.writeWithRoot(KelaBulkRequest(hetut)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def getVersiohistoriaUI[A](opiskeluoikeudenOid: String, user: MockUser = MockUsers.kelaLaajatOikeudet)(f: => A): A = {
    authGet(s"api/luovutuspalvelu/kela/versiohistoria-ui/$opiskeluoikeudenOid", user)(f)
  }

  private def getVersiohistoria[A](opiskeluoikeudenOid: String, user: MockUser = MockUsers.kelaLaajatOikeudet)(f: => A): A = {
    authGet(s"api/luovutuspalvelu/kela/versiohistoria/$opiskeluoikeudenOid", user)(f)
  }

  private def getOpiskeluoikeudenVersioUI[A](
    oppijaOid: String,
    opiskeluoikeudenOid: String,
    versio: Int,
    user: MockUser = MockUsers.kelaLaajatOikeudet
  )(f: => A): A = {
    authGet(s"api/luovutuspalvelu/kela/versiohistoria-ui/$oppijaOid/$opiskeluoikeudenOid/$versio", user)(f)
  }

  private def getOpiskeluoikeudenVersio[A](
    opiskeluoikeudenOid: String,
    versio: Int,
    user: MockUser = MockUsers.kelaLaajatOikeudet
  )(f: => A): A = {
    authGet(s"api/luovutuspalvelu/kela/versiohistoria/$opiskeluoikeudenOid/$versio", user)(f)
  }

  private def luoVersiohistoriaanRivi(oppija: schema.Henkilö, opiskeluoikeus: schema.AmmatillinenOpiskeluoikeus): Unit = {
    createOrUpdate(oppija, opiskeluoikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now)))
  }
}
