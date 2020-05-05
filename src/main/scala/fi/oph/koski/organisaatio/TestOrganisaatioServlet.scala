package fi.oph.koski.organisaatio

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.{AuthenticationSupport, KoskiSession}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class TestOrganisaatioServlet(implicit val application: KoskiApplication) extends ApiServlet with AuthenticationSupport with NoCache {
  private val organisaatioService = new OrganisaatioService(application)

  get("/newfetch") {
    renderObject[List[OrganisaatioHierarkia]](organisaatioService.organisaatioRepository.getOrganisaatioHierarkiaIncludingParents(params("oid")))
  }

  override def koskiSessionOption: Option[KoskiSession] = None
}
