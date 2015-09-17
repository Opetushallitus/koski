package fi.oph.tor.oppija

import com.typesafe.config.Config
import org.http4s.Uri._

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
    Oppija("esimerkki", "eero", "010101-123N"),
    Oppija("tekijä", "teija", "150995-914X")
  )

  override def findOppijat(query: String) = {
    oppijat.filter(searchString(_).contains(query.toLowerCase))
  }

  private def searchString(oppija: Oppija) = {
    oppija.toString.toLowerCase
  }
}