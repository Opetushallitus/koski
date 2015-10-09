package fi.oph.tor.oppija

import fi.oph.tor.json.Json
import fi.oph.tor.koulutus.{Koulutus, KoulutusRepository}
import fi.oph.tor.oppilaitos.{Oppilaitos, OppilaitosRepository}
import fi.oph.tor.security.Authenticated
import fi.oph.tor.tutkinto.{Tutkinto, TutkintoRepository}
import fi.oph.tor.{ErrorHandlingServlet, InvalidRequestException}
import fi.vm.sade.utils.slf4j.Logging

class OppijaServlet(oppijaRepository: OppijaRepository,
                    tutkintoRepository: TutkintoRepository,
                    koulutusRepository: KoulutusRepository,
                    oppilaitosRepository: OppilaitosRepository) extends ErrorHandlingServlet with Logging with Authenticated {

  get("/") {
    contentType = "application/json;charset=utf-8"
    params.get("query") match {
      case Some(query) if (query.length >= 3) => Json.write(oppijaRepository.findOppijat(query))
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
    val oppija: CreateOppijaAndKoulutus = Json.read[CreateOppijaAndKoulutus](request.body)
    val result: OppijaCreationResult = oppijaRepository.create(oppija)
    result match {
      case Created(oid) =>
        tutkintoRepository.create(Tutkinto(oppija.koulutus.tutkinto.ePeruste, oid, oppija.koulutus.oppilaitos.organisaatioId))
        oid
      case Failed(status, text) =>
        halt(status, text)
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
      tutkinto   <- tutkintoRepository.findBy(oppija)
      koulutus   <- koulutusRepository.findById(tutkinto.peruste)
      oppilaitos <- oppilaitosRepository.findById(tutkinto.oppilaitos)
    } yield {
      Map(
        "nimi" -> koulutus.nimi,
        "oppilaitos" -> Map(
          "nimi" -> oppilaitos.nimi
        )
      )
    }
  }
}

case class CreateOppijaAndKoulutus(
                  etunimet: String, kutsumanimi: String, sukunimi: String, hetu: String,
                  koulutus: CreateKoulutus
                 ) extends CreateOppija

case class CreateKoulutus(oppilaitos: Oppilaitos, tutkinto: Koulutus)