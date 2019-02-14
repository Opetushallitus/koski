package fi.oph.koski.etk

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import org.scalatest.FreeSpec

class ElaketurvakeskusCliSpec extends FreeSpec with RaportointikantaTestMethods with ElaketurvakeskusCLITestMethods {

  "ElaketurvakeskusCli" - {
    "Aineiston muodostaminen" - {
      "Csv tiedostosta" in {
        resetFixtures
        loadRaportointikantaFixtures
        withCsvFixture() {
          val cli = new ElaketurvakeskusCLI with MockOutput
          val args = Array("-csv", csvFilePath)
          cli.main(args)
          cli.consoleOutput should include(
            """{
              | "vuosi": 2016,
              | "tutkintojenLkm": 8,
              | "aikaleima":""".stripMargin)
          cli.consoleOutput should include(
            s"""| "tutkinnot": [
                |		{"henkilö":{"hetu":"021094-650K","syntymäaika":"1989-02-01","sukunimi":"Nenäkä","etunimet":"Dtes Apu"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2011-08-01","päättymispäivä":"2016-06-19"}},
                |		{"henkilö":{"hetu":"281192-654S","syntymäaika":"1983-04-01","sukunimi":"Test","etunimet":"Testi Hy"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2015-08-01","päättymispäivä":"2016-05-31"}},
                |		{"henkilö":{"hetu":"061188-685J","syntymäaika":"1991-09-01","sukunimi":"Eespä","etunimet":"Eespä Jesta"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-01-31"}},
                |		{"henkilö":{"hetu":"291093-711P","syntymäaika":"1970-10-01","sukunimi":"Kaik","etunimet":"Veikee Kaik Aputap"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-05-18"}},
                |		{"henkilö":{"hetu":"221195-677D","syntymäaika":"1981-01-02","sukunimi":"Leikkita","etunimet":"Jest Kaikke"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2013-08-01","päättymispäivä":"2016-03-27"}},
                |		{"henkilö":{"hetu":"311293-717T","syntymäaika":"1991-01-02","sukunimi":"Sutjaka","etunimet":"Mietis Betat"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2013-08-01","päättymispäivä":"2016-03-17"}},
                |		{"henkilö":{"hetu":"260977-606E","syntymäaika":"1993-01-02","sukunimi":"Sutjakast","etunimet":"Ftes Testitap"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-05-31"}},
                |		{"henkilö":{"hetu":"","syntymäaika":"1988-02-02","sukunimi":"Kai","etunimet":"Betat Testitap"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2015-08-01","päättymispäivä":"2017-06-06"}}
                | ]""".stripMargin)
        }
      }
      "Api vastauksesta" in {
        resetFixtures
        loadRaportointikantaFixtures
        withCsvFixture() {
          val cli = new ElaketurvakeskusCLI with MockOutput
          val args = Array("-user", "pää:pää", "-api", "ammatillisetperustutkinnot:2016-01-01:2016-12-12", "-port", koskiPort)
          val ammattilaisenOpiskeluoikeusOid = raportointikantaQueryHelper.opiskeluoikeusByOppijaOidAndKoulutusmuoto(MockOppijat.ammattilainen.oid, "ammatillinenkoulutus").head.opiskeluoikeusOid

          cli.main(args)
          cli.consoleOutput should include(
            s"""{
               | "vuosi": 2016,
               | "tutkintojenLkm": 1,
               | "aikaleima":""".stripMargin)
          cli.consoleOutput should include(
            s"""| "tutkinnot": [
                |		{"henkilö":{"hetu":"280618-402H","syntymäaika":"1918-06-28","sukunimi":"Ammattilainen","etunimet":"Aarne"},"tutkinto":{"tutkinnonTaso":"ammatillinenperuskoulutus","alkamispäivä":"2012-09-01","päättymispäivä":"2016-05-31"},"viite":{"opiskeluoikeusOid":"${ammattilaisenOpiskeluoikeusOid}","opiskeluoikeusVersionumero":1,"oppijaOid":"1.2.246.562.24.00000000015"}}
                | ]
                |}""".stripMargin)
        }
      }
      "Yhdistaa csv:n tiedoston ja api-vastauksen" in {
        resetFixtures
        loadRaportointikantaFixtures
        withCsvFixture() {
          val cli = new ElaketurvakeskusCLI with MockOutput
          val args = Array("-csv", csvFilePath, "-user", "pää:pää", "-api", "ammatillisetperustutkinnot:2016-01-01:2016-12-12", "-port", koskiPort)
          val ammattilaisenOpiskeluoikeusOid = raportointikantaQueryHelper.opiskeluoikeusByOppijaOidAndKoulutusmuoto(MockOppijat.ammattilainen.oid, "ammatillinenkoulutus").head.opiskeluoikeusOid

          cli.main(args)
          cli.consoleOutput should include(
            s"""{
               | "vuosi": 2016,
               | "tutkintojenLkm": 9,
               | "aikaleima":""".stripMargin)
          cli.consoleOutput should include(
            s"""| "tutkinnot": [
                |		{"henkilö":{"hetu":"021094-650K","syntymäaika":"1989-02-01","sukunimi":"Nenäkä","etunimet":"Dtes Apu"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2011-08-01","päättymispäivä":"2016-06-19"}},
                |		{"henkilö":{"hetu":"281192-654S","syntymäaika":"1983-04-01","sukunimi":"Test","etunimet":"Testi Hy"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2015-08-01","päättymispäivä":"2016-05-31"}},
                |		{"henkilö":{"hetu":"061188-685J","syntymäaika":"1991-09-01","sukunimi":"Eespä","etunimet":"Eespä Jesta"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-01-31"}},
                |		{"henkilö":{"hetu":"291093-711P","syntymäaika":"1970-10-01","sukunimi":"Kaik","etunimet":"Veikee Kaik Aputap"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-05-18"}},
                |		{"henkilö":{"hetu":"221195-677D","syntymäaika":"1981-01-02","sukunimi":"Leikkita","etunimet":"Jest Kaikke"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2013-08-01","päättymispäivä":"2016-03-27"}},
                |		{"henkilö":{"hetu":"311293-717T","syntymäaika":"1991-01-02","sukunimi":"Sutjaka","etunimet":"Mietis Betat"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2013-08-01","päättymispäivä":"2016-03-17"}},
                |		{"henkilö":{"hetu":"260977-606E","syntymäaika":"1993-01-02","sukunimi":"Sutjakast","etunimet":"Ftes Testitap"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2014-08-01","päättymispäivä":"2016-05-31"}},
                |		{"henkilö":{"hetu":"","syntymäaika":"1988-02-02","sukunimi":"Kai","etunimet":"Betat Testitap"},"tutkinto":{"tutkinnonTaso":"alempikorkeakoulututkinto","alkamispäivä":"2015-08-01","päättymispäivä":"2017-06-06"}},
                |		{"henkilö":{"hetu":"280618-402H","syntymäaika":"1918-06-28","sukunimi":"Ammattilainen","etunimet":"Aarne"},"tutkinto":{"tutkinnonTaso":"ammatillinenperuskoulutus","alkamispäivä":"2012-09-01","päättymispäivä":"2016-05-31"},"viite":{"opiskeluoikeusOid":"${ammattilaisenOpiskeluoikeusOid}","opiskeluoikeusVersionumero":1,"oppijaOid":"1.2.246.562.24.00000000015"}}
                | ]
                |}""".stripMargin
          )
        }
      }
    }
    "Validointi" - {
      "Puutteellinen csv tiedosto" in {
        val cli = new ElaketurvakeskusCLI
        val args = Array("-csv", csvFilePath)
        withCsvFixture(mockCsv.replace(";;", ";")) {
          an[Exception] should be thrownBy (cli.main(args))
        }
      }
      "Csv:n ja api vastauksen vuodet eroavat" in {
        withCsvFixture(mockCsv.replaceAll("2016;", "2000;")) {
          val cli = new ElaketurvakeskusCLI
          val args = Array("-csv", csvFilePath, "-user", "pää:pää", "-api", "ammatillisetperustutkinnot:2016-01-01:2018-12-12", "-port", koskiPort)
          an[Exception] should be thrownBy (cli.main(args))
        }
      }
    }
    "Vivut" - {
      "Vaatii -user jos -api määritelty" in {
        val cli = new ElaketurvakeskusCLI
        val args = Array("-api", "ammatillisetperustutkinnot:2016-01-01:2018-12-12")
        an[Exception] should be thrownBy (cli.main(args))
      }
    }
  }
}
