package fi.oph.koski.valpas.jest

import fi.oph.koski.SharedJetty
import fi.oph.koski.mocha.KoskiCommandLineSpec
import org.scalatest.Tag

class ValpasFrontSpec extends KoskiCommandLineSpec {

  "Valpas front specs" taggedAs(ValpasFrontTag) in {
    SharedJetty.start
    runTestCommand("valpas-front", Seq("scripts/valpas-front-test.sh", SharedJetty.hostUrl))
  }
}

object ValpasFrontTag extends Tag("valpasfront")
