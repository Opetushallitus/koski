package fi.oph.koski.organisaatio

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.common.koskiuser.{AuthenticationSupport, RequiresSession}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class OrganisaatioServlet(implicit val application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache with RequiresSession {
  private val organisaatioService = new OrganisaatioService(application)

  get("/hierarkia") {
    val filtered = if (showAll) {
      organisaatioService.searchInAllOrganizations(query)
    } else {
      organisaatioService.searchInEntitledOrganizations(query, orgTypesToShow)
    }
    filtered.map(_.sortBy(koskiSession.lang))
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

  private def query = params.get("query")
  private def showAll = getBooleanParam("all") || koskiSessionOption.forall(_.hasGlobalReadAccess)
  private def orgTypesToShow = params.get("orgTypesToShow") match {
    case None => Kaikki
    case Some(param) if param == "vainVarhaiskasvatusToimipisteet" => VarhaiskasvatusToimipisteet
    case _ => OmatOrganisaatiot
  }
}

sealed trait OrgTypesToShow
case object Kaikki extends OrgTypesToShow
case object VarhaiskasvatusToimipisteet extends OrgTypesToShow
case object OmatOrganisaatiot extends OrgTypesToShow
