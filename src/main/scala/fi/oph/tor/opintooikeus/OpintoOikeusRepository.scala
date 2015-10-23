package fi.oph.tor.opintooikeus

import fi.oph.tor.http.HttpError
import fi.oph.tor.oppija.Oppija
import fi.oph.tor.user.UserContext

trait OpintoOikeusRepository {
  def filterOppijat(oppijat: Seq[Oppija])(implicit userContext: UserContext): Seq[Oppija]
  def findByOppijaOid(oid: String)(implicit userContext: UserContext): Seq[OpintoOikeus]
  def create(oppijaOid: String, opintoOikeus: OpintoOikeus): Either[HttpError, OpintoOikeus.Id]
  def resetFixtures {}

  def findOrCreate(oppijaOid: String, opintoOikeus: OpintoOikeus)(implicit userContext: UserContext): Either[HttpError, OpintoOikeus.Id] = {
    val opintoOikeudet: Seq[OpintoOikeus] = findByOppijaOid(oppijaOid)
    opintoOikeudet.find(_ == opintoOikeus) match {
      case Some(oikeus) => Right(0) // TODO: use actual id
      case _ => create(oppijaOid, opintoOikeus)
    }
  }
}