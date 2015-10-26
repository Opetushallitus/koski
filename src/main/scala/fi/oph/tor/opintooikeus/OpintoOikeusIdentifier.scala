package fi.oph.tor.opintooikeus

object OpintoOikeusIdentifier {
  def apply(oppijaOid: String, opintoOikeus: OpintoOikeus): OpintoOikeusIdentifier = opintoOikeus.id match {
    case Some(id) => PrimaryKey(id)
    case _ => new IdentifyingSetOfFields(oppijaOid, opintoOikeus)
  }
}

case class IdentifyingSetOfFields(oppijaOid: String, oppilaitosOrganisaatio: String, ePerusteetDiaarinumero: String) extends OpintoOikeusIdentifier {
  def this(oppijaOid: String, opintoOikeus: OpintoOikeus) = this(oppijaOid, opintoOikeus.oppilaitosOrganisaatio.oid, opintoOikeus.ePerusteetDiaarinumero)
}
case class PrimaryKey(id: Int) extends OpintoOikeusIdentifier

sealed trait OpintoOikeusIdentifier