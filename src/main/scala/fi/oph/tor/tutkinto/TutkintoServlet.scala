package fi.oph.tor.tutkinto

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.json.Json
import fi.oph.tor.servlet.{InvalidRequestException, ErrorHandlingServlet}

class TutkintoServlet(tutkintoRepository: TutkintoRepository) extends ErrorHandlingServlet {
   get("/oppilaitos/:oppilaitosId") {
     contentType = "application/json;charset=utf-8"
     (params.get("query"), params.get("oppilaitosId")) match {
       case (Some(query), Some(oppilaitosId)) if (query.length >= 3) => Json.write(tutkintoRepository.findTutkinnot(oppilaitosId, query))
       case _ => throw new InvalidRequestException(TorErrorCategory.badRequest.queryParam.searchTermTooShort)
     }
   }

  get("/rakenne/:diaariNumero") {
    contentType = "application/json;charset=utf-8"
    renderOption(TorErrorCategory.notFound.diaarinumeroaEiLöydy)(tutkintoRepository.findPerusteRakenne(params("diaariNumero")))
  }
}
