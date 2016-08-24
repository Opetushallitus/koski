package fi.oph.koski.tiedonsiirto

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.servlet.ApiServlet

class TiedonsiirtoServlet(val application: KoskiApplication) extends ApiServlet with RequiresAuthentication {
  get() {
    koskiUser.juuriOrganisaatio.toList.flatMap(application.tiedonsiirtoRepository.findByOrganisaatio)
  }
}
