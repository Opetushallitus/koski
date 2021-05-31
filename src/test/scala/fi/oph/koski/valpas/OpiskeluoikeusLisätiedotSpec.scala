package fi.oph.koski.valpas

import fi.oph.koski.KoskiApplicationForTests
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

  private def keyFor(oppijaLaajatTiedot: OppijaHakutilanteillaLaajatTiedot, ooOrdinal: Int) = {
    val oo = oppijaLaajatTiedot.oppija.opiskeluoikeudet(ooOrdinal)
    OpiskeluoikeusLisätiedotKey(
      oppijaOid = oppijaLaajatTiedot.oppija.henkilö.oid,
      opiskeluoikeusOid = oo.oid,
      oppilaitosOid = oo.oppilaitos.oid
    )
  }

  private lazy val oppijaLaajatTiedot = {
    val oppija = ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster
    oppijaService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(oppija.oid)(defaultSession).right.get
  }

  private lazy val vainViimeinenOpiskeluoikeus = oppijaLaajatTiedot.copy(
    oppija = oppijaLaajatTiedot.oppija.copy(
      opiskeluoikeudet = oppijaLaajatTiedot.oppija.opiskeluoikeudet.drop(2)
    )
  )

  "muu haku -tieto" - {
    "toimii tyhjällä hakuehdolla" in {
      repository.readForOppijat(Seq()) should equal(Seq())
    }

    "ei löydy jos ei tallennettu" in {
      repository.readForOppijat(Seq(oppijaLaajatTiedot)).head._2 should equal(Seq())
    }

    "tallentuu" in {
      repository.setMuuHaku(keyFor(oppijaLaajatTiedot, 1), value = false) should equal(HttpStatus.ok)
      repository.readForOppijat(Seq(oppijaLaajatTiedot)).head._2.head.muuHaku should equal(false)
    }

    "päivittyy" in {
      repository.setMuuHaku(keyFor(oppijaLaajatTiedot, 1), value = false) should equal(HttpStatus.ok)
      repository.setMuuHaku(keyFor(oppijaLaajatTiedot, 1), value = true) should equal(HttpStatus.ok)
      repository.readForOppijat(Seq(oppijaLaajatTiedot)).head._2.head.muuHaku should equal(true)
    }

    "on opiskeluoikeuskohtainen" - {
      "ensimmäiselle opiskeluoikeudelle asetettu tila ei näy toisella" in {
        repository.setMuuHaku(keyFor(oppijaLaajatTiedot, 1), value = true) should equal(HttpStatus.ok)
        repository.readForOppijat(Seq(vainViimeinenOpiskeluoikeus)).head._2 should equal(Seq())
      }

      "tilan voi asettaa ja hakea toiselle opiskeluoikeudelle erikseen" in {
        repository.setMuuHaku(keyFor(oppijaLaajatTiedot, 1), value = true) should equal(HttpStatus.ok)
        repository.setMuuHaku(keyFor(oppijaLaajatTiedot, 2), value = false) should equal(HttpStatus.ok)
        repository.readForOppijat(Seq(vainViimeinenOpiskeluoikeus)).head._2.head.muuHaku should equal(false)
      }
    }

    "voi hakea useita kerralla" in {
      repository.setMuuHaku(keyFor(oppijaLaajatTiedot, 1), value = true) should equal(HttpStatus.ok)
      repository.setMuuHaku(keyFor(oppijaLaajatTiedot, 2), value = false) should equal(HttpStatus.ok)
      val expected = Seq(
        (oppijaLaajatTiedot.oppija.opiskeluoikeudet(1).oid, true),
        (oppijaLaajatTiedot.oppija.opiskeluoikeudet(2).oid, false)
      )
      repository.readForOppijat(Seq(oppijaLaajatTiedot))
        .flatMap(r =>
          r._2.map(s => (s.opiskeluoikeusOid, s.muuHaku))
        ) should equal(expected)
    }
  }
}
