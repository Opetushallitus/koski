package fi.oph.koski.suoritusote

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koski.KoskiFacade
import fi.oph.koski.koskiuser.{RequiresAuthentication, KäyttöoikeusRepository}
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.schema._
import fi.oph.koski.servlet.HtmlServlet
import fi.vm.sade.security.ldap.DirectoryClient

class SuoritusServlet(
                       val käyttöoikeudet: KäyttöoikeusRepository,
                       val directoryClient: DirectoryClient,
                       val rekisteri: KoskiFacade,
                       val oppijaRepository: OppijaRepository,
                       val koski: KoskiFacade) extends HtmlServlet with RequiresAuthentication {

  get("/:oppijaOid") {
    val oppijaOid = params("oppijaOid")
    implicit val user = koskiUser

    renderEither(OpiskeluoikeusFinder(koski).opiskeluoikeudet(oppijaOid, params).right.flatMap { case Oppija(henkilö: TäydellisetHenkilötiedot, opiskeluoikeudet) =>
      val tyypit = opiskeluoikeudet.map(_.tyyppi.koodiarvo).toSet.toList
      tyypit match {
        case "korkeakoulutus" :: Nil => Right(new OpintosuoritusoteHtml().korkeakoulu(henkilö, opiskeluoikeudet.asInstanceOf[List[KorkeakoulunOpiskeluoikeus]] ))
        case "lukiokoulutus" :: Nil => Right(new OpintosuoritusoteHtml().lukio(henkilö, opiskeluoikeudet.asInstanceOf[List[LukionOpiskeluoikeus]]))
        case tyyppi :: Nil => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaOppilaitoksessaEiLöydy())
        case Nil => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaOppilaitoksessaEiLöydy())
        case xs => throw new IllegalStateException(s"Samassa oppilaitoksessa useamman tyyppisiä opiskeluoikeuksia. Tyypit: ${tyypit}")
      }
    })
  }
}

