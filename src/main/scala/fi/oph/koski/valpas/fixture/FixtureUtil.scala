package fi.oph.koski.valpas.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.valpas.repository.{MockRajapäivät, Rajapäivät}
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers


object FixtureUtil {
  def resetMockData(app: KoskiApplication, rajapäivät: MockRajapäivät = new MockRajapäivät()): FixtureState = synchronized {
    ValpasMockUsers.mockUsersEnabled = true
    Rajapäivät.enableMock(rajapäivät)
    app.fixtureCreator.resetFixtures(app.fixtureCreator.valpasFixtureState)
    FixtureState(app)
  }

  def clearMockData(app: KoskiApplication): FixtureState = synchronized {
    ValpasMockUsers.mockUsersEnabled = false
    Rajapäivät.disableMock()
    app.fixtureCreator.resetFixtures(app.fixtureCreator.koskiSpecificFixtureState)
    FixtureState(app)
  }
}

object FixtureState {
  def apply(application: KoskiApplication): FixtureState = FixtureState(
    fixture = application.fixtureCreator.getCurrentFixtureStateName(),
    rajapäivät = Rajapäivät.getCurrent()
  )
}

case class FixtureState(
  fixture: String,
  rajapäivät: Rajapäivät
)
