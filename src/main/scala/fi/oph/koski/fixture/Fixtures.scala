package fi.oph.koski.fixture

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudet
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOpintopolkuHenkilöFacade, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.MockLocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeushistoriaErrorRepository
import fi.oph.koski.raportointikanta.OpiskeluoikeusLoader
import fi.oph.koski.util.{Timing, Wait}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasOpiskeluoikeusFixtureState
import slick.jdbc.SQLActionBuilder
import slick.jdbc.SetParameter.SetUnit

import scala.io.Source
import scala.util.Using

object FixtureCreator {
  def generateOppijaOid(counter: Int) = "1.2.246.562.24." + "%011d".format(counter)
}

class FixtureCreator(application: KoskiApplication) extends Logging with QueryMethods with Timing {
  val db = application.masterDatabase.db

  private val raportointikantaService = application.raportointikantaService
  private val ytrService = application.ytrDownloadService
  private val yoTodistusService = application.yoTodistusService
  private var currentFixtureState: FixtureState = new NotInitializedFixtureState
  private val opiskeluoikeushistoriaErrorRepository = new OpiskeluoikeushistoriaErrorRepository(application.masterDatabase.db)

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = currentFixtureState.defaultOppijat

  def resetFixtures(
    fixtureState: FixtureState = koskiSpecificFixtureState,
    reloadRaportointikanta: Boolean = false,
    reloadYtrData: Boolean = false,
  ): Unit = synchronized {
    if (shouldUseFixtures) {
      logger.info(s"Test: ${loadFixtureSnapshot(fixtureState)}")
//      val fixtureNameHasChanged = fixtureState.name != currentFixtureState.name
//      application.cacheManager.invalidateAllCaches
//      application.suostumuksenPeruutusService.deleteAll()
//      currentFixtureState = fixtureState
//      fixtureState.resetFixtures
//      application.koskiLocalizationRepository.asInstanceOf[MockLocalizationRepository].reset
//      application.valpasLocalizationRepository.asInstanceOf[MockLocalizationRepository].reset
//      application.tiedonsiirtoService.index.deleteAll()
//      application.päivitetytOpiskeluoikeudetJono.poistaKaikki()
//      opiskeluoikeushistoriaErrorRepository.truncate
//      yoTodistusService.reset()
//
//      if (reloadYtrData || fixtureNameHasChanged) {
//        ytrService.loadFixturesAndWaitUntilComplete(force = true)
//      }
//
//      if (reloadRaportointikanta || fixtureNameHasChanged) {
//        raportointikantaService.loadRaportointikanta(force = true, pageSize = OpiskeluoikeusLoader.LocalTestingBatchSize)
//        Wait.until { raportointikantaService.isLoadComplete }
//      }

      logger.info(s"Application fixtures reset to ${fixtureState.name}")
    }
  }

  def clearOppijanOpiskeluoikeudet(oppijaOid: String): Unit = synchronized {
    if (shouldUseFixtures) {
      // Mitätöi opiskeluoikeudet ensin
      implicit val user = KoskiSpecificSession.systemUser

      application.oppijaFacade.findOppija(oppijaOid, findMasterIfSlaveOid = false, useVirta = false, useYtr = false).flatMap(_.warningsToLeft) match {
        case Right(oppija) =>
          oppija.opiskeluoikeudet.map(_.oid.get).map(application.oppijaFacade.invalidateOpiskeluoikeus)
        case _ => Nil
      }
      application.perustiedotIndexer.sync(refresh = true)

      // Poista mahdolliset suostumuksen peruutukset
      application.suostumuksenPeruutusService.deleteAllForOppija(oppijaOid)

      // Poista oppijan suoritusjaot
      application.suoritusjakoRepository.deleteAllForOppija(oppijaOid)
      application.suoritusjakoRepositoryV2.deleteAllForOppija(oppijaOid)

      // VST:n yms. mitätöinnit jättävät raatoja oo-tauluun, poista nekin
      runDbSync(DBIO.sequence(Seq(
        KoskiOpiskeluOikeudet.filter(_.oppijaOid  inSetBind List(oppijaOid)).delete
      )))
    }
  }

