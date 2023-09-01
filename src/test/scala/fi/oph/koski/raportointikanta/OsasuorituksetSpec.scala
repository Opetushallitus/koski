package fi.oph.koski.raportointikanta

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.schema.MahdollisestiTunnustettu
import fi.oph.koski.schema.Opiskeluoikeus.Oid
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OsasuorituksetSpec extends AnyFreeSpec with Matchers with BeforeAndAfterAll with RaportointikantaTestMethods with OpiskeluoikeusTestMethods {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    reloadRaportointikanta
  }

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
    mainRaportointiDb.runDbSync(
      mainRaportointiDb.ROsasuoritukset
        .filter(_.opiskeluoikeusOid inSetBind tunnustettujaSisältävätOpiskeluoikeusOidit)
        .result
    ).groupBy(_.opiskeluoikeusOid)
  }

  private def osasuoritukset = for {
    oppija <- koskeenTallennetutOppijat
    opiskeluoikeus <- oppija.opiskeluoikeudet
    if opiskeluoikeus.oid.isDefined
    suoritus <- opiskeluoikeus.suoritukset
    osasuoritus <- suoritus.osasuoritusLista
  } yield (opiskeluoikeus.oid.get, osasuoritus)
}
