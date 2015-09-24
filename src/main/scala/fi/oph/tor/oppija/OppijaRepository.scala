package fi.oph.tor.oppija

import com.typesafe.config.Config

object OppijaRepository {
  def apply(config: Config) = {
    if (config.hasPath("authentication-service")) {
      new AuthenticationServiceClient(config.getString("authentication-service.username"), config.getString("authentication-service.password"), config.getString("opintopolku.virkailija.url"))
    } else {
      new MockOppijaRepository
    }
  }
}

trait OppijaRepository {
  def findOppijat(query: String): List[Oppija]
}

class MockOppijaRepository extends OppijaRepository {
  val oppijat = List(
    Oppija("1.2.246.562.24.00000000001", "esimerkki", "eero", "010101-123N"),
    Oppija("1.2.246.562.24.00000000002", "eerola", "jouni", ""),
    Oppija("1.2.246.562.24.00000000003", "markkanen", "eero", ""),
    Oppija("1.2.246.562.24.00000000004", "tekijä", "teija", "150995-914X")
  )

  override def findOppijat(query: String) = {
    oppijat.filter(searchString(_).contains(query.toLowerCase))
  }

  private def searchString(oppija: Oppija) = {
    oppija.toString.toLowerCase
  }
}