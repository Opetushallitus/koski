package fi.oph.koski.organisaatio

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AuthenticationSupport, KäyttöoikeusOrg}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OrganisaatioServlet(implicit val application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache {
  get("/hierarkia") {
    val query = params.get("query")
    val all = getBooleanParam("all")
    val lang = koskiSessionOption.map(_.lang).getOrElse("fi")

    val filtered = if (all || koskiSessionOption.isEmpty || koskiSessionOption.get.hasGlobalReadAccess) {
      query match {
        case Some(query) if (query.length >= 3) =>
          application.organisaatioRepository.findHierarkia(query)
        case _ =>
          Nil
      }
    } else {
      val all: Set[OrganisaatioHierarkia] = koskiSessionOption.get.orgKäyttöoikeudet.filter(_.juuri).flatMap{ko: KäyttöoikeusOrg => application.organisaatioRepository.getOrganisaatioHierarkia(ko.organisaatioOid)}
      query match {
        case Some(query) =>
          OrganisaatioHierarkiaFilter(query, lang).filter(all)
        case None => all
      }
    }
    filtered.toList.sortBy(_.nimi.get(lang))
  }

  get("/sahkoposti-virheiden-raportointiin") {
    renderEither[SähköpostiVirheidenRaportointiin](
      params.get("organisaatio")
        .toRight(KoskiErrorCategory.badRequest.queryParam.missing("Missing organisaatio"))
        .flatMap(OrganisaatioOid.validateOrganisaatioOid)
        .map(application.organisaatioRepository.findSähköpostiVirheidenRaportointiin)
        .flatMap(_.toRight(KoskiErrorCategory.notFound()))
    )
  }
}
