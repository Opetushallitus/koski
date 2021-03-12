package fi.oph.koski.valpas.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.valpas.repository.{MockRajapäivät, Rajapäivät}
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers


object FixtureUtil {
  def resetMockData(app: KoskiApplication, rajapäivät: MockRajapäivät = new MockRajapäivät()): Unit = synchronized {
    ValpasMockUsers.mockUsersEnabled = true
    Rajapäivät.enableMock(rajapäivät)
    app.fixtureCreator.resetFixtures(app.fixtureCreator.valpasFixtureState)
  }

  def clearMockData(app: KoskiApplication): Unit = synchronized {
    ValpasMockUsers.mockUsersEnabled = false
    Rajapäivät.disableMock()
    app.fixtureCreator.resetFixtures(app.fixtureCreator.koskiSpecificFixtureState)
  }
}
