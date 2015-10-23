package fi.oph.tor.opintooikeus

case class OpintoOikeus(ePerusteetDiaarinumero: String, oppilaitosOrganisaatio: String, suoritustapa: Option[String] = None, osaamisala: Option[String] = None, id: Option[Int] = None)

/**
 *  Primary key for OpintoOikeus
 */
case class OpintoOikeusIdentifier(oppijaOid: String, oppilaitosOrganisaatio: String, ePerusteetDiaarinumero: String)

object OpintoOikeusIdentifier {
  def apply(oppijaOid: String, opintoOikeus: OpintoOikeus): OpintoOikeusIdentifier = OpintoOikeusIdentifier(oppijaOid, opintoOikeus.oppilaitosOrganisaatio, opintoOikeus.ePerusteetDiaarinumero)
}

object OpintoOikeus {
  type Id = Int
}