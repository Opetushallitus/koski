package fi.oph.koski.fixture

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.db.KoskiTables.KoskiOpiskeluOikeudet
import fi.oph.koski.db.QueryMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, MockOpintopolkuHenkilöFacade, OppijaHenkilöWithMasterInfo, OppijanumerorekisteriKotikuntahistoriaRow}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.localization.MockLocalizationRepository
import fi.oph.koski.log.Logging
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeushistoriaErrorRepository
import fi.oph.koski.raportointikanta.OpiskeluoikeusLoader
import fi.oph.koski.util.{Timing, Wait}
import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasOpiskeluoikeusFixtureState

import scala.collection.mutable

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
  def defaultKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] = currentFixtureState.defaultKuntahistoriat
  def defaultTurvakieltoKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] = currentFixtureState.defaultTurvakieltoKuntahistoriat

  def resetFixtures(
    fixtureState: FixtureState = koskiSpecificFixtureState,
    reloadRaportointikanta: Boolean = false,
    reloadYtrData: Boolean = false,
  ): Unit = synchronized {
    def reloadRaportointikantaAndWait(): Unit = {
      raportointikantaService.loadRaportointikanta(force = true, pageSize = OpiskeluoikeusLoader.LocalTestingBatchSize)
      Wait.until {
        raportointikantaService.isLoadComplete
      }
    }

    if (shouldUseFixtures) {
      val fixtureNameHasChanged = fixtureState.name != currentFixtureState.name
      application.cacheManager.invalidateAllCaches

      if (raportointikantaService.isEmpty && reloadRaportointikanta) {
        reloadRaportointikantaAndWait()
      }

      application.suostumuksenPeruutusService.deleteAll()
      currentFixtureState = fixtureState
      fixtureState.resetFixtures
      application.koskiLocalizationRepository.asInstanceOf[MockLocalizationRepository].reset
      application.valpasLocalizationRepository.asInstanceOf[MockLocalizationRepository].reset
      application.tiedonsiirtoService.index.deleteAll()
      application.päivitetytOpiskeluoikeudetJono.poistaKaikki()
      opiskeluoikeushistoriaErrorRepository.truncate
      application.massaluovutusService.truncate()
      application.todistusService.truncate()
      yoTodistusService.reset()

      if (reloadYtrData || fixtureNameHasChanged) {
        ytrService.loadFixturesAndWaitUntilComplete(force = true)
      }

      if (reloadRaportointikanta || fixtureNameHasChanged) {
        reloadRaportointikantaAndWait()
      }

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

      application.omaDataOAuth2Repository.deleteAllForOppija(oppijaOid)

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

  lazy val koskiSpecificFixtureState = new KoskiSpecificFixtureState(application)
  lazy val valpasFixtureState = new ValpasOpiskeluoikeusFixtureState(application)
}

trait FixtureState extends Timing {
  def name: String
  def defaultOppijat: List[OppijaHenkilöWithMasterInfo]
  def defaultKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]]
  def defaultTurvakieltoKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]]
  def resetFixtures: Unit

  def oppijaOids: List[String]
}

class NotInitializedFixtureState extends FixtureState {
  val name: String = NotInitializedFixtureState.name

  def defaultOppijat = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }

  def defaultKuntahistoriat = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }
  def defaultTurvakieltoKuntahistoriat = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }

  def resetFixtures = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }

  def oppijaOids = {
    throw new IllegalStateException("Internal error: Fixtures not initialized correctly")
  }
}

object NotInitializedFixtureState {
  val name = "NOT_INITIALIZED"
}

abstract class DatabaseFixtureState(application: KoskiApplication) extends FixtureState {
  def resetFixtures = {
    application.henkilöRepository.opintopolku.henkilöt.asInstanceOf[MockOpintopolkuHenkilöFacade].resetFixtures(defaultOppijat, defaultKuntahistoriat, defaultTurvakieltoKuntahistoriat)
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
  def defaultKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] = KoskiSpecificMockOppijat.defaultKuntahistoriat
  def defaultTurvakieltoKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] = KoskiSpecificMockOppijat.defaultTurvakieltoKuntahistoriat


  lazy val databaseFixtureCreator: DatabaseFixtureCreator = new KoskiSpecificDatabaseFixtureCreator(application)
}

object KoskiSpecificFixtureState {
  val name = "KOSKI_SPECIFIC"
}
