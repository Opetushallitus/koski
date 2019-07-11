package fi.oph.koski.etk

import java.text.SimpleDateFormat

import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.log.AuditLogTester
import org.json4s.jackson.JsonMethods.parse
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.json4s.DefaultFormats
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

class ElaketurvakeskusCliSpec extends FreeSpec with RaportointikantaTestMethods with ElaketurvakeskusCliTestMethods with OpiskeluoikeusTestMethods with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    resetFixtures
    loadRaportointikantaFixtures
  }

  "ElaketurvakeskusCli" - {
    "Aineiston muodostaminen" - {
      "Csv tiedostosta" in {
        withCsvFixture() {
          val cli = ElaketurvakeskusCliForTest
          val args = Array("-csv", csvFilePath, "-user", "pää:pää", "-port", koskiPort)
          cli.main(args)
          outputResult should include(
            """{
              | "vuosi": 2016,
              | "tutkintojenLkm": 10,
              | "aikaleima":""".stripMargin)
          outputResult should include(
            s"""| "tutkinnot": [
                |		{"henkilö":{"hetu":"021094-650K","syntymäaika":"1989-02-01","sukunimi":"Nenäkä","etunimet":"Dtes Apu"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2011-08-01","päättymispäivä":"2016-06-19"}},
                |		{"henkilö":{"hetu":"281192-654S","syntymäaika":"1983-04-01","sukunimi":"Test","etunimet":"Testi Hy"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2015-08-01","päättymispäivä":"2016-05-31"},"viite":{"oppijaOid":"1.2.246.562.24.96616592932"}},
                |		{"henkilö":{"hetu":"061188-685J","syntymäaika":"1991-09-01","sukunimi":"Eespä","etunimet":"Eespä Jesta"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-01-31"}},
                |		{"henkilö":{"hetu":"291093-711P","syntymäaika":"1970-10-01","sukunimi":"Kaik","etunimet":"Veikee Kaik Aputap"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-05-18"}},
                |		{"henkilö":{"hetu":"221195-677D","syntymäaika":"1981-01-02","sukunimi":"Leikkita","etunimet":"Jest Kaikke"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2013-08-01","päättymispäivä":"2016-03-27"}},
                |		{"henkilö":{"hetu":"311293-717T","syntymäaika":"1991-01-02","sukunimi":"Sutjaka","etunimet":"Mietis Betat"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2013-08-01","päättymispäivä":"2016-03-17"}},
                |		{"henkilö":{"hetu":"260977-606E","syntymäaika":"1993-01-02","sukunimi":"Sutjakast","etunimet":"Ftes Testitap"},"tutkinto":{"tutkinnonTaso":"ylempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-05-31"}},
                |		{"henkilö":{"syntymäaika":"1988-02-02","sukunimi":"Kai","etunimet":"Betat Testitap"},"tutkinto":{"tutkinnonTaso":"ylempiammattikorkeakoulututkinto","alkamispäivä":"2015-08-01","päättymispäivä":"2017-06-06"},"viite":{"oppijaOid":"1.2.246.562.24.86863218011"}},
                |		{"henkilö":{"syntymäaika":"1988-02-02","sukunimi":"Pai","etunimet":"Ketat Testitap"},"tutkinto":{"tutkinnonTaso":"ammattikorkeakoulututkinto","alkamispäivä":"2015-08-01","päättymispäivä":"2017-06-06"},"viite":{"oppijaOid":"1.2.246.562.24.86863218012"}},
                |		{"henkilö":{"sukunimi":"Alho","etunimet":"Aapeli"},"tutkinto":{"päättymispäivä":"2018-08-31"}}
                | ]""".stripMargin)
        }
      }
      "Api vastauksesta" in {
        withCsvFixture() {
          val cli = ElaketurvakeskusCliForTest
          val args = Array("-user", "pää:pää", "-api", "ammatillisetperustutkinnot:2016-01-01:2016-12-12", "-port", koskiPort)
          val ammattilaisenOpiskeluoikeusOid = getOpiskeluoikeudet(MockOppijat.ammattilainen.oid).find(_.tyyppi.koodiarvo == "ammatillinenkoulutus").get.oid.get

          cli.main(args)
          outputResult should include(
            s"""{
               | "vuosi": 2016,
               | "tutkintojenLkm": 1,
               | "aikaleima":""".stripMargin)
          outputResult should include(
            s"""| "tutkinnot": [
                |		{"henkilö":{"hetu":"280618-402H","syntymäaika":"1918-06-28","sukunimi":"Ammattilainen","etunimet":"Aarne"},"tutkinto":{"tutkinnonTaso":"ammatillinenperustutkinto","alkamispäivä":"2012-09-01","päättymispäivä":"2016-05-31"},"viite":{"opiskeluoikeusOid":"${ammattilaisenOpiskeluoikeusOid}","opiskeluoikeusVersionumero":1,"oppijaOid":"${MockOppijat.ammattilainen.oid}"}}
                | ]
                |}""".stripMargin)
        }
      }
      "Yhdistaa csv:n tiedoston ja api-vastauksen" in {
        resetFixtures
        loadRaportointikantaFixtures
        withCsvFixture() {
          val cli = ElaketurvakeskusCliForTest
          val args = Array("-csv", csvFilePath, "-user", "pää:pää", "-api", "ammatillisetperustutkinnot:2016-01-01:2016-12-12", "-port", koskiPort)
          val ammattilaisenOpiskeluoikeusOid = getOpiskeluoikeudet(MockOppijat.ammattilainen.oid).find(_.tyyppi.koodiarvo == "ammatillinenkoulutus").get.oid.get

          cli.main(args)
          outputResult should include(
            s"""{
               | "vuosi": 2016,
               | "tutkintojenLkm": 11,
               | "aikaleima":""".stripMargin)
          outputResult should include(
            s"""| "tutkinnot": [
                |		{"henkilö":{"hetu":"021094-650K","syntymäaika":"1989-02-01","sukunimi":"Nenäkä","etunimet":"Dtes Apu"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2011-08-01","päättymispäivä":"2016-06-19"}},
                |		{"henkilö":{"hetu":"281192-654S","syntymäaika":"1983-04-01","sukunimi":"Test","etunimet":"Testi Hy"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2015-08-01","päättymispäivä":"2016-05-31"},"viite":{"oppijaOid":"1.2.246.562.24.96616592932"}},
                |		{"henkilö":{"hetu":"061188-685J","syntymäaika":"1991-09-01","sukunimi":"Eespä","etunimet":"Eespä Jesta"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-01-31"}},
                |		{"henkilö":{"hetu":"291093-711P","syntymäaika":"1970-10-01","sukunimi":"Kaik","etunimet":"Veikee Kaik Aputap"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-05-18"}},
                |		{"henkilö":{"hetu":"221195-677D","syntymäaika":"1981-01-02","sukunimi":"Leikkita","etunimet":"Jest Kaikke"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2013-08-01","päättymispäivä":"2016-03-27"}},
                |		{"henkilö":{"hetu":"311293-717T","syntymäaika":"1991-01-02","sukunimi":"Sutjaka","etunimet":"Mietis Betat"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2013-08-01","päättymispäivä":"2016-03-17"}},
                |		{"henkilö":{"hetu":"260977-606E","syntymäaika":"1993-01-02","sukunimi":"Sutjakast","etunimet":"Ftes Testitap"},"tutkinto":{"tutkinnonTaso":"ylempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-05-31"}},
                |		{"henkilö":{"syntymäaika":"1988-02-02","sukunimi":"Kai","etunimet":"Betat Testitap"},"tutkinto":{"tutkinnonTaso":"ylempiammattikorkeakoulututkinto","alkamispäivä":"2015-08-01","päättymispäivä":"2017-06-06"},"viite":{"oppijaOid":"1.2.246.562.24.86863218011"}},
                |		{"henkilö":{"syntymäaika":"1988-02-02","sukunimi":"Pai","etunimet":"Ketat Testitap"},"tutkinto":{"tutkinnonTaso":"ammattikorkeakoulututkinto","alkamispäivä":"2015-08-01","päättymispäivä":"2017-06-06"},"viite":{"oppijaOid":"1.2.246.562.24.86863218012"}},
                |		{"henkilö":{"sukunimi":"Alho","etunimet":"Aapeli"},"tutkinto":{"päättymispäivä":"2018-08-31"}},
                |		{"henkilö":{"hetu":"280618-402H","syntymäaika":"1918-06-28","sukunimi":"Ammattilainen","etunimet":"Aarne"},"tutkinto":{"tutkinnonTaso":"ammatillinenperustutkinto","alkamispäivä":"2012-09-01","päättymispäivä":"2016-05-31"},"viite":{"opiskeluoikeusOid":"${ammattilaisenOpiskeluoikeusOid}","opiskeluoikeusVersionumero":1,"oppijaOid":"${MockOppijat.ammattilainen.oid}"}}
                | ]
                |}""".stripMargin
          )
        }
      }
      "Tuottaa validin JSON:in joka voidaan parsia" in {
        implicit val formats = new DefaultFormats {
          override def dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        }
        withCsvFixture() {
          val cli = ElaketurvakeskusCliForTest
          val args = Array("-csv", csvFilePath, "-user", "pää:pää", "-api", "ammatillisetperustutkinnot:2016-01-01:2016-12-12", "-port", koskiPort)
          cli.main(args)
          val result = parse(outputResult).extract[EtkResponse]

          result.tutkintojenLkm should equal(11)
          result.tutkinnot.length should equal(result.tutkintojenLkm)
        }
      }
    }
    "Jos csv tiedostosta puuttuu vaadittuja kolumnin nimiä" in {
      val cli = ElaketurvakeskusCli
      val args = Array("-csv", csvFilePath, "-user", "pää:pää")
      withCsvFixture(mockCsv.replace("hetu", "foobar")) {
        intercept[Error] {
          cli.main(args)
        }.getMessage should include(
          "Csv tiedostosta puuttuu haluttuja otsikoita. List(hetu)"
        )
      }
    }
    "Csv:n ja api vastauksen vuodet eroavat" in {
      withCsvFixture(mockCsv.replaceAll("2016;", "2000;")) {
        val cli = ElaketurvakeskusCli
        val args = Array("-csv", csvFilePath, "-user", "pää:pää", "-api", "ammatillisetperustutkinnot:2016-01-01:2018-12-12", "-port", koskiPort)
        intercept[Error] {
          cli.main(args)
        }.getMessage should be(
          "Vuosien 2000 ja 2016 tutkintotietoja yritettiin yhdistää"
        )
      }
    }
  }
  "Jos argumenteilta puuttuu parametreja" in {
    val cli = ElaketurvakeskusCli
    val args = Array("-csv", csvFilePath, "-user")
    an[Error] should be thrownBy (cli.main(args))
    intercept[Error] { cli.main(args) }.getMessage should be(
      "Unknown parameters for argument List(-user)"
    )
  }
  "Vaatii aina -user vivun" in {
    val cli = ElaketurvakeskusCli
    val args = Array("-csv", csvFilePath)
    an[Error] should be thrownBy (cli.main(args))
    intercept[Error] {cli.main(args)}.getMessage should be(
      "määritä -user tunnus:salasana"
    )
  }
  "Luo AuditLogit" in {
    AuditLogTester.clearMessages
    withCsvFixture() {
      val cli = ElaketurvakeskusCliForTest
      val args = Array("-csv", csvFilePath, "-user", "pää:pää", "-api", "ammatillisetperustutkinnot:2016-01-01:2016-12-12", "-port", koskiPort)

      cli.main(args)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_HAKU", "target" -> Map("oppijaHenkiloOid" -> MockOppijat.ammattilainen.oid)))
    }
  }
}
