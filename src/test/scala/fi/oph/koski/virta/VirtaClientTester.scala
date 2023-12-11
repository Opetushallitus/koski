package fi.oph.koski.virta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.util.XML

// Client for the Virta Opintotietopalvelu, see https://wiki.eduuni.fi/display/CSCVIRTA/VIRTA-opintotietopalvelu
object VirtaClientTester extends App {
  val hetulla: VirtaHakuehtoHetu = VirtaHakuehtoHetu("150113-4146")
  val oppijanumerolla = VirtaHakuehtoKansallinenOppijanumero("aed09afd87a8c6d76b76bbd")
  private val client: VirtaClient = VirtaClient(new Hetu(KoskiApplication.defaultConfig.getBoolean("acceptSyntheticHetus")), KoskiApplication.defaultConfig)

  client.opintotiedot(hetulla).foreach(result => println(XML.prettyPrint(result)))

  client.henkilötiedot(hetulla, "10076").foreach(result => println(XML.prettyPrint(result)))

}
