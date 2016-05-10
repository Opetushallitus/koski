package fi.oph.tor.virta

import fi.oph.tor.config.TorApplication

// Client for the Virta Opintotietopalvelu, see https://confluence.csc.fi/display/VIRTA/VIRTA-opintotietopalvelu
object VirtaClientTester extends App {
  val hetulla: VirtaHakuehtoHetu = VirtaHakuehtoHetu("090888-929X")
  val oppijanumerolla = VirtaHakuehtoKansallinenOppijanumero("aed09afd87a8c6d76b76bbd")
  private val client: VirtaClient = VirtaClient(TorApplication.defaultConfig)

  client.opintotiedot(hetulla).foreach(result => println(XML.prettyPrint(result)))

  client.henkilötiedot(hetulla, "10076").foreach(result => println(XML.prettyPrint(result)))

}
