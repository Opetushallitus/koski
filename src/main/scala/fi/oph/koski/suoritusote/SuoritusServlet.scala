package fi.oph.koski.suoritusote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresAuthentication
import fi.oph.koski.schema._
import fi.oph.koski.servlet.HtmlServlet

class SuoritusServlet(val application: KoskiApplication) extends HtmlServlet with RequiresAuthentication {

  get("/:oppijaOid") {
    val oppijaOid = params("oppijaOid")
    implicit val user = koskiSession
    implicit val localizations = application.localizationRepository

    renderEither(OpiskeluoikeusFinder(application.oppijaFacade).opiskeluoikeudet(oppijaOid, params).right.flatMap {
      case Oppija(henkilö: TäydellisetHenkilötiedot, opiskeluoikeudet) => {
        val tyypit = opiskeluoikeudet.map(_.tyyppi.koodiarvo).toSet.toList
        tyypit match {
          case "korkeakoulutus" :: Nil => Right(new OpintosuoritusoteHtml().korkeakoulu(henkilö, opiskeluoikeudet.asInstanceOf[List[KorkeakoulunOpiskeluoikeus]] ))
          case "lukiokoulutus" :: Nil => Right(new OpintosuoritusoteHtml().lukio(henkilö, opiskeluoikeudet.asInstanceOf[List[LukionOpiskeluoikeus]]))
          case "ibtutkinto" :: Nil => Right(new IBOpintosuoritusoteHtml().ib(henkilö, opiskeluoikeudet.asInstanceOf[List[IBOpiskeluoikeus]]))
          case tyyppi :: Nil => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaOppilaitoksessaEiLöydy())
          case Nil => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaOppilaitoksessaEiLöydy())
          case xs => throw new IllegalStateException(s"Samassa oppilaitoksessa useamman tyyppisiä opiskeluoikeuksia. Tyypit: ${tyypit}")
        }
      }
      case _ => throw new RuntimeException("Unreachable match arm: OpiskeluoikeusFinder must return exisiting Oppija on success")
    })
  }
}

