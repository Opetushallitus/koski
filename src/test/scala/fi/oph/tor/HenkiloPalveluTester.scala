package fi.oph.tor

import fi.oph.tor.config.TorApplication
import fi.oph.tor.henkilö.AuthenticationServiceClient

object HenkiloPalveluTester extends App {
  AuthenticationServiceClient(TorApplication().config).lisääKäyttöoikeusRyhmä(
    "1.2.246.562.24.38799436834",
    "1.2.246.562.10.93135224694",
    4056292
  )
}