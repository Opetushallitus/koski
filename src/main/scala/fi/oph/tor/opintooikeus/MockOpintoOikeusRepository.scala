package fi.oph.tor.opintooikeus

import fi.oph.tor.oppija.{MockOppijaRepository, Oppija}
import fi.oph.tor.user.UserContext

class MockOpintoOikeusRepository extends OpintoOikeusRepository {
  private val oppijat = new MockOppijaRepository

  private def defaultOpintoOikeudet = List(
    OpintoOikeus(oppijaOid = oppijat.eero.oid, ePerusteetDiaarinumero = "1013059", oppilaitosOrganisaatio =  "1"),
    OpintoOikeus(oppijaOid = oppijat.eerola.oid, ePerusteetDiaarinumero = "1013059", oppilaitosOrganisaatio =  "1"),
    OpintoOikeus(oppijaOid = oppijat.teija.oid, ePerusteetDiaarinumero = "1013059", oppilaitosOrganisaatio =  "1"),
    OpintoOikeus(oppijaOid = oppijat.markkanen.oid, ePerusteetDiaarinumero = "1013059", oppilaitosOrganisaatio =  "3")
  )
  private var opintoOikeudet = defaultOpintoOikeudet

  override def findBy(oppija: Oppija)(implicit userContext: UserContext): List[OpintoOikeus] = opintoOikeudet.filter{ opintoOikeus =>
    opintoOikeus.oppijaOid == oppija.oid && userContext.hasReadAccess(opintoOikeus.oppilaitosOrganisaatio)
  }

  override def create(opintoOikeus: OpintoOikeus) = opintoOikeudet = opintoOikeudet :+ opintoOikeus

  override def filterOppijat(oppijat: List[Oppija])(implicit userContext: UserContext) = oppijat.filter(!findBy(_).isEmpty)

  override def resetMocks: Unit = {
    opintoOikeudet = defaultOpintoOikeudet
  }
}
