package fi.oph.koski.turvakielto

import fi.oph.koski.db.KoskiTables.{KoskiOpiskeluOikeudet, YtrOpiskeluOikeudet}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.schema.Opiskeluoikeus
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
    val koskiRivit = runDbSync(KoskiOpiskeluOikeudet.filter(_.poistettu === false).result)
      .map(row => (row.oid, row.koulutusmuoto, row.toOpiskeluoikeusUnsafe: Opiskeluoikeus))
    val ytrRivit = runDbSync(YtrOpiskeluOikeudet.filter(_.poistettu === false).result)
      .map(row => (row.oid, row.koulutusmuoto, row.toOpiskeluoikeusUnsafe: Opiskeluoikeus))
    val rivit = koskiRivit ++ ytrRivit

    rivit should not be empty
    ytrRivit should not be empty

    val virheet = rivit.flatMap { case (oid, koulutusmuoto, opiskeluoikeus) =>
      Try(TurvakieltoService.poistaOpiskeluoikeudenTurvakiellonAlaisetTiedot(opiskeluoikeus)) match {
        case Success(_) => None
        case Failure(e) => Some(s"oid=$oid koulutusmuoto=$koulutusmuoto: ${e.getClass.getSimpleName}: ${e.getMessage}")
      }
    }

    withClue(s"TurvakieltoService kaatui seuraaville opiskeluoikeuksille:\n${virheet.mkString("\n")}\n") {
      virheet shouldBe empty
    }
  }
}
