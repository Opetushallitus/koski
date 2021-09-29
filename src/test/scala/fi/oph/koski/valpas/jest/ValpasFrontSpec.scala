package fi.oph.koski.valpas.jest

import fi.oph.koski.SharedJetty
import fi.oph.koski.mocha.KoskiCommandLineSpec
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

class ValpasFrontSpec extends AnyFreeSpec with KoskiCommandLineSpec {

  "Valpas front specs" taggedAs(ValpasFrontTag) in {
    SharedJetty.start()
    runTestCommand("valpas-front", Seq("scripts/valpas-front-test.sh", SharedJetty.hostUrl))
  }
}

object ValpasFrontTag extends Tag("valpasfront")
