package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.DataSheet
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.rouhinta.{OppivelvollinenRow, PelkkäHetuRow, ValpasRouhintaService}
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.{Assertion, BeforeAndAfterAll}

class ValpasRouhintaSpec extends ValpasTestBase with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = {
    FixtureUtil.resetMockData(KoskiApplicationForTests)
  }

  val eiOppivelvollisuuttaSuorittavatOppijat = List(
    HetuhakuExpectedData(
      oppija = ValpasMockOppijat.aikuistenPerusopetuksestaEronnut,
      ooPäättymispäivä = "30.8.2021",
      ooViimeisinTila = Some("Eronnut"),
      ooKoulutusmuoto = Some("Aikuisten perusopetus"),
      ooToimipiste = Some("Ressun lukio"),
      keskeytys = None,
    ),
    HetuhakuExpectedData(
      oppija = ValpasMockOppijat.eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksia,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
    ),
    HetuhakuExpectedData(
      oppija = ValpasMockOppijat.eiKoskessaOppivelvollinen,
      ooPäättymispäivä = t.get("rouhinta_ei_opiskeluoikeutta"),
      ooViimeisinTila = None,
      ooKoulutusmuoto = None,
      ooToimipiste = None,
      keskeytys = None,
    ),
    HetuhakuExpectedData(
      oppija = ValpasMockOppijat.oppivelvollisuusKeskeytettyEiOpiskele,
      ooPäättymispäivä = "30.5.2021",
      ooViimeisinTila = Some("Valmistunut"),
      ooKoulutusmuoto = Some("Perusopetus"),
      ooToimipiste = Some("Jyväskylän normaalikoulu"),
      keskeytys = Some("1.6.2021 -"),
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
  }

  lazy val hetuhaku = loadHetuhaku

  def expectEiOppivelvollisuuttaSuorittavatPropsMatch[T](f: OppivelvollinenRow => T, g: HetuhakuExpectedData => T): Assertion = {
    eiOppivelvollisuuttaSuorittavat.map(o => (
      o.oppijaOid,
      f(o),
    )) should contain theSameElementsAs eiOppivelvollisuuttaSuorittavatOppijat.map(o => (
      o.oppija.oid,
      g(o),
    ))
  }

  lazy val eiOppivelvollisuuttaSuorittavienHetut = eiOppivelvollisuuttaSuorittavatOppijat.map(_.oppija.hetu.get)

  lazy val eiOppivelvollisuuttaSuorittavat = hetuhaku.collectFirst {
    case d: DataSheet if d.title == t.get("rouhinta_tab_ei_oppivelvollisuutta_suorittavat") => d.rows.collect {
      case r: OppivelvollinenRow => r
    }
  }.get

  lazy val oppivelvollisuuttaSuorittavat = pelkkäHetuRows("rouhinta_tab_oppivelvollisuutta_suorittavat")

  lazy val oppivelvollisuudenUlkopuoliset = pelkkäHetuRows("rouhinta_tab_ovl_ulkopuoliset")

  lazy val oppijanumerorekisterinUlkopuoliset = pelkkäHetuRows("rouhinta_tab_onr_ulkopuoliset")

  lazy val virheelliset = pelkkäHetuRows("rouhinta_tab_virheelliset_hetut")

  lazy val t = new LocalizationReader(KoskiApplicationForTests.valpasLocalizationRepository, "fi")

  private def loadHetuhaku() = {
    new ValpasRouhintaService(KoskiApplicationForTests)
      .haeHetulistanPerusteellaExcel(
        hetut = eiOppivelvollisuuttaSuorittavienHetut ++
          oppivelvollisuuttaSuorittavienHetut ++
          oppivelvollisuudenUlkopuolistenHetut ++
          oppijanumerorekisterinUlkopuolisetHetut ++
          virheellisetHetut,
        language = "fi",
        password = Some("hunter2")
      )(session(ValpasMockUsers.valpasHelsinki))
      .fold(
        error => fail(s"Haku hetulistalla epäonnistui: $error"),
        result => result.sheets
      )
  }

  private def pelkkäHetuRows(titleKey: String): Seq[PelkkäHetuRow] = hetuhaku.collectFirst {
    case d: DataSheet if d.title == t.get(titleKey) => d.rows.collect {
      case r: PelkkäHetuRow => r
    }
  }.get

}

case class HetuhakuExpectedData(
  oppija: LaajatOppijaHenkilöTiedot,
  ooPäättymispäivä: String,
  ooViimeisinTila: Option[String],
  ooKoulutusmuoto: Option[String],
  ooToimipiste: Option[String],
  keskeytys: Option[String],
)
