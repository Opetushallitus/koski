package fi.oph.koski.api

import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudet
import fi.oph.koski.db.KoskiOpiskeluoikeusRow
import fi.oph.koski.{DatabaseTestMethods, KoskiApplicationForTests}
import fi.oph.koski.fixture.KoskiSpecificDatabaseFixtureCreator
import org.scalatest.matchers.should.Matchers
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.util.Wait

trait OpiskeluoikeudenMitätöintiJaPoistoTestMethods extends HttpSpecification with DatabaseTestMethods with Matchers {
  def ensimmäinenMitätöitävissäolevaOpiskeluoikeusIdJärjestyksessä: KoskiOpiskeluoikeusRow = runDbSync(
    KoskiOpiskeluOikeudet.filterNot(_.mitätöity).sortBy(_.id).result
  ).head

  def mitätöiOpiskeluoikeus(oid: String, user: UserWithPassword = defaultUser): Unit = {
    delete(s"api/opiskeluoikeus/${oid}", headers = authHeaders(user))(verifyResponseStatusOk())
  }

  def mitätöiOpiskeluoikeusCallback[A](oid: String, user: UserWithPassword = defaultUser)(f: => A = verifyResponseStatusOk()): A = {
    delete(s"api/opiskeluoikeus/${oid}", headers = authHeaders(user))(f)
  }

  def ensimmäinenPoistettavissaolevaOpiskeluoikeusIdJärjestyksessä: KoskiOpiskeluoikeusRow = runDbSync(
    KoskiOpiskeluOikeudet.filterNot(_.mitätöity).filter(_.suoritustyypit.@>(List("vstvapaatavoitteinenkoulutus"))).sortBy(_.id).result
  ).head

  def poistaOpiskeluoikeus(oppijaOid: String, opiskeluoikeusOid: String) = {
    KoskiApplicationForTests.fixtureCreator.koskiSpecificFixtureState.databaseFixtureCreator.asInstanceOf[KoskiSpecificDatabaseFixtureCreator].peruutaSuostumusOpiskeluoikeudelta(
      oppijaOid, opiskeluoikeusOid
    ) should be(true)
  }

  def päivitäOpiskeluoikeus(oo: KoskiOpiskeluoikeusRow) = {
    runDbSync(KoskiOpiskeluOikeudet.insertOrUpdate(oo))
  }
}
