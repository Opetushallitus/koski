package fi.oph.koski.servlet

import fi.oph.koski.html.ContentSecurityPolicy
import fi.oph.koski.util.Cryptographic
import org.scalatra.ScalatraBase

trait ContentSecurityPolicyServlet extends ScalatraBase {

  def allowFrameAncestors: Boolean

  def get(transformers: org.scalatra.RouteTransformer*)(action: String => scala.Any): org.scalatra.Route = {
    super.get(transformers:_*) {
      val nonce = setNonceHeader
      action(nonce)
    }
  }

  protected def setNonceHeader: String = {
    val nonce = Cryptographic.nonce
    ContentSecurityPolicy.headers(allowFrameAncestors, nonce).foreach {
      case (h, v) => response.setHeader(h, v)
    }
    nonce
  }
}
