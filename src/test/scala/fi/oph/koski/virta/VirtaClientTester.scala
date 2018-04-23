package fi.oph.koski.virta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.util.XML

// Client for the Virta Opintotietopalvelu, see https://confluence.csc.fi/display/VIRTA/VIRTA-opintotietopalvelu
object VirtaClientTester extends App {
  val hetulla: VirtaHakuehtoHetu = VirtaHakuehtoHetu("150113-4146")
  val oppijanumerolla = VirtaHakuehtoKansallinenOppijanumero("aed09afd87a8c6d76b76bbd")
  private val client: VirtaClient = VirtaClient(KoskiApplication.defaultConfig)

  client.opintotiedot(hetulla).foreach(result => println(XML.prettyPrint(result)))

  client.henkilötiedot(hetulla, "10076").foreach(result => println(XML.prettyPrint(result)))

}
