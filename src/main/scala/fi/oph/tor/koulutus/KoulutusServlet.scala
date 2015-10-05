package fi.oph.tor.koulutus

import fi.oph.tor.json.Json
import fi.oph.tor.{ErrorHandlingServlet, InvalidRequestException}

class KoulutusServlet(koulutusRepository: KoulutusRepository) extends ErrorHandlingServlet {
   get("/oppilaitos/:oppilaitosId") {
     contentType = "application/json;charset=utf-8"
     (params.get("query"), params.get("oppilaitosId")) match {
       case (Some(query), Some(oppilaitosId)) if (query.length >= 3) => Json.write(koulutusRepository.etsiKoulutukset(oppilaitosId, query))
       case _ => throw new InvalidRequestException("query parameter length must be at least 3")
     }
   }
 }
