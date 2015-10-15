package fi.oph.tor.oppija

import java.io

import fi.oph.tor.json.Json
import fi.oph.tor.opintooikeus.{OpintoOikeus, OpintoOikeusRepository}
import fi.oph.tor.oppilaitos.OppilaitosRepository
import fi.oph.tor.security.RequiresAuthentication
import fi.oph.tor.tutkinto.TutkintoRepository
import fi.oph.tor.user.UserRepository
import fi.oph.tor.{ErrorHandlingServlet, InvalidRequestException}
import fi.vm.sade.utils.slf4j.Logging

class OppijaServlet(oppijaRepository: OppijaRepository,
                    opintoOikeusRepository: OpintoOikeusRepository,
                    tutkintoRepository: TutkintoRepository,
                    oppilaitosRepository: OppilaitosRepository)(implicit val userRepository: UserRepository) extends ErrorHandlingServlet with Logging with RequiresAuthentication {

  get("/") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) =>
        val oppijat: List[Oppija] = oppijaRepository.findOppijat(query)
        val filtered = opintoOikeusRepository.filterOppijat(oppijat)
        Json.write(filtered)
      case _ => throw new InvalidRequestException("query parameter length must be at least 3")
    }
  }

  get("/:oid") {
    contentType = "application/json;charset=utf-8"

    userView(params("oid")) match {
      case Right(user) => Json.write(user)
      case Left(msg) => halt(404, msg)
    }
  }

  post("/") {
    contentType = "text/plain;charset=utf-8"
    val oppija: CreateOppijaAndOpintoOikeus = Json.read[CreateOppijaAndOpintoOikeus](request.body)
    if(!userContext.hasReadAccess(oppija.opintoOikeus.organisaatioId)) {
      halt(403, "Forbidden")
    }
    val result: CreationResult = oppijaRepository.findOrCreate(oppija)
    if (result.ok) {
      val oid = result.text
      opintoOikeusRepository.findOrCreate(OpintoOikeus(oppija.opintoOikeus.ePerusteDiaarinumero, oid, oppija.opintoOikeus.organisaatioId))
      oid
    } else {
      halt(result.httpStatus, result.text)
    }
  }

  private def userView(oid: String) = oppijaRepository.findById(oid) match {
    case Some(oppija) => Right(
      TorOppijaView(oppija.oid, oppija.sukunimi, oppija.etunimet, oppija.hetu, opintoOikeudetForOppija(oppija))
    )
    case None => Left(s"Oppija with oid: $oid not found")
  }

  private def opintoOikeudetForOppija(oppija: Oppija) = {
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