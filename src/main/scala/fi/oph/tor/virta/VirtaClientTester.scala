package fi.oph.tor.virta

import fi.oph.tor.config.TorApplication

// Client for the Virta Opintotietopalvelu, see https://confluence.csc.fi/display/VIRTA/VIRTA-opintotietopalvelu
object VirtaClientTester extends App {
  val hetulla: VirtaHakuehtoHetu = VirtaHakuehtoHetu("290492-9455")
  val oppijanumerolla = VirtaHakuehtoKansallinenOppijanumero("aed09afd87a8c6d76b76bbd")
  val result = VirtaClient(TorApplication.defaultConfig).fetchVirtaData(hetulla)
  println(XML.prettyPrint(result.get))
}
