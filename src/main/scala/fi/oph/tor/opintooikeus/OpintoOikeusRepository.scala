package fi.oph.tor.opintooikeus

import fi.oph.tor.http.HttpError
import fi.oph.tor.oppija.Oppija
import fi.oph.tor.user.UserContext

trait OpintoOikeusRepository {
  def filterOppijat(oppijat: List[Oppija])(implicit userContext: UserContext): List[Oppija]
  def findByOppijaOid(oid: String)(implicit userContext: UserContext): List[OpintoOikeus]
  def create(opintoOikeus: OpintoOikeus): Either[HttpError, OpintoOikeus.Id]
  def resetFixtures {}

  def findOrCreate(opintoOikeus: OpintoOikeus)(implicit userContext: UserContext): Either[HttpError, OpintoOikeus.Id] = {
    val opintoOikeudet: List[OpintoOikeus] = findByOppijaOid(opintoOikeus.oppijaOid)
    opintoOikeudet.find(_ == opintoOikeus) match {
      case Some(oikeus) => Right(0) // TODO: use actual id
      case _ => create(opintoOikeus)
    }
  }
}