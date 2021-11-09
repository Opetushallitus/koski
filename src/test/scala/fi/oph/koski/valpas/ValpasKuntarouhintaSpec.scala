package fi.oph.koski.valpas

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.rouhinta.ValpasRouhintaService
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

class ValpasKuntarouhintaSpec extends ValpasRouhintaTestBase {

  override protected def beforeAll(): Unit = {
    FixtureUtil.resetMockData(KoskiApplicationForTests, date(2021, 5, 20))
  }

  val kunta = "624" // Pyhtää

  val eiOppivelvollisuuttaSuorittavatOppijat = List(
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.aikuistenPerusopetuksessa,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.aikuistenPerusopetuksessaAineopiskelija,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksia,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.eronnutOppija,
      ooPäättymispäivä = "1.1.2021",
      ooViimeisinTila = Some("Eronnut"),
      ooKoulutusmuoto = Some("Perusopetus"),
      ooToimipiste = Some("Jyväskylän normaalikoulu"),
      keskeytys = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.intSchool9LuokaltaValmistumisenJälkeenEronnutOppija,
      ooPäättymispäivä = "1.1.2021",
      ooViimeisinTila = Some("Eronnut"),
      ooKoulutusmuoto = Some("International school"),
      ooToimipiste = Some("International School of Helsinki"),
      keskeytys = None,
    ),
  )

  "Rouhinta" - {
    "Kunnalla" - {
      "Oppivelvolliset" - {
        "Sisältää oikeat hetut" in {
          expectEiOppivelvollisuuttaSuorittavatPropsMatch(
            actual => actual.hetu,
            expected => expected.oppija.hetu,
          )
        }
        "Päättymispäivä" in {
          expectEiOppivelvollisuuttaSuorittavatPropsMatch(
            actual => actual.ooPäättymispäivä,
            expected => expected.ooPäättymispäivä,
          )
        }
        "Viimeisin tila" in {
          expectEiOppivelvollisuuttaSuorittavatPropsMatch(
            actual => actual.ooViimeisinTila,
            expected => expected.ooViimeisinTila,
          )
        }
        "Koulutusmuoto" in {
          expectEiOppivelvollisuuttaSuorittavatPropsMatch(
            actual => actual.ooKoulutusmuoto,
            expected => expected.ooKoulutusmuoto,
          )
        }
        "Toimipiste/oppilaitos" in {
          expectEiOppivelvollisuuttaSuorittavatPropsMatch(
            actual => actual.ooToimipiste,
            expected => expected.ooToimipiste,
          )
        }
        "Oppivelvollisuuden keskeytys" in {
          expectEiOppivelvollisuuttaSuorittavatPropsMatch(
            actual => actual.keskeytys,
            expected => expected.keskeytys.getOrElse(""),
          )
        }
      }
    }
  }

  lazy val hakutulosSheets = loadKuntahaku

  private def loadKuntahaku() = {
    new ValpasRouhintaService(KoskiApplicationForTests)
      .haeKunnanPerusteellaExcel(
        kunta = kunta,
        language = "fi",
        password = Some("hunter2")
      )(session(ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu))
      .fold(
        error => fail(s"Haku Kunnalla epäonnistui: $error"),
        result => result.sheets
      )
  }
}
