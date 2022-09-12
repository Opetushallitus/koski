package fi.oph.koski.valpas.oppija

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.valpas.ValpasTestBase
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import fi.oph.koski.valpas.valpasrepository.ValpasExampleData
import org.scalatest.BeforeAndAfterAll

import java.time.LocalDate

class ValpasOppijanPoistoreServiceSpec extends ValpasTestBase with BeforeAndAfterAll {
  val service: ValpasOppivelvollisuudestaVapautusService = KoskiApplicationForTests.valpasOppivelvollisuudestaVapautusService
  implicit val defaultImplSession = defaultSession

  override def beforeAll(): Unit = {
    service.db.deleteAll()
  }

  "Oppijan poisto" - {
    "Oppijan poiston jälkeen oid siivotaan filtteröinnissä" in {
      val valmiiksiPoistetutOppijat = ValpasExampleData.oppivelvollisuudestaVapautetut.map(_._1)
      val poistettuOppija = ValpasMockOppijat.eronnutOppija
      val oppivelvollinenOppija = ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021

      val oppijat = valmiiksiPoistetutOppijat ++ List(poistettuOppija, oppivelvollinenOppija)

      service.lisääOppivelvollisuudestaVapautus(poistettuOppija.oid, LocalDate.of(2022, 9, 13))
      val result = service.mapVapautetutOppijat(oppijat, { o: LaajatOppijaHenkilöTiedot => List(o.oid) }) {
        _.copy(etunimet = "*POISTETTU*")
      }

      result.zip(oppijat).foreach {
        case (oppija, _) if oppija.oid == poistettuOppija.oid => oppija.etunimet should equal("*POISTETTU*")
        case (oppija, alkupOppija) => oppija.etunimet should equal(alkupOppija.etunimet)
      }
    }
  }
}
