package fi.oph.tor.koulutus

import com.typesafe.config.Config

trait KoulutusRepository {
  def etsiKoulutukset(oppilaitosId: String, query: String): List[Koulutus]
}

object KoulutusRepository {
  def apply(config: Config) = new MockKoulutusRepository
}

class MockKoulutusRepository extends KoulutusRepository {
  def koulutukset = List(
    Koulutus("Autoalan työnjohdon erikoisammattitutkinto", "1013059", "357305")
  )

  override def etsiKoulutukset(oppilaitosId: String, query: String) = {
    koulutukset.filter(_.toString.toLowerCase.contains(query.toLowerCase))
  }
}