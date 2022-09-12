package fi.oph.koski.fixture

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOpintopolkuHenkilöFacade, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.localization.MockLocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeushistoriaErrorRepository
import fi.oph.koski.suostumus.SuostumuksenPeruutusService
import fi.oph.koski.util.{Timing, Wait}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasOpiskeluoikeusFixtureState

object FixtureCreator {
  def generateOppijaOid(counter: Int) = "1.2.246.562.24." + "%011d".format(counter)
}

class FixtureCreator(application: KoskiApplication) extends Logging with Timing {
  private val raportointikantaService = application.raportointikantaService
  private var currentFixtureState: FixtureState = new NotInitializedFixtureState
  private val opiskeluoikeushistoriaErrorRepository = new OpiskeluoikeushistoriaErrorRepository(application.masterDatabase.db)

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = currentFixtureState.defaultOppijat

  def resetFixtures(
    fixtureState: FixtureState = koskiSpecificFixtureState,
    reloadRaportointikanta: Boolean = false
  ): Unit = synchronized {
    if (shouldUseFixtures) {
      val fixtureNameHasChanged = fixtureState.name != currentFixtureState.name
      application.cacheManager.invalidateAllCaches
      application.suostumuksenPeruutusService.deleteAll()
      currentFixtureState = fixtureState
      fixtureState.resetFixtures
      application.koskiLocalizationRepository.asInstanceOf[MockLocalizationRepository].reset
      application.valpasLocalizationRepository.asInstanceOf[MockLocalizationRepository].reset
      application.tiedonsiirtoService.index.deleteAll()
      application.päivitetytOpiskeluoikeudetJono.poistaKaikki()
      opiskeluoikeushistoriaErrorRepository.truncate
      application.valpasOppivelvollisuudestaVapautusService.db.deleteAll()

      if (reloadRaportointikanta || fixtureNameHasChanged) {
        raportointikantaService.loadRaportointikanta(force = true)
        Wait.until { raportointikantaService.isLoadComplete }
      }

      logger.info(s"Application fixtures reset to ${fixtureState.name}")
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
      throw new RuntimeException(s"Trying to use fixtures against an ElasticSearch index with more than ${application.perustiedotIndexer.SmallIndexMaxRows} rows")
    }
    useFixtures
  }

  def allOppijaOids: List[String] = (koskiSpecificFixtureState.oppijaOids ++ valpasFixtureState.oppijaOids).distinct // oids that should be considered when deleting fixture data

  def getCurrentFixtureStateName() = currentFixtureState.name

  lazy val koskiSpecificFixtureState = new KoskiSpecificFixtureState(application)
  lazy val valpasFixtureState = new ValpasOpiskeluoikeusFixtureState(application)
}

trait FixtureState extends Timing {
  def name: String
  def defaultOppijat: List[OppijaHenkilöWithMasterInfo]
  def resetFixtures: Unit

  def oppijaOids: List[String]
}

class NotInitializedFixtureState extends FixtureState {
  val name = "NOT_INITIALIZED"

  def defaultOppijat = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }

  def resetFixtures = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }

  def oppijaOids = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }
}

abstract class DatabaseFixtureState(application: KoskiApplication) extends FixtureState {
  def resetFixtures = {
    timed("Resetting database fixtures") (databaseFixtureCreator.resetFixtures)
    application.henkilöRepository.opintopolku.henkilöt.asInstanceOf[MockOpintopolkuHenkilöFacade].resetFixtures(defaultOppijat)
  }

  def oppijaOids: List[String] = (
    defaultOppijat.map(_.henkilö.oid) ++ (1 to (defaultOppijat.length) + 100).map(FixtureCreator.generateOppijaOid).toList
    ).distinct

  def databaseFixtureCreator: DatabaseFixtureCreator
}

class KoskiSpecificFixtureState(application: KoskiApplication) extends DatabaseFixtureState(application)  {
  val name = "KOSKI_SPECIFIC"

  def defaultOppijat: List[OppijaHenkilöWithMasterInfo] = KoskiSpecificMockOppijat.defaultOppijat

  lazy val databaseFixtureCreator: DatabaseFixtureCreator = new KoskiSpecificDatabaseFixtureCreator(application)
}
