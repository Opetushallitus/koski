package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables._
import fi.oph.koski.db._
import fi.oph.koski.henkilo.{MockOppijat, OppijaHenkilö, OppijaHenkilöWithMasterInfo, VerifiedHenkilöOid}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.perustiedot.{OpiskeluoikeudenOsittaisetTiedot, OpiskeluoikeudenPerustiedot}
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing
import slick.dbio.DBIO

import scala.reflect.runtime.universe.TypeTag

abstract class DatabaseFixtureCreator(application: KoskiApplication, opiskeluoikeusFixtureCacheTableName: String, opiskeluoikeusHistoriaFixtureCacheTableName: String) extends QueryMethods with Timing {
  implicit val user = KoskiSpecificSession.systemUser
  protected val validator = application.validator
  val db = application.masterDatabase.db
  implicit val accessType = AccessType.write

  protected def validateOpiskeluoikeus[T: TypeTag](oo: T, session: KoskiSpecificSession = user): T =
    validator.extractAndValidateOpiskeluoikeus(JsonSerializer.serialize(oo))(session, AccessType.write) match {
      case Right(opiskeluoikeus) => opiskeluoikeus.asInstanceOf[T]
      case Left(status) => throw new RuntimeException("Fixture insert failed for " + JsonSerializer.write(oo) + ": " + status)
    }

  protected lazy val opiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = validatedOpiskeluoikeudet ++ invalidOpiskeluoikeudet

  var fixtureCacheCreated = false
  var cachedPerustiedot: Option[Seq[OpiskeluoikeudenOsittaisetTiedot]] = None

  def resetFixtures: Unit = {
    if (!application.masterDatabase.isLocal) throw new IllegalStateException("Trying to reset fixtures in remote database")

    val henkilöOids = application.fixtureCreator.allOppijaOids.sorted

    runDbSync(DBIO.sequence(Seq(
      OpiskeluOikeudet.filter(_.oppijaOid inSetBind (henkilöOids)).delete,
      Tables.Henkilöt.filter(_.oid inSetBind henkilöOids).delete,
      Preferences.delete,
      Tables.PerustiedotSync.delete,
      Tables.SuoritusJako.delete,
      Tables.SuoritusJakoV2.delete,
    ) ++ oppijat.map(application.henkilöCache.addHenkilöAction)))

    application.perustiedotIndexer.sync(refresh = false) // Make sure the sync queue is empty
    application.perustiedotIndexer.deleteByOppijaOids(henkilöOids, refresh = false)

    if (!fixtureCacheCreated) {
      cachedPerustiedot = Some(opiskeluoikeudet.map { case (henkilö, opiskeluoikeus) =>
        val id = application.opiskeluoikeusRepository.createOrUpdate(VerifiedHenkilöOid(henkilö), opiskeluoikeus, false).right.get.id
        OpiskeluoikeudenPerustiedot.makePerustiedot(id, opiskeluoikeus, application.henkilöRepository.opintopolku.withMasterInfo(henkilö))
      })
      application.perustiedotIndexer.sync(refresh = true)
      val henkilöOidsIn = henkilöOids.map("'" + _ + "'").mkString(",")
      runDbSync(DBIO.seq(
        sqlu"drop table if exists #$opiskeluoikeusFixtureCacheTableName",
        sqlu"create table #$opiskeluoikeusFixtureCacheTableName as select * from opiskeluoikeus where oppija_oid in (#$henkilöOidsIn)",
        sqlu"drop table if exists #$opiskeluoikeusHistoriaFixtureCacheTableName",
        sqlu"create table #$opiskeluoikeusHistoriaFixtureCacheTableName as select * from opiskeluoikeushistoria where opiskeluoikeus_id in (select id from #$opiskeluoikeusFixtureCacheTableName)"
      ))
      fixtureCacheCreated = true
    } else {
      runDbSync(DBIO.seq(
        sqlu"alter table opiskeluoikeus disable trigger update_opiskeluoikeus_aikaleima",
        sqlu"insert into opiskeluoikeus select * from #$opiskeluoikeusFixtureCacheTableName",
        sqlu"insert into opiskeluoikeushistoria select * from #$opiskeluoikeusHistoriaFixtureCacheTableName",
        sqlu"alter table opiskeluoikeus enable trigger update_opiskeluoikeus_aikaleima"
      ))
      application.perustiedotIndexer.updatePerustiedot(cachedPerustiedot.get, upsert = true, refresh = true)
    }
  }

  protected def oppijat: List[OppijaHenkilöWithMasterInfo]

  protected def validatedOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)]

  protected def invalidOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)]
}
