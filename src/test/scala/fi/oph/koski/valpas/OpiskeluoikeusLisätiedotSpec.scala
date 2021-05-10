package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.valpas.db.ValpasSchema.OpiskeluoikeusLisätiedotKey
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat
import org.scalatest.BeforeAndAfterAll


class OpiskeluoikeusLisätiedotSpec extends ValpasTestBase with BeforeAndAfterAll {
  private val repository = KoskiApplicationForTests.valpasOpiskeluoikeusLisätiedotRepository
  private val oppijaService = KoskiApplicationForTests.valpasOppijaService

  override def afterAll(): Unit = {
    repository.truncate() // Clean up database
    super.afterAll()
  }

  private def keyFor(oppija: LaajatOppijaHenkilöTiedot, ooOrdinal: Int) = {
    val valpasOppija = oppijaService.getOppijaHakutilanteillaLaajatTiedot(oppija.oid)(defaultSession).right.get
    val oo = valpasOppija.oppija.opiskeluoikeudet(ooOrdinal)
    OpiskeluoikeusLisätiedotKey(
      oppijaOid = oppija.oid,
      opiskeluoikeusOid = oo.oid,
      oppilaitosOid = oo.oppilaitos.oid
    )
  }

  "muu haku -tieto" - {
    lazy val oo1Key = keyFor(ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia, 0)
    lazy val oo2Key = keyFor(ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia, 1)

    "ei löydy jos ei tallennettu" in {
      repository.read(Set(oo1Key)) should equal(Seq())
    }

    "tallentuu" in {
      repository.setMuuHaku(oo1Key, value = false) should equal(HttpStatus.ok)
      repository.read(Set(oo1Key)).head.muuHaku should equal(false)
    }

    "päivittyy" in {
      repository.setMuuHaku(oo1Key, value = true) should equal(HttpStatus.ok)
      repository.read(Set(oo1Key)).head.muuHaku should equal(true)
    }

    "on opiskeluoikeuskohtainen" in {
      repository.read(Set(oo2Key)) should equal(Seq())
      repository.setMuuHaku(oo2Key, value = false) should equal(HttpStatus.ok)
      repository.read(Set(oo2Key)).head.muuHaku should equal(false)
    }

    "voi hakea useita kerralla" in {
      val expected = Set(
        (oo1Key.opiskeluoikeusOid, true),
        (oo2Key.opiskeluoikeusOid, false)
      )
      repository.read(Set(oo1Key, oo2Key))
        .map(r => (r.opiskeluoikeusOid, r.muuHaku))
        .toSet should equal(expected)
    }
  }
}
