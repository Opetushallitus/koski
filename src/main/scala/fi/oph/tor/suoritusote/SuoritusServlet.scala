package fi.oph.tor.suoritusote

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.tor.oppija.OppijaRepository
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

  get("/:oppijaOid/:opiskeluoikeusId") {
    val oid = params("oppijaOid")
    val opiskeluoikeusId = params("opiskeluoikeusId")
    implicit val user = torUser

    opiskeluOikeusRepository.findByOppijaOid(oid)(torUser).find(oo => oo.tyyppi.koodiarvo == "korkeakoulutus" && oo.lähdejärjestelmänId.exists(_.id == opiskeluoikeusId)).flatMap(oo => oppijaRepository.findByOid(oid).map((_, oo))) match {
      case Some((ht, oo)) => new OpintosuoritusoteHtml().render(ht, oo)
      case _ => TorErrorCategory.notFound
    }
  }

}
