package fi.oph.koski

import fi.oph.koski.http.HttpSpecification
import fi.oph.koski.log.Logging
import org.scalatest.{BeforeAndAfterAll, Suite}

// Palauttaa oletus-fixturet testin ajamisen jälkeen.
// Käytä testeissä, jotka muuttavat tietokantojen tilaa.
trait DirtiesFixtures extends HttpSpecification with BeforeAndAfterAll with Logging { this: Suite =>
  protected def alterFixture(): Unit = {}

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    alterFixture()
  }

  override protected def afterAll(): Unit = {
    logger.info("Resetting dirty fixtures")
    resetFixtures() // Siivoa fixturet takaisin oletustilaan
    super.afterAll()
  }
}
