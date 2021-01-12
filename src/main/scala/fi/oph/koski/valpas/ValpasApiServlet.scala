package fi.oph.koski.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.scalatra.CorsSupport

// TODO: Unauthenticated pois, CORS-tukea pitänee kuristaa
class ValpasApiServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with Unauthenticated with CorsSupport {
  options("/*"){
    response.setHeader(
      "Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
  }

  get("/hello") {
    "Hello world!"
  }
}


