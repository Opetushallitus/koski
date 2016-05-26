package fi.oph.koski.suoritusote

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koski.KoskiFacade
import fi.oph.koski.koskiuser.{RequiresAuthentication, UserOrganisationsRepository}
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.schema._
import fi.oph.koski.servlet.HtmlServlet
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
        val oppilaitoksenOpiskeluoikeudet: List[Opiskeluoikeus] = opiskeluoikeudet.filter(_.oppilaitos.oid == oppilaitosOid).toList
        val tyypit = oppilaitoksenOpiskeluoikeudet.map(_.tyyppi.koodiarvo).toSet.toList
        tyypit match {
          case "korkeakoulutus" :: Nil => new OpintosuoritusoteHtml().korkeakoulu(henkilö, oppilaitoksenOpiskeluoikeudet.asInstanceOf[List[KorkeakoulunOpiskeluoikeus]] )
          case "lukiokoulutus" :: Nil => new OpintosuoritusoteHtml().lukio(henkilö, oppilaitoksenOpiskeluoikeudet.asInstanceOf[List[LukionOpiskeluoikeus]])
          case tyyppi :: Nil => KoskiErrorCategory.notFound.opiskeluoikeuttaOppilaitoksessaEiLöydy()
          case Nil => KoskiErrorCategory.notFound.opiskeluoikeuttaOppilaitoksessaEiLöydy()
          case xs => throw new IllegalStateException(s"Samassa oppilaitoksessa useamman tyyppisiä opiskeluoikeuksia. Oppija:${henkilö.oid} oppilaitos:${oppilaitosOid} tyypit: ${tyypit}")
        }
      case _ => KoskiErrorCategory.notFound.oppijaaEiLöydy()
    }
  }
}
