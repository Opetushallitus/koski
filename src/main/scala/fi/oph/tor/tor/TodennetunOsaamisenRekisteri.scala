package fi.oph.tor.tor

import fi.oph.tor.http.HttpError
import fi.oph.tor.opintooikeus.{OpintoOikeus, OpintoOikeusRepository}
import fi.oph.tor.oppija._
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.tutkinto.{TutkintoRakenne, RakenneOsa, TutkintoRepository}
import fi.oph.tor.user.UserContext

class TodennetunOsaamisenRekisteri(oppijaRepository: OppijaRepository,
                                   opintoOikeusRepository: OpintoOikeusRepository,
                                   tutkintoRepository: TutkintoRepository,
                                   oppilaitosRepository: OppilaitosRepository) {

  def findOppijat(query: String)(implicit userContext: UserContext): Seq[Oppija] = {
    val oppijat: List[Oppija] = oppijaRepository.findOppijat(query)
    val filtered = opintoOikeusRepository.filterOppijat(oppijat)
    filtered
  }

  def findOrCreate(oppija: CreateOppija)(implicit userContext: UserContext): Either[HttpError, Oppija.Id] = {
    if (oppija.opintoOikeudet.length == 0) {
      Left(HttpError(400, "At least one OpintoOikeus required"))
    }
    else {
      oppija.opintoOikeudet.flatMap(oikeus => validateOpintoOikeus(oikeus)).headOption match {
        case Some(error) => Left(error)
        case _ =>
          val result = oppijaRepository.findOrCreate(oppija)
          result.right.flatMap { oppijaOid: String =>
            val opintoOikeusCreationResults = oppija.opintoOikeudet.map { opintoOikeus =>
              opintoOikeusRepository.findOrCreate(oppijaOid, opintoOikeus)
            }
            opintoOikeusCreationResults.find(_.isLeft) match {
              case Some(Left(error)) => Left(error)
              case _ => Right(oppijaOid)
            }
          }
      }
    }
  }

  def validateOpintoOikeus(opintoOikeus: OpintoOikeus)(implicit userContext: UserContext) = {
    if(!userContext.hasReadAccess(opintoOikeus.oppilaitosOrganisaatio)) {
      Left(HttpError(403, "Forbidden"))
    }
    if(tutkintoRepository.findByEPerusteDiaarinumero(opintoOikeus.ePerusteetDiaarinumero).isEmpty) {
      Some(HttpError(400, "Invalid ePeruste: " + opintoOikeus.ePerusteetDiaarinumero))
    }
    else if(!userContext.hasReadAccess(opintoOikeus.oppilaitosOrganisaatio)) {
      Some(HttpError(403, "Forbidden"))
    } else {
      None
    }
  }

  def userView(oid: String)(implicit userContext: UserContext): Either[HttpError, TorOppijaView] = oppijaRepository.findByOid(oid) match {
    case Some(oppija) =>
      opintoOikeudetForOppija(oppija) match {
        case Nil => notFound(oid)
        case opintoOikeudet => Right(TorOppijaView(oppija.oid, oppija.sukunimi, oppija.etunimet, oppija.hetu, opintoOikeudet))
      }
    case None => notFound(oid)
  }

  def notFound(oid: String): Left[HttpError, Nothing] = {
    Left(HttpError(404, s"Oppija with oid: $oid not found"))
  }

  private def opintoOikeudetForOppija(oppija: Oppija)(implicit userContext: UserContext): Seq[TorOpintoOikeusView] = {
    for {
      opintoOikeus   <- opintoOikeusRepository.findByOppijaOid(oppija.oid)
      tutkinto   <- tutkintoRepository.findByEPerusteDiaarinumero(opintoOikeus.ePerusteetDiaarinumero)
      oppilaitos <- oppilaitosRepository.findById(opintoOikeus.oppilaitosOrganisaatio)
    } yield {
      TorOpintoOikeusView(tutkinto.ePerusteetDiaarinumero, oppilaitos.organisaatioId, tutkinto.nimi, TorOppilaitosView(oppilaitos.nimi), tutkintoRepository.findPerusteRakenne(tutkinto.ePerusteetDiaarinumero))
    }
  }
}


case class TorOppijaView(oid: String, sukunimi: String, etunimet: String, hetu: String, opintoOikeudet: Seq[TorOpintoOikeusView])

case class TorOpintoOikeusView(ePerusteetDiaarinumero: String, oppilaitosOrganisaatio: String, nimi: String, oppilaitos: TorOppilaitosView, rakenne: Option[TutkintoRakenne])

case class TorOppilaitosView(nimi: String)

case class CreateOppija(oid: Option[String], hetu: Option[String], etunimet: Option[String], kutsumanimi: Option[String], sukunimi: Option[String], opintoOikeudet: List[OpintoOikeus])