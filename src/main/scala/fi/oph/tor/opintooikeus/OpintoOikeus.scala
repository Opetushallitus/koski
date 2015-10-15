package fi.oph.tor.opintooikeus

case class OpintoOikeus(ePerusteetDiaarinumero: String, oppijaOid: String, oppilaitosOrganisaatio: String)

object OpintoOikeus {
  type Id = Int
}
