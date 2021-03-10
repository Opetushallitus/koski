package fi.oph.koski.valpas.jest

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.SharedJetty
import fi.oph.koski.mocha.KoskiCommandLineSpec
import fi.oph.koski.valpas.repository.MockRajapäivät
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers
import org.scalatest.Tag

class ValpasFrontSpec extends KoskiCommandLineSpec {
  val fixtureCreator = KoskiApplicationForTests.fixtureCreator
  val raportointikantaService = KoskiApplicationForTests.raportointikantaService

  "Valpas front specs" taggedAs(ValpasFrontTag) in {
    SharedJetty.start

    ValpasMockUsers.mockUsersEnabled = true
    fixtureCreator.resetFixtures(fixtureCreator.valpasFixtureState)
    MockRajapäivät.mockRajapäivät = MockRajapäivät()
    raportointikantaService.loadRaportointikanta(force = true)

    runTestCommand("valpas-front", Seq("scripts/valpas-front-test.sh", SharedJetty.hostUrl))
  }
}

object ValpasFrontTag extends Tag("valpasfront")
