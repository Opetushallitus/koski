package fi.oph.koski.valpas.jest

import fi.oph.koski.mocha.KoskiCommandLineSpec
import org.scalatest.Tag

class ValpasFrontSpec extends KoskiCommandLineSpec {
  "Valpas front specs" taggedAs(ValpasFrontTag) in {
    runTestCommand("valpas-front", Seq("scripts/valpas-front-test.sh"))
  }
}

object ValpasFrontTag extends Tag("valpasfront")
