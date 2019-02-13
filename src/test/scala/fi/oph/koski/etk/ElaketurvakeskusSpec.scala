package fi.oph.koski.etk

import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.documentation.AmmatillinenExampleData
import org.scalatest.{FreeSpec, Matchers}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods

class ElaketurvakeskusSpec extends FreeSpec with LocalJettyHttpSpecification with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethodsAmmatillinen with ElaketurvakeskusCLITestMethods {

  "Tutkintotietojen muodostaminen" - {
    "Valmiit ammatilliset perustutkinnot" in {
      resetFixtures
      loadRaportointikantaFixtures

      val mockOppija = MockOppijat.ammattilainen
      val mockOppijanOpiskeluoikeusOid = raportointikantaQueryHelper.opiskeluoikeusByOppijaOidAndKoulutusmuoto(mockOppija.oid, "ammatillinenkoulutus").head.opiskeluoikeusOid

      postAikajakso(date(2016, 1, 1), date(2016, 12, 12), 2016) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[EtkResponse](body)
        response.vuosi should equal(2016)
        response.tutkintojenLkm should equal(1)
        response.aikaleima shouldBe a[Timestamp]
        response.tutkinnot should equal(List(
          EtkTutkintotieto(
            EtkHenkilö(mockOppija.hetu, date(1918, 6, 28), mockOppija.sukunimi, mockOppija.etunimet),
            EtkTutkinto("ammatillinenkoulutus", date(2012, 9, 1), Some(date(2016, 5, 31))),
            Some(EtkViite(mockOppijanOpiskeluoikeusOid, 1, mockOppija.oid)))
        ))
      }
    }
    "Oppija jolla monta oidia" in {
      val slaveMock = MockOppijat.slave.henkilö
      val masterMock = MockOppijat.master

      createOrUpdate(slaveMock, AmmatillinenExampleData.perustutkintoOpiskeluoikeusValmis())
      loadRaportointikantaFixtures

      val slaveOppijanOpiskeluoikeusOid = raportointikantaQueryHelper.opiskeluoikeusByOppijaOidAndKoulutusmuoto(slaveMock.oid, "ammatillinenkoulutus").head.opiskeluoikeusOid

      postAikajakso(date(2016, 1, 1), date(2016, 12, 31), 2016) {
        verifyResponseStatusOk()
        JsonSerializer.parse[EtkResponse](body).tutkinnot should contain(
          EtkTutkintotieto(
            EtkHenkilö(masterMock.hetu, date(1997, 10, 10), masterMock.sukunimi, masterMock.etunimet),
            EtkTutkinto("ammatillinenkoulutus", date(2012, 9, 1), Some(date(2016, 5, 31))),
            Some(EtkViite(slaveOppijanOpiskeluoikeusOid, 1, slaveMock.oid)))
        )
      }
    }
  }

  "ElaketurvakeskusCLI - komentorivityokalu" - {
    "Tulostaa jsonin" in {
      resetFixtures
      loadRaportointikantaFixtures
      withCsvFixture {
        val cli = new ElaketurvakeskusCLI with MockOutput
        val input = Array("-csv", csvFilePath, "-user", "pää:pää", "-api", "ammatillisetperustutkinnot:2016-01-01:2016-12-12", "-port", koskiPort)
        val ammattilaisenOpiskeluoikeusOid = raportointikantaQueryHelper.opiskeluoikeusByOppijaOidAndKoulutusmuoto(MockOppijat.ammattilainen.oid, "ammatillinenkoulutus").head.opiskeluoikeusOid

        cli.main(input)
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
    "Virheellinen csv tiedosto" in {
      withFailingCsvFixture {
        val cli = new ElaketurvakeskusCLI
        val input = Array("-csv", failingCsvFilePath)
        an[Exception] should be thrownBy (cli.main(input))
      }
    }
  }

  private def postAikajakso[A](alku: LocalDate, loppu: LocalDate, vuosi: Int)(f: => A): A = {
    post(
      "api/elaketurvakeskus/ammatillisetperustutkinnot",
      JsonSerializer.writeWithRoot(EtkTutkintotietoRequest(alku, loppu, vuosi)),
      headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent
    )(f)
  }
}
