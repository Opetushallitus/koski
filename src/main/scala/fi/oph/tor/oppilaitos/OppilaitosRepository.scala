package fi.oph.tor.oppilaitos

import com.typesafe.config.Config
import fi.oph.tor.oppilaitos.Oppilaitos

trait OppilaitosRepository {
  def findOppilaitokset(query: String): List[Oppilaitos]
}

object OppilaitosRepository {
  def apply(config: Config) = new MockOppilaitosRepository
}

class MockOppilaitosRepository extends OppilaitosRepository {
  private def oppilaitokset = List(
    Oppilaitos("1", "Helsingin Ammattioppilaitos"),
    Oppilaitos("2", "Metropolia Helsinki"),
    Oppilaitos("3", "Omnia Helsinki")
  )
  override def findOppilaitokset(query: String) = oppilaitokset.filter(_.toString.toLowerCase.contains(query.toLowerCase))
}
