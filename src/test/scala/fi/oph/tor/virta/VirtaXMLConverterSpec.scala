package fi.oph.tor.virta

import fi.oph.tor.config.TorApplication
import fi.oph.tor.toruser.MockUsers
import org.scalatest.{Matchers, FreeSpec}

class VirtaXMLConverterSpec extends FreeSpec with Matchers {
  implicit val user = MockUsers.kalle.asTorUser
  "Keskeneräinen tutkinto" in {
    TorApplication().virta.findByHetu("010675-9981").flatMap(_.suoritukset).map(_.tila.koodiarvo) should equal(List("KESKEN"))
  }
  "Valmis tutkinto" in {
    TorApplication().virta.findByHetu("290492-9455").flatMap(_.suoritukset).map(_.tila.koodiarvo) should equal(List("VALMIS"))
  }
}
