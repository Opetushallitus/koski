package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresOmaDataOAuth2
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport


class OmaDataOAuth2ResourceServerServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet
                                                                          with Logging with ContentEncodingSupport with NoCache with RequiresOmaDataOAuth2 {

  // in: access token
  // out: data, jos käyttäjällä oikeudet kyseiseen access tokeniin.
  //      TAI OAuth2-protokollan mukainen virheilmoitus (joka luotetaan nginx:n välittävän sellaisenaan, jos pyyntö on tänne asti tullut?)
  post("/") {
    // TODO: pitäisikö tarkistaa muita headereitä kuin Bearer?
    //
    val result = request.header("X-Auth").map(_.split(" ")) match {
      case Some(Array("Bearer", token)) if token == "dummy-access-token" =>
        // TODO: oikea toteutus + testit
        Right(DummyResourceResponse("todo"))
      case _ =>
        // TODO: pitäisikö virheestä kertoa detaljeita, esim. oliko vaan expired token tms.?
        Left(KoskiErrorCategory.badRequest())
    }
    renderEither(result)
  }
}

case class DummyResourceResponse(
  data: String
)
