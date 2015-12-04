package fi.oph.tor

import com.typesafe.config.Config
import fi.oph.tor.config.TorApplication
import fi.oph.tor.henkilö.HenkilöPalveluClient

object HenkiloPalveluTester extends App {
  new HenkiloPalveluTester(TorApplication().config).lisääKäyttöoikeusRyhmä(
    "1.2.246.562.24.38799436834",
    "1.2.246.562.10.93135224694",
    4056292
  )
}

class HenkiloPalveluTester(config: Config) {
  val client = HenkilöPalveluClient(config)

  def lisääKäyttöoikeusRyhmä(henkilö: String, org: String, ryhmä: Int): Unit = {
    client.httpClient.put(client.virkailijaUriFromString("/authentication-service/resources/henkilo/" + henkilö + "/organisaatiohenkilo/" + org + "/kayttooikeusryhmat"), List(LisääKäyttöoikeusRyhmä(ryhmä)))
  }
}

case class LisääKäyttöoikeusRyhmä(ryhmaId: Int, alkuPvm: String = "2015-12-04T11:08:13.042Z", voimassaPvm: String = "2024-12-02T01:00:00.000Z", selected: Boolean = true)