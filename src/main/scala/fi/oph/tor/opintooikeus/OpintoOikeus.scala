package fi.oph.tor.opintooikeus

case class OpintoOikeus(ePerusteetDiaarinumero: String, oppilaitosOrganisaatio: String, suoritustapa: Option[String] = None, osaamisala: Option[String] = None, id: Option[Int] = None)

sealed trait OpintoOikeusIdentifier

case class IdentifyingSetOfFields(oppijaOid: String, oppilaitosOrganisaatio: String, ePerusteetDiaarinumero: String) extends OpintoOikeusIdentifier {
  def this(oppijaOid: String, opintoOikeus: OpintoOikeus) = this(oppijaOid, opintoOikeus.oppilaitosOrganisaatio, opintoOikeus.ePerusteetDiaarinumero)
}
case class PrimaryKey(id: Int) extends OpintoOikeusIdentifier

object OpintoOikeusIdentifier {
  def apply(oppijaOid: String, opintoOikeus: OpintoOikeus): OpintoOikeusIdentifier = opintoOikeus.id match {
    case Some(id) => PrimaryKey(id)
    case _ => new IdentifyingSetOfFields(oppijaOid, opintoOikeus)
  }
}

object OpintoOikeus {
  type Id = Int
}