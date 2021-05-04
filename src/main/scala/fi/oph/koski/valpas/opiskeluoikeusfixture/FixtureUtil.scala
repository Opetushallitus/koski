package fi.oph.koski.valpas.opiskeluoikeusfixture

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.valpas.db.ValpasDatabaseFixtureLoader
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers


object FixtureUtil {
  val DefaultTarkastelupäivä: LocalDate = date(2021, 9, 5)

  def resetMockData(app: KoskiApplication, tarkastelupäivä: LocalDate = DefaultTarkastelupäivä): FixtureState = synchronized {
    ValpasMockUsers.mockUsersEnabled = true
    app.valpasRajapäivätService.asInstanceOf[MockValpasRajapäivätService].asetaMockTarkastelupäivä(tarkastelupäivä)
    app.fixtureCreator.resetFixtures(app.fixtureCreator.valpasFixtureState)
    new ValpasDatabaseFixtureLoader(app.valpasKuntailmoitusQueryService).reset()
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
)
