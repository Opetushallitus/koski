package fi.oph.tor.suoritusote

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.schema.Opiskeluoikeus
import fi.oph.tor.servlet.ErrorHandlingServlet
import fi.oph.tor.tor.TodennetunOsaamisenRekisteri
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient

class SuoritusServlet(
  val userRepository: UserOrganisationsRepository,
  val directoryClient: DirectoryClient,
  val rekisteri: TodennetunOsaamisenRekisteri,
  val oppijaRepository: OppijaRepository,
  val opiskeluOikeusRepository: OpiskeluOikeusRepository) extends ErrorHandlingServlet with RequiresAuthentication {

  get("/:oppijaOid/:oppilaitosOid") {
    val oid = params("oppijaOid")
    val oppilaitosOid = params("oppilaitosOid")
    implicit val user = torUser
    oppijaRepository.findByOid(oid) match {
      case Some(henkilötiedot) =>
        val opiskeluoikeudet = opiskeluOikeusRepository.findByOppijaOid(oid)(torUser)
          .filter(_.oppilaitos.oid == oppilaitosOid).toList

        new OpintosuoritusoteHtml().render(henkilötiedot, opiskeluoikeudet)

      case None => renderStatus(TorErrorCategory.notFound.oppijaaEiLöydy())
    }
  }
}
