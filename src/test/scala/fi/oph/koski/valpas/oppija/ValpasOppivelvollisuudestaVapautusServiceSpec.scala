package fi.oph.koski.valpas.oppija

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import fi.oph.koski.valpas.ValpasTestBase
import fi.oph.koski.valpas.db.ValpasDatabaseFixtureLoader
import fi.oph.koski.valpas.opiskeluoikeusfixture.{FixtureUtil, ValpasMockOppijat}
import fi.oph.koski.valpas.oppivelvollisuudestavapautus.{UusiOppivelvollisuudestaVapautus, ValpasOppivelvollisuudestaVapautusService}
import fi.oph.koski.valpas.valpasrepository.ValpasExampleData
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.BeforeAndAfterAll

import java.time.LocalDate

class ValpasOppivelvollisuudestaVapautusServiceSpec extends ValpasTestBase with BeforeAndAfterAll {
  val service: ValpasOppivelvollisuudestaVapautusService = KoskiApplicationForTests.valpasOppivelvollisuudestaVapautusService
  implicit val defaultImplSession = session(ValpasMockUsers.valpasHelsinki)
  val fixtureLoader = new ValpasDatabaseFixtureLoader(KoskiApplicationForTests)

  override def beforeAll(): Unit = {
    service.db.deleteAll()
    FixtureUtil.resetMockData(KoskiApplicationForTests)
  }

  "Oppivelvollisuudesta vapautus" - {
    "Oppivelvollisuuden vapautustiedon perusteella voidaan minimoida luovutettua dataa" in {
      val oppivelvollisuudestaFikstuurissaVapautetutOppijat = ValpasExampleData.oppivelvollisuudestaVapautetut.map(_._1)
      val vapautettavaOppija = ValpasMockOppijat.eronnutOppija
      val kaikkiVapautetutOppijat = oppivelvollisuudestaFikstuurissaVapautetutOppijat ++ List(vapautettavaOppija)

      val oppivelvollinenOppija = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021

      val oppijat = kaikkiVapautetutOppijat ++ List(oppivelvollinenOppija)

      service.lisääOppivelvollisuudestaVapautus(UusiOppivelvollisuudestaVapautus(vapautettavaOppija.oid, LocalDate.of(2000, 8, 1), "091")) should equal(Right(()))
      val result = service.mapVapautetutOppijat(oppijat, { o: LaajatOppijaHenkilöTiedot => List(o.oid) }) {
        case (oppija, pvm) => oppija.copy(etunimet = s"*VAPAUTETTU ${pvm.vapautettu.format(finnishDateFormat)}*")
      }

      result.zip(oppijat).foreach {
        case (oppija, _) if kaikkiVapautetutOppijat.exists(_.oid == oppija.oid) => oppija.etunimet should equal("*VAPAUTETTU 1.8.2000*")
        case (oppija, alkupOppija) => oppija.etunimet should equal(alkupOppija.etunimet)
      }
    }
  }
}
