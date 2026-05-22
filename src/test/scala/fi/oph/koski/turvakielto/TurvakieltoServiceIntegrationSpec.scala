package fi.oph.koski.turvakielto

import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudet
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.{DatabaseTestMethods, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

class TurvakieltoServiceIntegrationSpec
  extends AnyFreeSpec
    with Matchers
    with KoskiHttpSpec
    with DatabaseTestMethods {

  implicit private val systemUser: SensitiveDataAllowed = SensitiveDataAllowed.SystemUser

  "Turvakieltosiivous ei kaadu fixturen yhdessäkään Koski-opiskeluoikeudessa" in {
    val rows = runDbSync(KoskiOpiskeluOikeudet.filter(_.poistettu === false).result)
    rows should not be empty

    val virheet = rows.flatMap { row =>
      val opiskeluoikeus = row.toOpiskeluoikeusUnsafe
      Try(TurvakieltoService.poistaOpiskeluoikeudenTurvakiellonAlaisetTiedot(opiskeluoikeus)) match {
        case Success(_) => None
        case Failure(e) => Some(s"oid=${row.oid} koulutusmuoto=${row.koulutusmuoto}: ${e.getClass.getSimpleName}: ${e.getMessage}")
      }
    }

    withClue(s"TurvakieltoService kaatui seuraaville opiskeluoikeuksille:\n${virheet.mkString("\n")}\n") {
      virheet shouldBe empty
    }
  }
}
