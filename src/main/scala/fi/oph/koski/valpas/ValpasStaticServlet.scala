package fi.oph.koski.valpas

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode.FrontendValvontaMode
import fi.oph.koski.frontendvalvonta.{FrontendValvontaMode, FrontendValvottuServlet}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.HtmlServlet
import org.scalatra.ScalatraServlet

import scala.io.{Codec, Source}

class ValpasStaticServlet(implicit val application: KoskiApplication) extends ScalatraServlet with Logging with FrontendValvottuServlet {
  def allowFrameAncestors = false
  override def frontendValvontaMode: FrontendValvontaMode = FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/*")(nonce => {
    contentType = "text/html"
    try {
      Source.fromInputStream(getServletContext.getResource("/valpas/index.html.template").openStream())(Codec.UTF8).mkString.replaceAll("<%==VALPAS_CSP_NONCE==%>", nonce)
    } catch {
      case e: Exception =>
        logger.error(e)("ValpasStaticServlet failure: " + e.getMessage)
        halt(500, "Internal server error")
    }
  })

}
