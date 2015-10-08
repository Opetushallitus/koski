package fi.oph.tor.koulutus

import com.typesafe.config.Config

trait KoulutusRepository {
  def findKoulutukset(oppilaitosId: String, query: String): List[Koulutus]
  def findById(id: String): Option[Koulutus]
}

object KoulutusRepository {
  def apply(config: Config) = new MockKoulutusRepository
}

class MockKoulutusRepository extends KoulutusRepository {
  def koulutukset = List(
    Koulutus("Autoalan työnjohdon erikoisammattitutkinto", ePeruste =  "1013059", koulutusKoodi =  "357305")
  )

  override def findKoulutukset(oppilaitosId: String, query: String) = {
    koulutukset.filter(_.toString.toLowerCase.contains(query.toLowerCase))
  }

  override def findById(id: String) = koulutukset.filter(_.ePeruste == id).headOption
}