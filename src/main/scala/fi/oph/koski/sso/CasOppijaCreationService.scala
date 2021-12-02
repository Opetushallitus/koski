package fi.oph.koski.sso

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schema.{Nimitiedot, UusiHenkilö}
import org.scalatra.servlet.ServletApiImplicits.enrichRequest

import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletRequest

class CasOppijaCreationService(application: KoskiApplication) {
  def findOrCreate(request: HttpServletRequest, validHetu: String) =
    application.henkilöRepository
      .findByHetuOrCreateIfInYtrOrVirta(validHetu, nimitiedot(request))
      .orElse(create(request, validHetu))

  def create(request: HttpServletRequest, validHetu: String) =
    nimitiedot(request)
      .map(toUusiHenkilö(validHetu, _))
      .map(application.henkilöRepository
        .findOrCreate(_)
        .left.map(s => new RuntimeException(s.errorString.mkString))
        .toTry.get
      )

  def nimitiedot(request: HttpServletRequest): Option[Nimitiedot] =
    for {
      etunimet <- utf8Header(request, "FirstName")
      kutsumanimi <- utf8Header(request, "givenName")
      sukunimi <- utf8Header(request, "sn")
    } yield Nimitiedot(etunimet = etunimet, kutsumanimi = kutsumanimi, sukunimi = sukunimi)

  private def toUusiHenkilö(validHetu: String, nimitiedot: Nimitiedot) = UusiHenkilö(
    hetu = validHetu,
    etunimet = nimitiedot.etunimet,
    kutsumanimi = Some(nimitiedot.kutsumanimi),
    sukunimi = nimitiedot.sukunimi
  )

  private def utf8Header(request: HttpServletRequest, headerName: String): Option[String] =
    request.header(headerName)
      .map(header => new String(header.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8))
      .map(_.trim)
      .filter(_.nonEmpty)
}
