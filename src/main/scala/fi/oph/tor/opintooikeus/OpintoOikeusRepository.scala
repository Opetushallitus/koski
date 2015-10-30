package fi.oph.tor.opintooikeus

import fi.oph.tor.http.HttpStatus
import fi.oph.tor.oppija.{PossiblyUnverifiedOppijaOid, Oppija}
import fi.oph.tor.user.UserContext

trait OpintoOikeusRepository {
  def filterOppijat(oppijat: Seq[Oppija])(implicit userContext: UserContext): Seq[Oppija]
  def findByOppijaOid(oid: String)(implicit userContext: UserContext): Seq[OpintoOikeus]
  def find(identifier: OpintoOikeusIdentifier)(implicit userContext: UserContext): Option[OpintoOikeus]
  def create(oppijaOid: String, opintoOikeus: OpintoOikeus): Either[HttpStatus, OpintoOikeus.Id]
  def resetFixtures {}
  def update(oppijaOid: String, opintoOikeus: OpintoOikeus): HttpStatus

  def createOrUpdate(oppijaOid: PossiblyUnverifiedOppijaOid, opintoOikeus: OpintoOikeus)(implicit userContext: UserContext): Either[HttpStatus, OpintoOikeus.Id] = {
    val opintoOikeudet: Option[OpintoOikeus] = find(OpintoOikeusIdentifier(oppijaOid.oppijaOid, opintoOikeus))
    opintoOikeudet match {
      case Some(oikeus) => update(oppijaOid.oppijaOid, opintoOikeus.copy(id = oikeus.id)) match {
        case error if error.isError => Left(error)
        case _ => Right(oikeus.id.get)
      }
      case _ =>
        oppijaOid.verifiedOid match {
          case Some(oid) => create(oid, opintoOikeus)
          case None => Left(HttpStatus.notFound("Oppija " + oppijaOid.oppijaOid + " not found"))
        }
    }
  }
}