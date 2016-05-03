package fi.oph.tor.virta

import fi.oph.tor.config.TorApplication
import fi.oph.tor.schema.UusiHenkilö
import fi.oph.tor.toruser.MockUsers
import org.scalatest.{Matchers, FreeSpec}

class VirtaXMLConverterSpec extends FreeSpec with Matchers {
  implicit val user = MockUsers.kalle.asTorUser
  "Keskeneräinen tutkinto" in {
    TorApplication().virta.findByHenkilö(UusiHenkilö("010675-9981", "etu", "etu", "suku")).flatMap(_.suoritukset).map(_.tila.koodiarvo) should equal(List("KESKEN"))
  }
  "Valmis tutkinto" in {
    TorApplication().virta.findByHenkilö(UusiHenkilö("290492-9455", "etu", "etu", "suku")).flatMap(_.suoritukset).map(_.tila.koodiarvo) should equal(List("VALMIS"))
  }
}
