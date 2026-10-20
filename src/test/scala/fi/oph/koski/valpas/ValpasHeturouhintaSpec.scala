package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.raportit.{DataSheet, Sheet}
import fi.oph.koski.valpas.log.ValpasAuditLog
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.oppivelvollisuudestavapautus.UusiOppivelvollisuudestaVapautus
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
    RouhintaExpectedData(
      oppija = ValpasMockOppijat.vainMitätöityjäOpiskeluoikeuksia,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
      kuntailmoitusKohde = None,
      kuntailmoitusPvm = None,
    ),
  )

  val oppivelvollisuuttaSuorittavienHetut = List(
    ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021.hetu.get,
    ValpasMockOppijat.oppivelvollisuusKeskeytettyToistaiseksi.hetu.get,
  )

  val oppivelvollisuudenUlkopuolistenHetut = List(
    ValpasMockOppijat.eiOppivelvollinenSyntynytEnnen2004.hetu.get,
    ValpasMockOppijat.eiOppivelvollinenLiianNuori.hetu.get,
    ValpasMockOppijat.ennenLainRajapäivääPeruskoulustaValmistunut.hetu.get,
    ValpasMockOppijat.eiOppivelvollinenLukuvuosimaksuRahoitteinen.hetu.get,
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

    "Oppivelvollisuudesta vapautettu oppija, jolla on vain mitätöityjä opiskeluoikeuksia, on oppivelvollisuuden ulkopuolinen" in {
      FixtureUtil.resetMockData(KoskiApplicationForTests)
      val oppija = ValpasMockOppijat.vainMitätöityjäOpiskeluoikeuksia

      def haeEiSuorittavienJaUlkopuolistenHetut() = {
        val sheets = loadHetuhaku()
        val eiSuorittavat = sheets.collectFirst {
          case d: DataSheet if d.title == t.get("rouhinta_tab_ei_oppivelvollisuutta_suorittavat") => d.rows.collect {
            case r: ValpasRouhintaOppivelvollinenSheetRow => r
          }
        }.get
        (eiSuorittavat.flatMap(_.hetu), pelkkäHetuRows("rouhinta_tab_ovl_ulkopuoliset", sheets).map(_.hetu))
      }

      val (eiSuorittavatEnnen, ulkopuolisetEnnen) = haeEiSuorittavienJaUlkopuolistenHetut()
      eiSuorittavatEnnen should contain (oppija.hetu.get)
      ulkopuolisetEnnen should not contain (oppija.hetu.get)

      KoskiApplicationForTests.valpasOppivelvollisuudestaVapautusService
        .lisääOppivelvollisuudestaVapautus(
          UusiOppivelvollisuudestaVapautus(oppija.oid, LocalDate.of(2021, 1, 1), "624")
        )(session(ValpasMockUsers.valpasPyhtääJaAapajoenPeruskoulu)) should equal(Right(()))

      val (eiSuorittavat, ulkopuoliset) = haeEiSuorittavienJaUlkopuolistenHetut()
      eiSuorittavat should not contain (oppija.hetu.get)
      ulkopuoliset should contain (oppija.hetu.get)
    }

    "Näyttää Koskesta puuttuvan oppijan oppivelvollisuuden keskeytyksen" in {
      FixtureUtil.resetMockData(KoskiApplicationForTests)
      val oppija = ValpasMockOppijat.eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksia

      val keskeytys = loadHetuhaku(List(oppija.hetu.get)).collectFirst {
        case d: DataSheet if d.title == t.get("rouhinta_tab_ei_oppivelvollisuutta_suorittavat") => d.rows.collect {
          case r: ValpasRouhintaOppivelvollinenSheetRow => r
        }
      }.get.find(_.hetu == oppija.hetu).map(_.keskeytys)

      keskeytys should equal(Some("1.9.2021 -"))
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

  private def pelkkäHetuRows(titleKey: String, sheets: Seq[Sheet] = hakutulosSheets): Seq[ValpasRouhintaPelkkäHetuSheetRow] = sheets.collectFirst {
    case d: DataSheet if d.title == t.get(titleKey) => d.rows.collect {
      case r: ValpasRouhintaPelkkäHetuSheetRow => r
    }
  }.get

}
