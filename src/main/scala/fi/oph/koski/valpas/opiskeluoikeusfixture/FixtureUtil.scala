package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.valpas.opiskeluoikeusrepository.{MockValpasRajapäivät, ValpasRajapäivät}
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers


object FixtureUtil {
  def resetMockData(app: KoskiApplication, rajapäivät: MockValpasRajapäivät = new MockValpasRajapäivät()): FixtureState = synchronized {
    ValpasMockUsers.mockUsersEnabled = true
    ValpasRajapäivät.enableMock(rajapäivät)
    app.fixtureCreator.resetFixtures(app.fixtureCreator.valpasFixtureState)
    FixtureState(app)
  }

  def clearMockData(app: KoskiApplication): FixtureState = synchronized {
    ValpasMockUsers.mockUsersEnabled = false
    ValpasRajapäivät.disableMock()
    app.fixtureCreator.resetFixtures(app.fixtureCreator.koskiSpecificFixtureState)
    FixtureState(app)
  }
}

object FixtureState {
  def apply(application: KoskiApplication): FixtureState = FixtureState(
    fixture = application.fixtureCreator.getCurrentFixtureStateName(),
    rajapäivät = ValpasRajapäivät.getCurrent()
  )
}

case class FixtureState(
  fixture: String,
  rajapäivät: ValpasRajapäivät
)
