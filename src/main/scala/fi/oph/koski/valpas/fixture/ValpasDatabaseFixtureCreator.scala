package fi.oph.koski.valpas.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.fixture.KoskiDatabaseFixtureCreator
import fi.oph.koski.henkilo.{MockOppijat, OppijaHenkilö, VerifiedHenkilöOid}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.perustiedot.{OpiskeluoikeudenOsittaisetTiedot, OpiskeluoikeudenPerustiedot}
import fi.oph.koski.schema._
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import slick.dbio.DBIO

class ValpasDatabaseFixtureCreator(application: KoskiApplication) extends KoskiDatabaseFixtureCreator(application) {
  var fixtureCacheCreated = false
  var cachedPerustiedot: Option[Seq[OpiskeluoikeudenOsittaisetTiedot]] = None

  def resetFixtures: Unit = {
    if (database.config.isRemote) throw new IllegalStateException("Trying to reset fixtures in remote database")

    application.perustiedotSyncScheduler.sync(refresh = false)

    val henkilöOids = MockOppijat.oids.sorted

    runDbSync(DBIO.sequence(Seq(
      OpiskeluOikeudet.filter(_.oppijaOid inSetBind (henkilöOids)).delete,
      Tables.Henkilöt.filter(_.oid inSetBind henkilöOids).delete,
      Preferences.delete,
      Tables.PerustiedotSync.delete,
      Tables.SuoritusJako.delete,
      Tables.SuoritusJakoV2.delete,
    ) ++ ValpasMockOppijat.defaultOppijat.map(application.henkilöCache.addHenkilöAction)))

    application.perustiedotIndexer.deleteByOppijaOids(henkilöOids, refresh = false)

    if (!fixtureCacheCreated) {
      cachedPerustiedot = Some(opiskeluoikeudet.map { case (henkilö, opiskeluoikeus) =>
        val id = application.opiskeluoikeusRepository.createOrUpdate(VerifiedHenkilöOid(henkilö), opiskeluoikeus, false).right.get.id
        OpiskeluoikeudenPerustiedot.makePerustiedot(id, opiskeluoikeus, application.henkilöRepository.opintopolku.withMasterInfo(henkilö))
      })
      application.perustiedotIndexer.updatePerustiedot(cachedPerustiedot.get, upsert = true, refresh = true)
      val henkilöOidsIn = henkilöOids.map("'" + _ + "'").mkString(",")
      runDbSync(DBIO.seq(
        sqlu"drop table if exists opiskeluoikeus_valpas_fixture",
        sqlu"create table opiskeluoikeus_valpas_fixture as select * from opiskeluoikeus where oppija_oid in (#$henkilöOidsIn)",
        sqlu"drop table if exists opiskeluoikeushistoria_valpas_fixture",
        sqlu"create table opiskeluoikeushistoria_valpas_fixture as select * from opiskeluoikeushistoria where opiskeluoikeus_id in (select id from opiskeluoikeus_fixture)"
      ))
      fixtureCacheCreated = true
    } else {
      runDbSync(DBIO.seq(
        sqlu"alter table opiskeluoikeus disable trigger update_opiskeluoikeus_aikaleima",
        sqlu"insert into opiskeluoikeus select * from opiskeluoikeus_valpas_fixture",
        sqlu"insert into opiskeluoikeushistoria select * from opiskeluoikeushistoria_valpas_fixture",
        sqlu"alter table opiskeluoikeus enable trigger update_opiskeluoikeus_aikaleima"
      ))
      application.perustiedotIndexer.updatePerustiedot(cachedPerustiedot.get, upsert = true, refresh = true)
    }
  }

  protected lazy val validatedOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = {
    defaultOpiskeluOikeudet.zipWithIndex.map { case ((henkilö, oikeus), index) =>
      timed(s"Validating fixture ${index}", 500) {
        validator.validateAsJson(Oppija(henkilö.toHenkilötiedotJaOid, List(oikeus))) match {
          case Right(oppija) => (henkilö, oppija.tallennettavatOpiskeluoikeudet.head)
          case Left(status) => throw new RuntimeException(
            s"Fixture insert failed for ${henkilö.etunimet} ${henkilö.sukunimi} with data ${JsonSerializer.write(oikeus)}: ${status}"
          )
        }
      }
    }
  }

  protected lazy val invalidOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = List()

  private def defaultOpiskeluOikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = List(
    (ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021, ValpasExampleData.oppivelvollinenYsiluokkaKeskenKeväällä2021Opiskeluoikeus)
  )
}
