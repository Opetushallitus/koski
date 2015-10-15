package fi.oph.tor.tor

import fi.oph.tor.http.HttpError
import fi.oph.tor.opintooikeus.{OpintoOikeus, OpintoOikeusRepository}
import fi.oph.tor.oppija._
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.tutkinto.TutkintoRepository
import fi.oph.tor.user.UserContext

class TodennetunOsaamisenRekisteri(oppijaRepository: OppijaRepository,
                                   opintoOikeusRepository: OpintoOikeusRepository,
                                   tutkintoRepository: TutkintoRepository,
                                   oppilaitosRepository: OppilaitosRepository) {

  def findOppijat(query: String)(implicit userContext: UserContext): List[Oppija] = {
    val oppijat: List[Oppija] = oppijaRepository.findOppijat(query)
    val filtered = opintoOikeusRepository.filterOppijat(oppijat)
    filtered
  }

  def findOrCreate(oppija: CreateOppijaAndOpintoOikeus)(implicit userContext: UserContext): Either[HttpError, Oppija.Id] = {
    if(!userContext.hasReadAccess(oppija.opintoOikeus.organisaatioId)) {
      Left(HttpError(403, "Forbidden"))
    } else {
      val result = oppijaRepository.findOrCreate(oppija)
      result.right.flatMap { oid: String =>
        opintoOikeusRepository
          .findOrCreate(OpintoOikeus(oppija.opintoOikeus.ePerusteDiaarinumero, oid, oppija.opintoOikeus.organisaatioId))
          .right
          .map { id: Int => oid }
      }
    }
  }

  // TODO: check access
  def userView(oid: String)(implicit userContext: UserContext): Either[String, TorOppijaView] = oppijaRepository.findById(oid) match {
    case Some(oppija) => Right(
      TorOppijaView(oppija.oid, oppija.sukunimi, oppija.etunimet, oppija.hetu, opintoOikeudetForOppija(oppija))
    )
    case None => Left(s"Oppija with oid: $oid not found")
  }

  private def opintoOikeudetForOppija(oppija: Oppija)(implicit userContext: UserContext) = {
    for {
      opintoOikeus   <- opintoOikeusRepository.findByOppijaOid(oppija.oid)
      tutkinto   <- tutkintoRepository.findByEPerusteDiaarinumero(opintoOikeus.ePerusteetDiaarinumero)
      oppilaitos <- oppilaitosRepository.findById(opintoOikeus.oppilaitosOrganisaatio)
    } yield {
      TorOpintoOikeusView(tutkinto.nimi, TorOppilaitosView(oppilaitos.nimi))
    }
  }
}


case class TorOppijaView(oid: String, sukunimi: String, etunimet: String, hetu: String, opintoOikeudet: List[TorOpintoOikeusView])

case class TorOpintoOikeusView(nimi: String, oppilaitos: TorOppilaitosView)

case class TorOppilaitosView(nimi: String)

case class CreateOppijaAndOpintoOikeus(
                                        etunimet: String, kutsumanimi: String, sukunimi: String, hetu: String,
                                        opintoOikeus: CreateOpintoOikeus
                                        ) extends CreateOppija

case class CreateOpintoOikeus(ePerusteDiaarinumero: String, organisaatioId: String)