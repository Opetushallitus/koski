package fi.oph.tor.suoritusote

import fi.oph.tor.http.TorErrorCategory
import fi.oph.tor.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.tor.oppija.OppijaRepository
import fi.oph.tor.schema.{Oppija, TaydellisetHenkilötiedot}
import fi.oph.tor.servlet.HtmlServlet
import fi.oph.tor.tor.TodennetunOsaamisenRekisteri
import fi.oph.tor.toruser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient

class SuoritusServlet(
  val userRepository: UserOrganisationsRepository,
  val directoryClient: DirectoryClient,
  val rekisteri: TodennetunOsaamisenRekisteri,
  val oppijaRepository: OppijaRepository,
  val todennetunOsaamisenRekisteri: TodennetunOsaamisenRekisteri) extends HtmlServlet with RequiresAuthentication {

  get("/:oppijaOid/:oppilaitosOid") {
    val oid = params("oppijaOid")
    val oppilaitosOid = params("oppilaitosOid")
    implicit val user = torUser

    todennetunOsaamisenRekisteri.findTorOppija(oid) match {
      case Right(Oppija(henkilö: TaydellisetHenkilötiedot, opiskeluoikeudet)) =>
        new OpintosuoritusoteHtml().render(henkilö, opiskeluoikeudet.filter(_.oppilaitos.oid == oppilaitosOid).toList)
      case _ => TorErrorCategory.notFound.oppijaaEiLöydy()
    }
  }
}
