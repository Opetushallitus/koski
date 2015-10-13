package fi.oph.tor.oppija

import fi.oph.tor.json.Json
import fi.oph.tor.tutkinto.{Tutkinto, TutkintoRepository}
import fi.oph.tor.oppilaitos.{Oppilaitos, OppilaitosRepository}
import fi.oph.tor.security.RequiresAuthentication
import fi.oph.tor.opintooikeus.{OpintoOikeusRepository, OpintoOikeus}
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
    val result: OppijaCreationResult = oppijaRepository.findOrCreate(oppija)
    if (result.ok) {
      val oid = result.text
      opintoOikeusRepository.create(OpintoOikeus(oppija.opintoOikeus.tutkinto.ePerusteDiaarinumero, oid, oppija.opintoOikeus.oppilaitos.organisaatioId))
      oid
    } else {
      halt(result.httpStatus, result.text)
    }
  }

  private def userView(oid: String) = oppijaRepository.findById(oid) match {
    case Some(oppija) => Right(
      Map(
        "oid" -> oppija.oid,
        "sukunimi" -> oppija.sukunimi,
        "etunimet" -> oppija.etunimet,
        "hetu" -> oppija.hetu,
        "tutkinnot" -> tutkinnotForOppija(oppija)
      )
    )
    case None => Left(s"Oppija with oid: $oid not found")
  }

  private def tutkinnotForOppija(oppija: Oppija) = {
    for {
      opintoOikeus   <- opintoOikeusRepository.findBy(oppija)
      tutkinto   <- tutkintoRepository.findByEPerusteDiaarinumero(opintoOikeus.ePerusteetDiaarinumero)
      oppilaitos <- oppilaitosRepository.findById(opintoOikeus.oppilaitosOrganisaatio)
    } yield {
      Map(
        "nimi" -> tutkinto.nimi,
        "oppilaitos" -> Map(
          "nimi" -> oppilaitos.nimi
        )
      )
    }
  }
}

case class CreateOppijaAndOpintoOikeus(
                  etunimet: String, kutsumanimi: String, sukunimi: String, hetu: String,
                  opintoOikeus: CreateOpintoOikeus
                 ) extends CreateOppija

case class CreateOpintoOikeus(oppilaitos: Oppilaitos, tutkinto: Tutkinto)