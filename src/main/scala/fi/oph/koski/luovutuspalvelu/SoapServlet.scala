package fi.oph.koski.luovutuspalvelu

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.servlet.ApiServlet

import scala.util.control.NonFatal
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, Node, NodeSeq}

trait SoapServlet extends ApiServlet {
  def writeXml(elem: NodeSeq): Unit = {
    contentType = "text/xml"
    response.writer.print(elem)
  }

  def xmlBody: Either[HttpStatus, Elem] = try {
    Right(scala.xml.XML.loadString(request.body))
  } catch {
    case NonFatal(e) =>
      Left(KoskiErrorCategory.badRequest.format.xml(e.getMessage))
  }

  def soapError(status: HttpStatus): Elem = {
    assert(status.isError, "Yritettiin luoda SOAP virheviestiä ok-statuksella")
    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
      <SOAP-ENV:Header />
      <SOAP-ENV:Body>
        <SOAP-ENV:Fault>
          <faultcode>SOAP-ENV:Server</faultcode>
          <faultstring>{status.errors.head.key}</faultstring>
          <detail><message>{status.errorString.getOrElse(status.errors.head.key)}</message></detail>
        </SOAP-ENV:Fault>
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>
  }

  def replaceSoapBody(envelope: NodeSeq, newBody: Node): NodeSeq = {
    val SoapEnvelopeNamespace = "http://schemas.xmlsoap.org/soap/envelope/"
    val requestToResponse = new RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case e@Elem(prefix, "Body", attribs, scope, _, _*) if e.namespace == SoapEnvelopeNamespace =>
          Elem(prefix, "Body", attribs, scope, false, newBody)
        case other => other
      }
    }
    new RuleTransformer(requestToResponse).transform(envelope)
  }

  override def renderStatus(status: HttpStatus): Unit = {
    response.setStatus(500)
    writeXml(soapError(status))
  }
}
