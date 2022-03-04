package fi.oph.koski.api

import fi.oph.koski.db.KoskiTables.OpiskeluOikeudet
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.{DatabaseTestMethods, KoskiApplicationForTests}
import fi.oph.koski.fixture.KoskiSpecificDatabaseFixtureCreator
import org.scalatest.matchers.should.Matchers
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.koskiuser.UserWithPassword

trait OpiskeluoikeudenMitätöintiJaPoistoTestMethods extends HttpSpecification with DatabaseTestMethods with Matchers {
  def ensimmäinenMitätöitävissäolevaOpiskeluoikeusIdJärjestyksessä: OpiskeluoikeusRow = runDbSync(
    OpiskeluOikeudet.filterNot(_.mitätöity).sortBy(_.id).result
  ).head

  def mitätöiOpiskeluoikeus(oid: String, user: UserWithPassword = defaultUser) = {
    delete(s"api/opiskeluoikeus/${oid}", headers = authHeaders(user))(verifyResponseStatusOk())
  }

  def ensimmäinenPoistettavissaolevaOpiskeluoikeusIdJärjestyksessä: OpiskeluoikeusRow = runDbSync(
    OpiskeluOikeudet.filterNot(_.mitätöity).filter(_.suoritustyypit.@>(List("vstvapaatavoitteinenkoulutus"))).sortBy(_.id).result
  ).head

  def poistaOpiskeluoikeus(oppijaOid: String, opiskeluoikeusOid: String) = {
    KoskiApplicationForTests.fixtureCreator.koskiSpecificFixtureState.databaseFixtureCreator.asInstanceOf[KoskiSpecificDatabaseFixtureCreator].peruutaSuostumusOpiskeluoikeudelta(
      oppijaOid, opiskeluoikeusOid
    ) should be(true)
  }
}
