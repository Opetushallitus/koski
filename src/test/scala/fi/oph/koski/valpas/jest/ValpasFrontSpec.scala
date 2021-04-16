package fi.oph.koski.valpas.jest

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.SharedJetty
import fi.oph.koski.mocha.KoskiCommandLineSpec
import fi.oph.koski.valpas.opiskeluoikeusfixture.FixtureUtil
import org.scalatest.Tag

class ValpasFrontSpec extends KoskiCommandLineSpec {
  private val raportointikantaService = KoskiApplicationForTests.raportointikantaService

  "Valpas front specs" taggedAs(ValpasFrontTag) in {
    SharedJetty.start

    FixtureUtil.resetMockData(KoskiApplicationForTests)
    raportointikantaService.loadRaportointikanta(force = true)

    runTestCommand("valpas-front", Seq("scripts/valpas-front-test.sh", SharedJetty.hostUrl))
  }
}

object ValpasFrontTag extends Tag("valpasfront")
