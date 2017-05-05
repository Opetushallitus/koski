package fi.oph.koski.tutkinto

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.schema.Koodistokoodiviite
import fi.oph.koski.servlet.{ApiServlet, Cached24Hours}

class TutkinnonPerusteetServlet(tutkintoRepository: TutkintoRepository, koodistoViitePalvelu: KoodistoViitePalvelu) extends ApiServlet with Unauthenticated with Cached24Hours {
  get("/oppilaitos/:oppilaitosId") {
   contentType = "application/json;charset=utf-8"
   (params.get("query"), params.get("oppilaitosId")) match {
     case (Some(query), Some(oppilaitosId)) if (query.length >= 3) => tutkintoRepository.findTutkinnot(oppilaitosId, query)
     case _ => KoskiErrorCategory.badRequest.queryParam.searchTermTooShort()
   }
  }

  get("/diaarinumerot/koulutustyyppi/:koulutustyyppi") {
    val koulutusTyyppi = params("koulutustyyppi")
    koodistoViitePalvelu.getSisältyvätKoodiViitteet(koodistoViitePalvelu.getLatestVersion("koskikoulutustendiaarinumerot").get, Koodistokoodiviite(koulutusTyyppi, "koulutustyyppi"))
  }
}
