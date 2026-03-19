package fi.oph.koski.todistus.tiedote

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExamplesKielitutkinto
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, YleisenKielitutkinnonSuoritus}
import fi.oph.koski.util.Wait
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

import java.time.LocalDate

class KielitutkintotodistusTiedoteSpecHelpers extends AnyFreeSpec with KoskiHttpSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach with PutOpiskeluoikeusTestMethods[KielitutkinnonOpiskeluoikeus] {
  def tag = implicitly[reflect.runtime.universe.TypeTag[KielitutkinnonOpiskeluoikeus]]

  override val defaultUser: KoskiMockUser = MockUsers.paakayttaja

  val app = KoskiApplicationForTests

  val vahvistettuKielitutkinnonOpiskeluoikeus = ExamplesKielitutkinto.YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2011, 1, 3), "FI", "kt")
  val defaultOpiskeluoikeus = vahvistettuKielitutkinnonOpiskeluoikeus

  override protected def beforeAll(): Unit = {
    resetFixtures()
  }

  protected def cleanup(): Unit = {
    waitForSchedulerIdle()
    app.kielitutkintotodistusTiedoteRepository.truncateForLocal()
    app.todistusRepository.truncateForLocal()
    mockTiedotuspalveluClient.reset()
  }

  protected def mockTiedotuspalveluClient: MockTiedotuspalveluClient =
    app.tiedotuspalveluClient.asInstanceOf[MockTiedotuspalveluClient]

  protected def waitForSchedulerIdle(): Unit = {
    Wait.until(!app.kielitutkintotodistusTiedoteScheduler.schedulerInstance.exists(_.isTaskRunning))
  }

  protected def withoutRunningTiedoteScheduler[T](f: => T): T =
    try {
      waitForSchedulerIdle()
      app.kielitutkintotodistusTiedoteScheduler.schedulerInstance.foreach(_.suspend())
      waitForSchedulerIdle()
      // Tyhjennä schedulerin mahdollisesti luomat jobit ennen testiä
      app.kielitutkintotodistusTiedoteRepository.truncateForLocal()
      app.todistusRepository.truncateForLocal()
      mockTiedotuspalveluClient.reset()
      f
    } finally {
      app.kielitutkintotodistusTiedoteScheduler.schedulerInstance.foreach(_.unsuspend())
    }

  def getVahvistettuKielitutkinnonOpiskeluoikeusOid(oppijaOid: String): Option[String] = {
    getOpiskeluoikeudet(oppijaOid).find(_.suoritukset.exists {
      case s: YleisenKielitutkinnonSuoritus if s.vahvistus.isDefined => true
      case _ => false
    }).flatMap(_.oid)
  }
}
