package fi.oph.koski.valpas

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.koskiuser.AuthenticationUser
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.helsinginKaupunki
import fi.oph.koski.valpas.log.ValpasAuditLog
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.rouhinta.ValpasRouhintaService
import fi.oph.koski.valpas.valpasuser.{ValpasMockUsers, ValpasSession}

object ValpasKuntarouhintaSpec {
  val tarkastelupäivä = date(2021, 5, 20)

  val kuntakoodi = "624" // Pyhtää
  val kuntaOid = MockOrganisaatiot.pyhtäänKunta

  def eiOppivelvollisuuttaSuorittavatOppijat(t: LocalizationReader) = List(
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.aikuistenPerusopetuksessa,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
      kuntailmoitusKohde = None,
      kuntailmoitusPvm = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.aikuistenPerusopetuksessaAineopiskelija,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
      kuntailmoitusKohde = None,
      kuntailmoitusPvm = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksia,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
      kuntailmoitusKohde = None,
      kuntailmoitusPvm = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.eronnutOppija,
      ooPäättymispäivä = "1.1.2021",
      ooViimeisinTila = Some("Eronnut"),
      ooKoulutusmuoto = Some("Perusopetus"),
      ooToimipiste = Some("Jyväskylän normaalikoulu"),
      keskeytys = None,
      kuntailmoitusKohde = None,
      kuntailmoitusPvm = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.intSchool9LuokaltaValmistumisenJälkeenEronnutOppija,
      ooPäättymispäivä = "1.1.2021",
      ooViimeisinTila = Some("Eronnut"),
      ooKoulutusmuoto = Some("International school"),
      ooToimipiste = Some("International School of Helsinki"),
      keskeytys = None,
      kuntailmoitusKohde = None,
      kuntailmoitusPvm = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.oppivelvollisuusKeskeytettyEiOpiskele,
      ooPäättymispäivä = "15.5.2021",
      ooViimeisinTila = Some("Valmistunut"),
      ooKoulutusmuoto = Some("Perusopetus"),
      ooToimipiste = Some("Jyväskylän normaalikoulu"),
      keskeytys = Some("16.8.2021 -"),
      kuntailmoitusKohde = Some("Pyhtää"),
      kuntailmoitusPvm = Some("20.5.2021"),
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.opiskeluoikeudetonOppivelvollisuusikäinenOppija,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
      kuntailmoitusKohde = None,
      kuntailmoitusPvm = None,
    ),
  )
}

class ValpasKuntarouhintaSpec extends ValpasRouhintaTestBase {

  override protected def beforeAll(): Unit = {
    FixtureUtil.resetMockData(KoskiApplicationForTests, ValpasKuntarouhintaSpec.tarkastelupäivä)
  }

  val eiOppivelvollisuuttaSuorittavatOppijat = ValpasKuntarouhintaSpec.eiOppivelvollisuuttaSuorittavatOppijat(t)

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
        "Kuntailmoitus kohde" in {
          expectEiOppivelvollisuuttaSuorittavatPropsMatch(
            actual => actual.kuntailmoitusKohde,
            expected => expected.kuntailmoitusKohde
          )
        }
        "Kuntailmoitus pvm" in {
          expectEiOppivelvollisuuttaSuorittavatPropsMatch(
            actual => actual.kuntailmoitusPvm,
            expected => expected.kuntailmoitusPvm
          )
        }
      }
    }

    "Audit-log toimii myös isolla oppijamäärällä" in {
      val oids = Range.inclusive(1, 10000).map(n => s"1.2.246.562.10.000000${"%05d".format(n)}")
      ValpasAuditLog.auditLogRouhintahakuKunnalla(helsinginKaupunki, oids)(session(defaultUser))
    }
  }

  lazy val hakutulosSheets = loadKuntahaku

  private def loadKuntahaku() = {
    new ValpasRouhintaService(KoskiApplicationForTests)
      .haeKunnanPerusteellaExcel(
        kunta = ValpasKuntarouhintaSpec.kuntakoodi,
        language = "fi",
        password = Some("hunter2")
      )(session(ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu))
      .fold(
        error => fail(s"Haku Kunnalla epäonnistui: $error"),
        result => result.response.sheets
      )
  }
}
