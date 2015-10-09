package fi.oph.tor.opintooikeus

import com.typesafe.config.Config
import fi.oph.tor.oppija.Oppija

trait OpintoOikeusRepository {
  def findBy(oppija: Oppija): List[OpintoOikeus]
  def create(opintoOikeus: OpintoOikeus)
}

object OpintoOikeusRepository {
  def apply(config: Config) = new MockOpintoOikeusRepository
}

class MockOpintoOikeusRepository extends OpintoOikeusRepository {
  private def defaultOpintoOikeudet = List(OpintoOikeus(oppijaOid = "1.2.246.562.24.00000000001", ePerusteetDiaarinumero = "1013059", oppilaitosOrganisaatio =  "1"))
  private var opintoOikeudet = defaultOpintoOikeudet

  override def findBy(oppija: Oppija): List[OpintoOikeus] = opintoOikeudet.filter(_.oppijaOid == oppija.oid)

  override def create(opintoOikeus: OpintoOikeus) = opintoOikeudet = opintoOikeudet :+ opintoOikeus
}
