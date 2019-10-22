package fi.oph.koski.omattiedot

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.{HtmlServlet, NoCache}

class HuoltajaServlet(implicit val application: KoskiApplication) extends HtmlServlet with RequiresKansalainen with NoCache {
  // suomifi.valtuudet.redirectBackUrl points here, could be changed to /omattiedot but requires a whitelist change to vrk's valtuutuspalvelu
  get("/") {
    params.get("code") match {
      case Some(code) => redirect(s"/omattiedot?code=$code")
      case _ => redirect("/omattiedot")
    }
  }

  get("/valitse") {
    application.huoltajaService.getValtuudetUrl(koskiRoot) match {
      case Right(url) => redirect(url)
      case Left(status) => redirect("/omattiedot?code=errorCreatingSession")
    }
  }
}
