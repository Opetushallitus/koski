package fi.oph.tor.opintooikeus

import fi.oph.tor.http.HttpError
import fi.oph.tor.oppija.Oppija
import fi.oph.tor.user.UserContext

trait OpintoOikeusRepository {
  def filterOppijat(oppijat: Seq[Oppija])(implicit userContext: UserContext): Seq[Oppija]
  def findByOppijaOid(oid: String)(implicit userContext: UserContext): Seq[OpintoOikeus]
  def find(identifier: OpintoOikeusIdentifier)(implicit userContext: UserContext): Option[OpintoOikeus]
  def create(oppijaOid: String, opintoOikeus: OpintoOikeus): Either[HttpError, OpintoOikeus.Id]
  def resetFixtures {}
  def update(oppijaOid: String, opintoOikeus: OpintoOikeus): Option[HttpError]

  def createOrUpdate(oppijaOid: String, opintoOikeus: OpintoOikeus)(implicit userContext: UserContext): Either[HttpError, OpintoOikeus.Id] = {
    val opintoOikeudet: Option[OpintoOikeus] = find(OpintoOikeusIdentifier(oppijaOid, opintoOikeus))
    opintoOikeudet match {
      case Some(oikeus) => update(oppijaOid, opintoOikeus.copy(id = oikeus.id)) match {
        case Some(error) => Left(error)
        case _ => Right(oikeus.id.get)
      }
      case _ => create(oppijaOid, opintoOikeus)
    }
  }
}