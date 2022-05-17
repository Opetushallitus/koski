package fi.oph.koski.valpas

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.frontendvalvonta.FrontendValvontaMode.FrontendValvontaMode
import fi.oph.koski.frontendvalvonta.{FrontendValvontaMode, FrontendValvottuServlet}
import org.scalatra.ScalatraServlet

import scala.io.{Codec, Source}

class ValpasStaticServlet(implicit val application: KoskiApplication) extends ScalatraServlet with FrontendValvottuServlet {
  def allowFrameAncestors = false
  override def frontendValvontaMode: FrontendValvontaMode = FrontendValvontaMode(application.config.getString("frontend-valvonta.mode"))

  get("/*")(nonce => {
    contentType = "text/html"
    Source.fromInputStream(getServletContext.getResource("/koski/valpas/v2/index.html.template").openStream())(Codec.UTF8).mkString.replaceAll("<%==VALPAS_CSP_NONCE==%>", nonce)
  })

}
