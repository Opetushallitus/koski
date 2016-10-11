package fi.oph.koski.tutkinto

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{ApiServlet, Cached24Hours}

class TutkintoServlet(tutkintoRepository: TutkintoRepository) extends ApiServlet with Unauthenticated with Cached24Hours {
   get("/oppilaitos/:oppilaitosId") {
     contentType = "application/json;charset=utf-8"
     (params.get("query"), params.get("oppilaitosId")) match {
       case (Some(query), Some(oppilaitosId)) if (query.length >= 3) => tutkintoRepository.findTutkinnot(oppilaitosId, query)
       case _ => KoskiErrorCategory.badRequest.queryParam.searchTermTooShort()
     }
   }

  get("/rakenne/:diaariNumero") {
    contentType = "application/json;charset=utf-8"
    renderOption(KoskiErrorCategory.notFound.diaarinumeroaEiLöydy)(tutkintoRepository.findPerusteRakenne(params("diaariNumero")))
  }
}
