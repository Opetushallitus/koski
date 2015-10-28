package fi.oph.tor.tutkinto

import fi.oph.tor.arvosana.ArviointiasteikkoRepository
import fi.oph.tor.eperusteet.{EPerusteRakenne, EPerusteetTutkintoRakenne}
import fi.oph.tor.json.Json
import fi.oph.tor.json.Json._

class MockTutkintoRepository extends TutkintoRepository {
  def tutkinnot = List(
    Tutkinto("39/011/2014", "351301", Some("Autoalan perustutkinto"))
  )

  override def findTutkinnot(oppilaitosId: String, query: String) = {
    tutkinnot.filter(_.toString.toLowerCase.contains(query.toLowerCase))
  }

  override def findByEPerusteDiaarinumero(diaariNumero: String) = tutkinnot.filter(_.ePerusteetDiaarinumero == diaariNumero).headOption

  override def findPerusteRakenne(diaariNumero: String)(implicit arviointiAsteikot: ArviointiasteikkoRepository) = {
    findByEPerusteDiaarinumero(diaariNumero).map{peruste =>
      val rakenne = Json.readFile("src/main/resources/mockdata/eperusteet/612.json").extract[EPerusteRakenne]
      EPerusteetTutkintoRakenne.convertRakenne(rakenne)
    }
  }
}
