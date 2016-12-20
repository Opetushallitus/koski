package fi.oph.koski.organisaatio

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{AccessType, KäyttöoikeusOrg, RequiresAuthentication}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OrganisaatioServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication with NoCache {
  get("/hierarkia") {
    val query = params.get("query")
    val filtered = if (koskiSession.isRoot) {
      query match {
        case Some(query) =>
          application.organisaatioRepository.findHierarkia(query)
        case None =>
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
