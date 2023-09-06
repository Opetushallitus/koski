package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.raportit.{DataSheet, Sheet}
import fi.oph.koski.valpas.log.ValpasAuditLog
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.rouhinta.{ValpasRouhintaOppivelvollinenSheetRow, ValpasRouhintaPelkkäHetuSheetRow, ValpasRouhintaService}
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

import java.time.LocalDate

class ValpasHeturouhintaSpec extends ValpasRouhintaTestBase {

  override protected def beforeAll(): Unit = {
    FixtureUtil.resetMockData(KoskiApplicationForTests)
  }

  val eiOppivelvollisuuttaSuorittavatOppijat: List[RouhintaExpectedData] = List(
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.aikuistenPerusopetuksestaEronnut,
      ooPäättymispäivä = "30.8.2021",
      ooViimeisinTila = Some("Eronnut"),
      ooKoulutusmuoto = Some("Aikuisten perusopetus"),
      ooToimipiste = Some("Ressun lukio"),
      keskeytys = None,
      kuntailmoitusKohde = None,
      kuntailmoitusPvm = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.eiOppivelvollisuudenSuorittamiseenYksinäänKelpaaviaOpiskeluoikeuksia,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
      kuntailmoitusKohde = None,
      kuntailmoitusPvm = None,
    ),
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.eiKoskessaOppivelvollinen,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
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
  )

  val oppivelvollisuuttaSuorittavienHetut = List(
    ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.hetu.get,
    ValpasMockOppijat.oppivelvollisuusKeskeytettyToistaiseksi.hetu.get,
  )

  val oppivelvollisuudenUlkopuolistenHetut = List(
    ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.hetu.get,
    ValpasMockOppijat.eiOppivelvollinenLiianNuori.hetu.get,
  )

  val amiksestaValmistuneidenHetut = List(
    ValpasMockOppijat.amisValmistunutEronnutValmasta.hetu.get
  )

  val oppijanumerorekisterinUlkopuolisetHetut = List(
    "161004A404E",
  )

  val virheellisetHetut = List(
    "161004A404F",
  )

  "Rouhinta" - {
    "Hetulistalla" - {
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
      "Oppivelvollisuutta suorittavat sisältää oikeat hetut" in {
        oppivelvollisuuttaSuorittavat.map(_.hetu) should contain theSameElementsAs oppivelvollisuuttaSuorittavienHetut
      }
      "Oppivelvollisuuden ulkopuoliset sisältää oikeat hetut" in {
        oppivelvollisuudenUlkopuoliset.map(_.hetu) should contain theSameElementsAs oppivelvollisuudenUlkopuolistenHetut
      }
      "Oppijanumerorekisterin ulkopuoliset sisältää oikeat hetut" in {
        oppijanumerorekisterinUlkopuoliset.map(_.hetu) should contain theSameElementsAs oppijanumerorekisterinUlkopuolisetHetut
      }
      "Virheelliset hetut sisältää oikeat hetut" in {
        virheelliset.map(_.hetu) should contain theSameElementsAs virheellisetHetut
      }
    }

    "Heturouhinta ei palauta amiksesta valmistunutta" in {
      FixtureUtil.resetMockData(KoskiApplicationForTests, LocalDate.of(2023, 5, 2))

      val oppijat = loadHetuhaku(amiksestaValmistuneidenHetut).collectFirst {
        case d: DataSheet if d.title == t.get("rouhinta_tab_ei_oppivelvollisuutta_suorittavat") => d.rows.collect {
          case r: ValpasRouhintaOppivelvollinenSheetRow => r
        }
      }.get

      oppijat.flatMap(_.hetu) should not contain (ValpasMockOppijat.amisValmistunutEronnutValmasta.hetu.get)
    }

    "Audit-log toimii myös isolla oppijamäärällä" in {
      val hetut = Range.inclusive(0, 9999).map(n => s"123456-${"%04d".format(n)}")
      ValpasAuditLog.auditLogRouhintahakuHetulistalla(hetut, hetut)(session(defaultUser))
    }
  }

  lazy val hakutulosSheets: Seq[Sheet] = loadHetuhaku()

  lazy val oppivelvollisuuttaSuorittavat = pelkkäHetuRows("rouhinta_tab_oppivelvollisuutta_suorittavat")

  lazy val oppivelvollisuudenUlkopuoliset = pelkkäHetuRows("rouhinta_tab_ovl_ulkopuoliset")

  lazy val oppijanumerorekisterinUlkopuoliset = pelkkäHetuRows("rouhinta_tab_onr_ulkopuoliset")

  lazy val virheelliset = pelkkäHetuRows("rouhinta_tab_virheelliset_hetut")

  private def loadHetuhaku(muutHetut: List[String] = Nil) = {
    new ValpasRouhintaService(KoskiApplicationForTests)
      .haeHetulistanPerusteellaExcel(
        hetut = eiOppivelvollisuuttaSuorittavienHetut ++
          oppivelvollisuuttaSuorittavienHetut ++
          oppivelvollisuudenUlkopuolistenHetut ++
          oppijanumerorekisterinUlkopuolisetHetut ++
          virheellisetHetut ++
          muutHetut,
        language = "fi",
        password = Some("hunter2")
      )(session(ValpasMockUsers.valpasHelsinki))
      .fold(
        error => fail(s"Haku hetulistalla epäonnistui: $error"),
        result => result.response.sheets
      )
  }

  private def pelkkäHetuRows(titleKey: String): Seq[ValpasRouhintaPelkkäHetuSheetRow] = hakutulosSheets.collectFirst {
    case d: DataSheet if d.title == t.get(titleKey) => d.rows.collect {
      case r: ValpasRouhintaPelkkäHetuSheetRow => r
    }
  }.get

}
