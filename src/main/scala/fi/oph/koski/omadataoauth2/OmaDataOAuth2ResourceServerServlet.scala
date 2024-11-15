package fi.oph.koski.omadataoauth2

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonResources
import fi.oph.koski.koskiuser.RequiresOmaDataOAuth2
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import org.scalatra.ContentEncodingSupport


class OmaDataOAuth2ResourceServerServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet
  with Logging with ContentEncodingSupport with NoCache with RequiresOmaDataOAuth2 {

  private val dummyResourceFilename = "/omadataoauth2/dummydata.json"

  // in: access token
  // out: data, jos käyttäjällä oikeudet kyseiseen access tokeniin.
  //      TAI OAuth2-protokollan mukainen virheilmoitus (joka luotetaan nginx:n välittävän sellaisenaan, jos pyyntö on tänne asti tullut?)
  post("/") {
    // TODO: TOR-2210 pitäisikö tarkistaa muita headereitä kuin Bearer?
    val result = request.header("X-Auth").map(_.split(" ")) match {
      case Some(Array("Bearer", token)) if token == "dummy-access-token" =>
        // TODO:  oikea toteutus + testit
        Right(JsonResources.readResource(dummyResourceFilename))
      case _ =>
        // TODO: TOR-2210 pitäisikö virheestä kertoa detaljeita, esim. oliko vaan expired token tms.?
        // TODO: TOR-2210 Speksin mukainen virhesisältö, jos sellainen on resource serverille määritelty
        Left(KoskiErrorCategory.badRequest())
    }
    renderEither(result)
  }
}

case class DummyResourceResponse(
  data: String
)
