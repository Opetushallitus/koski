package fi.oph.koski.ibis

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.koskiuser.Rooli.OPHPAAKAYTTAJA
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.servlet.FileUploadSupport

class IbisServlet(implicit val application: KoskiApplication)
  extends KoskiSpecificApiServlet
    with FileUploadSupport
    with RequiresVirkailijaOrPalvelukäyttäjä
    with NoCache
{
  // Tuonti hakee opiskeluoikeudet ilman käyttöoikeusrajausta.
  before() {
    if (!session.hasRole(OPHPAAKAYTTAJA)) {
      haltWithStatus(KoskiErrorCategory.forbidden("Sallittu vain OPH-pääkäyttäjälle"))
    }
  }

  post("/esikatselu") {
    renderEither(tiedosto.map(application.ibisService.esikatsele(_)(session)))
  }

  post("/tuonti") {
    renderEither(tiedosto.map(application.ibisService.tuo(_)(session)))
  }

  private def tiedosto: Either[HttpStatus, Array[Byte]] =
    fileParams.get("tiedosto").map(_.get()).toRight(KoskiErrorCategory.badRequest("Tiedosto puuttuu"))
}
