package fi.oph.koski.tutkinto

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.Unauthenticated
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
    (koulutusTyyppi match {
      case Koulutustyyppi.aikuistenPerusopetus.koodiarvo => List("19/011/2015", "4/011/2004")
      case Koulutustyyppi.perusopetus.koodiarvo => List("104/011/2014", "1/011/2004")
      case _ => List()
    }).map(koodiarvo => koodistoViitePalvelu.validateRequired("koskikoulutustendiaarinumerot", koodiarvo))
  }
}
