package fi.oph.koski.organisaatio

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AuthenticationSupport, KoskiSession, KäyttöoikeusOrg}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OrganisaatioServlet(implicit val application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache {
  get("/hierarkia") {
    val query = params.get("query")
    val all = getBooleanParam("all")

    val filtered = if (all || koskiSessionOption.isEmpty || koskiSessionOption.get.hasGlobalReadAccess) {
      query match {
        case Some(query) if (query.length >= 3) =>
          application.organisaatioRepository.findHierarkia(query).sortBy(organisaatioNimi)
        case _ =>
          Nil
      }
    } else {
      val omatOrganisaatiot = organisaatiot
      val orgs = (if (orgTypesToShow.contains(vainVarhaiskasvatusToimipisteet)) Nil else omatOrganisaatiot) ++ hierarkianUlkopuolisetOrganisaatiot(omatOrganisaatiot.map(_.oid))

      query match {
        case Some(query) =>
          OrganisaatioHierarkiaFilter(query, lang).filter(orgs)
        case None => orgs
      }
    }
    filtered.map(_.sortBy(lang))
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

  private def orgTypesToShow = params.get("orgTypesToShow")
  private def lang = koskiSessionOption.map(_.lang).getOrElse("fi")
  private def organisaatioNimi: OrganisaatioHierarkia => String = _.nimi.get(lang)

  private def organisaatiot =
    koskiSessionOption.get.orgKäyttöoikeudet.filter(_.juuri).toList.flatMap { ko: KäyttöoikeusOrg =>
      application.organisaatioRepository.getOrganisaatioHierarkia(ko.organisaatio.oid)
    }.sortBy(organisaatioNimi)

  private def hierarkianUlkopuolisetOrganisaatiot(omatOrganisaatiot: List[String]) = {
    val hierarkianUlkopuolisetOrganisaatiot = if (koskiSessionOption.get.hasKoulutustoimijaVarhaiskasvatuksenJärjestäjäAccess && orgTypesToShow.forall(_ == vainVarhaiskasvatusToimipisteet)) {
      application.organisaatioRepository.findVarhaiskasvatusHierarkiat
    } else {
      Nil
    }
    hierarkianUlkopuolisetOrganisaatiot.filterNot(o => omatOrganisaatiot.contains(o.oid)).sortBy(organisaatioNimi)
  }

  private val vainVarhaiskasvatusToimipisteet = "vainVarhaiskasvatusToimipisteet"
}
