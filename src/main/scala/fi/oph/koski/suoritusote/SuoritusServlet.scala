package fi.oph.koski.suoritusote

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.schema.{Oppija, TaydellisetHenkilötiedot}
import fi.oph.koski.servlet.HtmlServlet
import fi.oph.koski.koski.KoskiFacade
import fi.oph.koski.koskiuser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.vm.sade.security.ldap.DirectoryClient

class SuoritusServlet(
  val userRepository: UserOrganisationsRepository,
  val directoryClient: DirectoryClient,
  val rekisteri: KoskiFacade,
  val oppijaRepository: OppijaRepository,
  val koski: KoskiFacade) extends HtmlServlet with RequiresAuthentication {

  get("/:oppijaOid/:oppilaitosOid") {
    val oid = params("oppijaOid")
    val oppilaitosOid = params("oppilaitosOid")
    implicit val user = koskiUser

    koski.findOppija(oid) match {
      case Right(Oppija(henkilö: TaydellisetHenkilötiedot, opiskeluoikeudet)) =>
        new OpintosuoritusoteHtml().render(henkilö, opiskeluoikeudet.filter(_.oppilaitos.oid == oppilaitosOid).toList)
      case _ => KoskiErrorCategory.notFound.oppijaaEiLöydy()
    }
  }
}
