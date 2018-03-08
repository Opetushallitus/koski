package fi.oph.koski.organisaatio

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{KäyttöoikeusOrg, RequiresAuthentication}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OrganisaatioServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  get("/hierarkia") {
    val query = params.get("query")
    val all = getBooleanParam("all")
    val filtered = if (all || koskiSession.hasGlobalReadAccess) {
      query match {
        case Some(query) if (query.length >= 3) =>
          application.organisaatioRepository.findHierarkia(query)
        case _ =>
          Nil
      }

    } else {
      val all: Set[OrganisaatioHierarkia] = koskiSession.orgKäyttöoikeudet.filter(_.juuri).flatMap{ko: KäyttöoikeusOrg => application.organisaatioRepository.getOrganisaatioHierarkia(ko.organisaatio.oid)}
      query match {
        case Some(query) =>
          OrganisaatioHierarkiaFilter(query, koskiSession.lang).filter(all)
        case None => all
      }
    }
    filtered.toList.sortBy(_.nimi.get(koskiSession.lang))
  }
}
