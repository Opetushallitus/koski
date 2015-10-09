package fi.oph.tor.opintooikeus

import fi.oph.tor.oppija.{MockOppijaRepository, Oppija}

class MockOpintoOikeusRepository extends OpintoOikeusRepository {
  private val oppijat = new MockOppijaRepository

  private def defaultOpintoOikeudet = List(
    OpintoOikeus(oppijaOid = oppijat.eero.oid, ePerusteetDiaarinumero = "1013059", oppilaitosOrganisaatio =  "1"),
    OpintoOikeus(oppijaOid = oppijat.eerola.oid, ePerusteetDiaarinumero = "1013059", oppilaitosOrganisaatio =  "1"),
    OpintoOikeus(oppijaOid = oppijat.teija.oid, ePerusteetDiaarinumero = "1013059", oppilaitosOrganisaatio =  "1"),
    OpintoOikeus(oppijaOid = oppijat.markkanen.oid, ePerusteetDiaarinumero = "1013059", oppilaitosOrganisaatio =  "1")
  )
  private var opintoOikeudet = defaultOpintoOikeudet

  override def findBy(oppija: Oppija): List[OpintoOikeus] = opintoOikeudet.filter(_.oppijaOid == oppija.oid)

  override def create(opintoOikeus: OpintoOikeus) = opintoOikeudet = opintoOikeudet :+ opintoOikeus

  override def filterOppijat(oppijat: List[Oppija]) = oppijat.filter(!findBy(_).isEmpty)
}
