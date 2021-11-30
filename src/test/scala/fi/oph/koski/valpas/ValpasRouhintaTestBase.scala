package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{DataSheet, Sheet}
import fi.oph.koski.valpas.rouhinta.ValpasRouhintaOppivelvollinenSheetRow
import org.scalatest.{Assertion, BeforeAndAfterAll}

trait ValpasRouhintaTestBase extends ValpasTestBase with BeforeAndAfterAll {

  def eiOppivelvollisuuttaSuorittavatOppijat: List[RouhintaExpectedData]

  def hakutulosSheets: Seq[Sheet]

  def expectEiOppivelvollisuuttaSuorittavatPropsMatch[T](f: ValpasRouhintaOppivelvollinenSheetRow => T, g: RouhintaExpectedData => T): Assertion = {
    eiOppivelvollisuuttaSuorittavat.map(o => (
      o.oppijaOid,
      f(o),
    )) should contain theSameElementsAs eiOppivelvollisuuttaSuorittavatOppijat.map(o => (
      o.oppija.oid,
      g(o),
    ))
  }

  lazy val eiOppivelvollisuuttaSuorittavienHetut = eiOppivelvollisuuttaSuorittavatOppijat.map(_.oppija.hetu.get)

  lazy val eiOppivelvollisuuttaSuorittavat = hakutulosSheets.collectFirst {
    case d: DataSheet if d.title == t.get("rouhinta_tab_ei_oppivelvollisuutta_suorittavat") => d.rows.collect {
      case r: ValpasRouhintaOppivelvollinenSheetRow => r
    }
  }.get

  lazy val t = new LocalizationReader(KoskiApplicationForTests.valpasLocalizationRepository, "fi")
}

case class RouhintaExpectedData(
  oppija: LaajatOppijaHenkilöTiedot,
  ooPäättymispäivä: String,
  ooViimeisinTila: Option[String],
  ooKoulutusmuoto: Option[String],
  ooToimipiste: Option[String],
  keskeytys: Option[String],
  kuntailmoitusKohde: Option[String],
  kuntailmoitusPvm: Option[String]
)
