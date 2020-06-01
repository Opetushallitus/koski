package fi.oph.koski.suoritusote

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.schema._
import fi.oph.koski.servlet.HtmlServlet

import scala.xml.Elem

class SuoritusServlet(implicit val application: KoskiApplication) extends HtmlServlet with RequiresVirkailijaOrPalvelukäyttäjä {

  get("/:oppijaOid") {
    val oppijaOid = params("oppijaOid")
    implicit val localizations = application.localizationRepository

    renderEither[Elem](OpiskeluoikeusFinder(application.oppijaFacade).opiskeluoikeudet(oppijaOid, params.toMap).flatMap(_.warningsToLeft).right.flatMap {
      case Oppija(henkilö: TäydellisetHenkilötiedot, opiskeluoikeudet) => {
        val tyypit = opiskeluoikeudet.map(_.tyyppi.koodiarvo).toSet.toList
        tyypit match {
          case OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo :: Nil => Right(new OpintosuoritusoteHtml().korkeakoulu(henkilö, opiskeluoikeudet.asInstanceOf[List[KorkeakoulunOpiskeluoikeus]] ))
          case OpiskeluoikeudenTyyppi.lukiokoulutus.koodiarvo :: Nil => Right(new LukioOpintosuoritusoteHtml().lukio(henkilö, opiskeluoikeudet.asInstanceOf[List[LukionOpiskeluoikeus]]))
          case OpiskeluoikeudenTyyppi.ibtutkinto.koodiarvo :: Nil => Right(new IBOpintosuoritusoteHtml().ib(henkilö, opiskeluoikeudet.asInstanceOf[List[IBOpiskeluoikeus]]))
          case tyyppi :: Nil => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaOppilaitoksessaEiLöydy())
          case Nil => Left(KoskiErrorCategory.notFound.opiskeluoikeuttaOppilaitoksessaEiLöydy())
          case xs => throw new IllegalStateException(s"Samassa oppilaitoksessa useamman tyyppisiä opiskeluoikeuksia. Tyypit: ${tyypit}")
        }
      }
      case _ => throw new RuntimeException("Unreachable match arm: OpiskeluoikeusFinder must return exisiting Oppija on success")
    })
  }
}

