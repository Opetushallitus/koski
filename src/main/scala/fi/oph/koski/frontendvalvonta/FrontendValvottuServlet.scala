package fi.oph.koski.frontendvalvonta

import fi.oph.koski.frontendvalvonta.FrontendValvontaMode.FrontendValvontaMode
import fi.oph.koski.util.Cryptographic
import org.scalatra.ScalatraBase

trait FrontendValvottuServlet extends ScalatraBase {

  def allowFrameAncestors: Boolean
  def allowFrameSrcSelf: Boolean = false
  def frontendValvontaMode: FrontendValvontaMode
  def unsafeAllowInlineStyles: Boolean = false
  def unsafeAllowBaseUri: Boolean = false

  def get(transformers: org.scalatra.RouteTransformer*)(action: String => scala.Any): org.scalatra.Route = {
    super.get(transformers: _*) {
      val nonce = setNonceHeader
      action(nonce)
    }
  }

  protected def setNonceHeader: String = {
    val nonce = Cryptographic.nonce
    FrontendValvontaHeaders.headers(allowFrameAncestors, allowFrameSrcSelf, frontendValvontaMode, unsafeAllowInlineStyles, unsafeAllowBaseUri, nonce).foreach {
      case (h, v) => response.setHeader(h, v)
    }
    nonce
  }
}
