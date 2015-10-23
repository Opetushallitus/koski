package fi.oph.tor.opintooikeus

case class OpintoOikeus(ePerusteetDiaarinumero: String, oppilaitosOrganisaatio: String, suoritustapa: Option[String] = None, osaamisala: Option[String] = None)

object OpintoOikeus {
  type Id = Int
}