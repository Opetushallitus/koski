package fi.oph.koski.organisaatio

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiSpecificAuthenticationSupport, RequiresSession}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class OrganisaatioServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with NoCache with RequiresSession {
  private val organisaatioService = new OrganisaatioService(application)

  get("/hierarkia") {
    val filtered = if (showAll) {
      organisaatioService.searchInAllOrganizations(query)
    } else {
      organisaatioService.searchInEntitledOrganizations(query, orgTypesToShow)
    }
    filtered.map(_.sortBy(session.lang))
  }

  get("/sahkoposti-virheiden-raportointiin") {
    renderEither[SähköpostiVirheidenRaportointiin]({
      val oid = params.get("organisaatio").toRight(KoskiErrorCategory.badRequest.queryParam.missing("Missing organisaatio"))
      val specialCase = oid.flatMap {
        application.organisaatioRepository
          .findSähköpostiVirheidenRaportointiinSpecialCase(_, application.koskiLocalizationRepository)
          .toRight(KoskiErrorCategory.notFound())
      }
      if (specialCase.isRight) specialCase else {
          oid
            .flatMap(OrganisaatioOid.validateOrganisaatioOid)
            .map(application.organisaatioRepository.findSähköpostiVirheidenRaportointiin)
            .flatMap(_.toRight(KoskiErrorCategory.notFound()))
      }
    })
  }

  private def query = params.get("query")
  private def showAll = getOptionalBooleanParam("all").getOrElse(false) || koskiSessionOption.forall(_.hasGlobalReadAccess)
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
