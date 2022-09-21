package fi.oph.koski.valpas.opiskeluoikeusfixture

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.log.Logging
import fi.oph.koski.valpas.db.ValpasDatabaseFixtureLoader
import fi.oph.koski.valpas.opiskeluoikeusfixture.FixtureUtil.DefaultTarkastelupäivä
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

class StatefulFixtureUtil(application: KoskiApplication) {
  def resetMockData(tarkastelupäivä: LocalDate = DefaultTarkastelupäivä, force: Boolean = false): FixtureState = {
    val state = FixtureState(application)
    FixtureUtil.resetMockData(
      application,
      tarkastelupäivä,
      resetKoskiFixtures = force || state.requiresFullResetFor(tarkastelupäivä)
    )
  }

  def clearMockData(): FixtureState = {
    FixtureUtil.clearMockData(application)
  }
}

object FixtureUtil extends Logging {
  val DefaultTarkastelupäivä: LocalDate = date(2021, 9, 5)

  def resetMockData(
    app: KoskiApplication,
    tarkastelupäivä: LocalDate = DefaultTarkastelupäivä,
    resetKoskiFixtures: Boolean = true,
  ): FixtureState = synchronized {
    ValpasMockUsers.mockUsersEnabled = true
    app.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    new ValpasDatabaseFixtureLoader(app).reset()
    if (resetKoskiFixtures) {
      app.fixtureCreator.resetFixtures(app.fixtureCreator.valpasFixtureState, reloadRaportointikanta = true)
    }
    logger.info("Valpas mock data reset DONE")
    FixtureState(app)
  }

  def clearMockData(app: KoskiApplication): FixtureState = synchronized {
    ValpasMockUsers.mockUsersEnabled = false
    app.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService].poistaMockTarkastelupäivä()
    app.fixtureCreator.resetFixtures(app.fixtureCreator.koskiSpecificFixtureState)
    FixtureState(app)
  }
}

object FixtureState {
  def apply(application: KoskiApplication): FixtureState = FixtureState(
    fixture = application.fixtureCreator.getCurrentFixtureStateName(),
    tarkastelupäivä = application.valpasRajapäivätService.tarkastelupäivä
  )
}

case class FixtureState(
  fixture: String,
  tarkastelupäivä: LocalDate
) {
  def requiresFullResetFor(pvm: LocalDate): Boolean =
    fixture != ValpasOpiskeluoikeusFixtureState.name || tarkastelupäivä != pvm
}