  def shouldUseFixtures = {
    val useFixtures: Boolean = Environment.currentEnvironment(application.config) match {
      case Environment.UnitTest => true
      case Environment.Local => Environment.isUsingLocalDevelopmentServices(application) && !Environment.skipFixtures
      case _ => false
    }

    if (useFixtures && application.masterDatabase.util.databaseIsLarge) {
      throw new RuntimeException(s"Trying to use fixtures against a database with more than ${application.masterDatabase.smallDatabaseMaxRows} rows")
    }
    if (useFixtures && application.perustiedotIndexer.indexIsLarge) {
      throw new RuntimeException(s"Trying to use fixtures against an OpenSearch index with more than ${application.perustiedotIndexer.SmallIndexMaxRows} rows")
    }
    useFixtures
  }

  def allOppijaOids: List[String] = (koskiSpecificFixtureState.oppijaOids ++ valpasFixtureState.oppijaOids).distinct // oids that should be considered when deleting fixture data

  def getCurrentFixtureStateName() = currentFixtureState.name

  def loadFixtureSnapshot(fixtureState: FixtureState = koskiSpecificFixtureState): Either[Throwable, Unit] =
    (for {
      _ <- loadDatabaseSnapshot(application.masterDatabase.db, s"${fixtureState.resourcePath}/koski.sql")
      _ <- loadDatabaseSnapshot(application.raportointiDatabase.db, s"${fixtureState.resourcePath}/raportointikanta.sql")
      _ <- loadDatabaseSnapshot(application.valpasDatabase.db, s"${fixtureState.resourcePath}/valpas.sql")
    } yield ())
      .toEither

  private def loadDatabaseSnapshot(database: DB, resource: String) = {
    Using(Source.fromResource(resource)) { sqlDump =>
      logger.info(s"Load database snapshot from $resource")
      QueryMethods.runDbSync(database, DBIO.seq(
        sqlu"DROP SCHEMA IF EXISTS public CASCADE",
        sqlu"DROP SCHEMA IF EXISTS oph CASCADE",
        sqlu"DROP SCHEMA IF EXISTS confidential CASCADE",
      ))
      QueryMethods.runDbSync(database, SQLActionBuilder(sqlDump.mkString, SetUnit).asUpdate)
    }
  }

  lazy val koskiSpecificFixtureState = new KoskiSpecificFixtureState(application)
  lazy val valpasFixtureState = new ValpasOpiskeluoikeusFixtureState(application)
}

trait FixtureState extends Timing {
  def name: String
  def defaultOppijat: List[OppijaHenkilöWithMasterInfo]
  def resetFixtures: Unit
  def resourcePath: String

  def oppijaOids: List[String]
}

class NotInitializedFixtureState extends FixtureState {
  val name: String = NotInitializedFixtureState.name

  def defaultOppijat = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }

  def resetFixtures = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }

  def oppijaOids = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }

  def resourcePath = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }
}

object NotInitializedFixtureState {
  val name = "NOT_INITIALIZED"
}

abstract class DatabaseFixtureState(application: KoskiApplication) extends FixtureState {
  def resetFixtures = {
    application.henkilöRepository.opintopolku.henkilöt.asInstanceOf[MockOpintopolkuHenkilöFacade].resetFixtures(defaultOppijat)
    timed("Resetting database fixtures") (databaseFixtureCreator.resetFixtures)
  }

  def oppijaOids: List[String] = (
    defaultOppijat.map(_.henkilö.oid) ++ (1 to (defaultOppijat.length) + 100).map(FixtureCreator.generateOppijaOid).toList
    ).distinct

  def databaseFixtureCreator: DatabaseFixtureCreator
}

class KoskiSpecificFixtureState(application: KoskiApplication) extends DatabaseFixtureState(application)  {
  val name: String = KoskiSpecificFixtureState.name

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = KoskiSpecificMockOppijat.defaultOppijat

  lazy val databaseFixtureCreator: DatabaseFixtureCreator = new KoskiSpecificDatabaseFixtureCreator(application)

  def resourcePath: String = "fixtures/koski/default"
}

object KoskiSpecificFixtureState {
  val name = "KOSKI_SPECIFIC"
}
