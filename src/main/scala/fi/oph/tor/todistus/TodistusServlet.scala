package fi.oph.tor.todistus

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.schema._
import fi.oph.tor.servlet.ErrorHandlingServlet
import fi.oph.tor.tor.TodennetunOsaamisenRekisteri
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient

class TodistusServlet(val userRepository: UserOrganisationsRepository, val directoryClient: DirectoryClient, rekisteri: TodennetunOsaamisenRekisteri) extends ErrorHandlingServlet with RequiresAuthentication {
  get("/opiskeluoikeus/:opiskeluoikeusId") {
    val opiskeluoikeusId = getIntegerParam("opiskeluoikeusId")
    rekisteri.findOpiskeluOikeus(opiskeluoikeusId)(torUser) match {
      case Right((henkilötiedot, opiskeluoikeus)) =>
          opiskeluoikeus.suoritukset.head match {
            case t: PeruskoulunPäättötodistus if t.tila.koodiarvo == "VALMIS" =>
              PeruskoulunPaattotodistusHtml.renderPeruskoulunPäättötodistus(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)
            case t: AmmatillisenTutkinnonSuoritus if t.tila.koodiarvo == "VALMIS" => // TODO: vain perustutkinnot
              AmmatillisenPerustutkinnonPaattotodistusHtml.renderAmmatillisenPerustutkinnonPaattotodistus(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)
            case t: LukionOppimääränSuoritus if t.tila.koodiarvo == "VALMIS" =>
              LukionPaattotodistusHtml.renderLukionPäättötodistus(opiskeluoikeus.koulutustoimija, opiskeluoikeus.oppilaitos, henkilötiedot, t)
            case _ => TorErrorCategory.notFound.todistustaEiLöydy()
          }
      case Left(status) => renderStatus(status)
    }
  }
}