package fi.oph.koski.raportointikanta

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.{DatabaseTestMethods, OpiskeluoikeusTestMethods}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.schema.MahdollisestiTunnustettu
import fi.oph.koski.schema.Opiskeluoikeus.Oid
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class OsasuorituksetSpec extends FreeSpec with Matchers with BeforeAndAfterAll with RaportointikantaTestMethods with OpiskeluoikeusTestMethods with DatabaseTestMethods {
  override protected def beforeAll(): Unit = loadRaportointikantaFixtures
  private lazy val raportointiDatabase = KoskiApplicationForTests.raportointiDatabase

  "Osasuoritukset" - {
    "Tunnustettu on 'true' tunnustetuille" in {
      tunnustetut should not be empty
      tunnustetut.foreach { case (_, osasuoritukset) =>
        osasuoritukset.exists(_.tunnustettu) should be(true)
      }
    }
  }

  private lazy val tunnustetut: Map[Oid, Seq[ROsasuoritusRow]] = {
    val tunnustettujaSisältävätOpiskeluoikeusOidit: List[Oid] = osasuoritukset.collect {
      case (oid, osasuoritus: MahdollisestiTunnustettu) if osasuoritus.tunnustettu.isDefined => oid
    }.distinct
    runDbSync(raportointiDatabase.ROsasuoritukset.filter(_.opiskeluoikeusOid inSetBind tunnustettujaSisältävätOpiskeluoikeusOidit).result).groupBy(_.opiskeluoikeusOid)
  }

  private def osasuoritukset = for {
    oppija <- koskeenTallennetutOppijat
    opiskeluoikeus <- oppija.opiskeluoikeudet
    if opiskeluoikeus.oid.isDefined
    suoritus <- opiskeluoikeus.suoritukset
    osasuoritus <- suoritus.osasuoritusLista
  } yield (opiskeluoikeus.oid.get, osasuoritus)
}
