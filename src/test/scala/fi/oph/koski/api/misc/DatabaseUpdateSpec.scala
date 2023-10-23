package fi.oph.koski.api.misc

import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudetWithAccessCheck
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.koskiuser.KoskiSpecificSession.systemUser
import fi.oph.koski.koskiuser.MockUsers.stadinAmmattiopistoTallentaja
import fi.oph.koski.organisaatio.MockOrganisaatiot.omnia
import fi.oph.koski.schema._
import fi.oph.koski.{DatabaseTestMethods, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class DatabaseUpdateSpec
  extends AnyFreeSpec
    with Matchers
    with OpiskeluoikeusTestMethodsAmmatillinen
    with SearchTestMethods
    with KoskiHttpSpec
    with DatabaseTestMethods {
  "Kun opiskeluoikeus päivitetään" - {
    "Oppilaitoksen muuttuessa oppilaitos_oid päivittyy" in {
      val opiskeluoikeus = setupOppijaWithAndGetOpiskeluoikeus(defaultOpiskeluoikeus, defaultHenkilö, stadinAmmattiopistoTallentaja)
      putOpiskeluoikeus(opiskeluoikeus.copy(oppilaitos = Some(Oppilaitos(omnia)), koulutustoimija = None)) {
        verifyResponseStatusOk()
      }
      opiskeluoikeus.oid.flatMap(oppilaitosOid) should equal(Some(omnia))
    }
  }

  private def oppilaitosOid(opiskeluoikeusOid: String): Option[String] =
    runDbSync(KoskiOpiskeluOikeudetWithAccessCheck(systemUser).filter(_.oid === opiskeluoikeusOid).map(_.oppilaitosOid).result).headOption
}
