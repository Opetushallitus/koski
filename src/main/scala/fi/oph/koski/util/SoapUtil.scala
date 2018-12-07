package fi.oph.koski.util

import fi.oph.koski.http.{ErrorDetail, HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.luovutuspalvelu.SuomiFiResponse

import scala.util.control.NonFatal
import scala.xml.{Elem, Node, NodeSeq, PCData}
import scala.xml.transform.{RewriteRule, RuleTransformer}

object SoapUtil {
  def extractHetu(soap: Elem) =
    (soap \\ "Envelope" \\ "Body" \\ "suomiFiRekisteritiedot" \\ "hetu")
      .headOption.map(_.text.trim)
      .toRight(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Hetu puuttuu"))

  def readXml(body: String): Either[HttpStatus, Elem] = try {
    Right(scala.xml.XML.loadString(body))
  } catch {
    case NonFatal(e) =>
      Left(KoskiErrorCategory.badRequest.format.xml(e.getMessage))
  }

  def soapBody(soap: Elem, o: SuomiFiResponse): NodeSeq = {
    replaceSoapBody(soap,
      <ns1:suomiFiRekisteritiedotResponse xmlns:ns1="http://docs.koski-xroad.fi/producer">
        {PCData(JsonSerializer.writeWithRoot(o))}
      </ns1:suomiFiRekisteritiedotResponse>)
  }

  def soapError(status: HttpStatus): Elem = {
    assert(status.isError, "Yritettiin luoda SOAP virheviestiä ok-statuksella")
    <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
      <SOAP-ENV:Header />
      <SOAP-ENV:Body>
        <SOAP-ENV:Fault>
          <faultcode>SOAP-ENV:Client</faultcode>
          <faultstring>{status.errors.head.key}</faultstring>
          <detail>{status.errorString.getOrElse(status.errors.head.key)}</detail>
        </SOAP-ENV:Fault>
      </SOAP-ENV:Body>
    </SOAP-ENV:Envelope>
  }

  private def replaceSoapBody(envelope: NodeSeq, newBody: Node): NodeSeq = {
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
}
