package fi.oph.koski.util

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.luovutuspalvelu.SuomiFiResponse

import scala.util.control.NonFatal
import scala.xml.{Elem, Node, NodeSeq, PCData}
import scala.xml.transform.{RewriteRule, RuleTransformer}

trait SoapUtil {
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

  def soapBody(soap: Elem, o: SuomiFiResponse) = {
    replaceSoapBody(soap,
      <ns1:suomiFiRekisteritiedotResponse xmlns:ns1="http://docs.koski-xroad.fi/producer">
        {PCData(JsonSerializer.writeWithRoot(o))}
      </ns1:suomiFiRekisteritiedotResponse>)
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
