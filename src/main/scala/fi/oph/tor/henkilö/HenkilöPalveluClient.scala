package fi.oph.tor.henkilö

import com.typesafe.config.Config
import fi.oph.tor.http.VirkailijaHttpClient

object HenkilöPalveluClient {
  def apply(config: Config) = {
    new VirkailijaHttpClient(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("opintopolku.virkailija.url"), "/authentication-service")
  }
}
