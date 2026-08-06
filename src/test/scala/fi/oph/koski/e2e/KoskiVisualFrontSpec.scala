package fi.oph.koski.e2e

import fi.oph.koski.{KoskiApplicationForTests, SharedJetty}
import fi.oph.koski.mocha.KoskiCommandLineSpec
import org.scalatest.Tag
import org.scalatest.freespec.AnyFreeSpec

/**
 * Visuaaliset regressiotestit (screenshot-vertailu) omana suitenaan.
 *
 * Ajetaan erillään KoskiFrontSpecistä, koska ne ajetaan Linux-kontissa:
 * kuvakaappausten on synnyttävä samassa ympäristössä riippumatta siitä, kenen
 * koneella ne otetaan. Backend pyörii ajurilla ja vain selain on kontissa.
 *
 * Ks. documentation/visual-testing.md
 */
class KoskiVisualFrontSpec extends AnyFreeSpec with KoskiCommandLineSpec {
  "Koski visual regression tests" taggedAs(KoskiVisualFrontTag) in {
    val sharedJetty = new SharedJetty(KoskiApplicationForTests)
    sharedJetty.start()
    runTestCommand("koski-visual-tests", Seq(
      "scripts/koski-visual.sh",
      "test",
      sharedJetty.hostUrl
    ))
  }
}

object KoskiVisualFrontTag extends Tag("integrationtest")
