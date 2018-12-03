package fi.oph.koski.luovutuspalvelu

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.{HenkilöOid, Hetu}
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresLuovutuspalvelu
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus}
import fi.oph.koski.servlet.{ApiServlet, NoCache, ObservableSupport}
import fi.oph.koski.util.{Timing, XML}
import org.json4s.JValue
import org.json4s.JsonAST.{JBool, JObject}
import org.scalatra.ContentEncodingSupport

import scala.util.control.NonFatal
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, Node, NodeSeq, PCData}

class LuovutuspalveluServlet(implicit val application: KoskiApplication) extends ApiServlet with ObservableSupport with RequiresLuovutuspalvelu with ContentEncodingSupport with NoCache with Timing {
  before() {
    if (!application.features.luovutuspalvelu) {
      haltWithStatus(KoskiErrorCategory.badRequest("Luovutuspalvelu-rajapinta ei käytössä tässä ympäristössä."))
    }
  }

  get("/healthcheck") {
    renderObject(JObject("ok" -> JBool(true)))
  }

  post("/oid") {
    withJsonBody { parsedJson =>
      renderEither(parseOidRequestV1(parsedJson).flatMap(application.luovutuspalveluService.findOppijaByOid))
    }()
  }

  post("/hetu") {
    withJsonBody { parsedJson =>
      renderEither(parseHetuRequestV1(parsedJson).flatMap(application.luovutuspalveluService.findOppijaByHetu))
    }()
  }

  post("/hetut") {
    withJsonBody { parsedJson =>
      parseBulkHetuRequestV1(parsedJson) match {
        case Right(req) =>
          streamResponse[JValue](application.luovutuspalveluService.queryOppijatByHetu(req), koskiSession)
        case Left(status) =>
          haltWithStatus(status)
      }
    }()
  }

  post("/suomi-fi-rekisteritiedot") {
    (for {
      xml <- readXml
      hetu <- extractHetu(xml)
      opiskeluoikeudet <- application.suomiFiService.suomiFiOpiskeluoikeudet(hetu)
    } yield soapBody(xml, opiskeluoikeudet)) match {
      case Right(soap)=>
        contentType = "text/xml"
        response.writer.print(XML.prettyPrintNodes(soap))
      case _ => KoskiErrorCategory.internalError
    }
  }

  private def extractHetu(soap: Elem) =
    (soap \\ "Envelope" \\ "Body" \\ "suomiFiRekisteritiedot" \\ "hetu")
      .headOption.map(_.text.trim)
      .toRight(KoskiErrorCategory.badRequest.validation.henkilötiedot.hetu("Hetu puuttuu"))

  private def soapBody(soap: Elem, o: SuomiFiResponse) = {
    replaceSoapBody(soap, <ns1:suomiFiRekisteritiedotResponse xmlns:ns1="http://docs.koski-xroad.fi/producer">
      <ns1:opiskeluoikeudet>
        {PCData(JsonSerializer.writeWithRoot(o))}
      </ns1:opiskeluoikeudet>
    </ns1:suomiFiRekisteritiedotResponse>)
  }

  private def readXml: Either[HttpStatus, Elem] = try {
    Right(scala.xml.XML.loadString(request.body))
  } catch {
    case NonFatal(e) =>
      Left(KoskiErrorCategory.badRequest.format.xml(e.getMessage))
  }

  private def replaceSoapBody(envelope: NodeSeq, newBody: Node): NodeSeq = {
    val SoapEnvelopeNamespace = "http://schemas.xmlsoap.org/soap/envelope/"
    val requestToResponse = new RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {
        case e@Elem(prefix, "Body", attribs, scope, _, _*) if e.namespace == SoapEnvelopeNamespace =>
          Elem(prefix, "Body", attribs, scope, false, newBody)
        case other =>
          other
      }
    }
    new RuleTransformer(requestToResponse).transform(envelope)
  }

  private def parseOidRequestV1(parsedJson: JValue): Either[HttpStatus, OidRequestV1] = {
    JsonSerializer.validateAndExtract[OidRequestV1](parsedJson)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .filterOrElse(_.v == 1, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      .flatMap(req => HenkilöOid.validateHenkilöOid(req.oid).map(_ => req))
      .flatMap(validateOpiskeluoikeudenTyypit(_, allowVirtaOrYtr = true))
  }

  private def parseHetuRequestV1(parsedJson: JValue): Either[HttpStatus, HetuRequestV1] = {
    JsonSerializer.validateAndExtract[HetuRequestV1](parsedJson)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .filterOrElse(_.v == 1, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      .flatMap(req => Hetu.validFormat(req.hetu).map(_ => req))
      .flatMap(validateOpiskeluoikeudenTyypit(_, allowVirtaOrYtr = true))
  }

  private def parseBulkHetuRequestV1(json: JValue): Either[HttpStatus, BulkHetuRequestV1] = {
    val MaxHetus = 1000
    JsonSerializer.validateAndExtract[BulkHetuRequestV1](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .filterOrElse(_.v == 1, KoskiErrorCategory.badRequest.queryParam("Tuntematon versio"))
      .flatMap(validateOpiskeluoikeudenTyypit(_, allowVirtaOrYtr = false))
      .filterOrElse(_.hetut.length <= MaxHetus, KoskiErrorCategory.badRequest.queryParam(s"Liian monta hetua, enintään $MaxHetus sallittu"))
      .flatMap(req => {
        req.hetut.map(Hetu.validFormat).collectFirst { case Left(status) => status } match {
          case Some(status) => Left(status)
          case None => Right(req)
        }
      })
  }

  private def validateOpiskeluoikeudenTyypit[T <: LuovutuspalveluRequest](req: T, allowVirtaOrYtr: Boolean): Either[HttpStatus, T] = {
    val virtaYtrTyypit = List("korkeakoulutus", "ylioppilastutkinto")
    if (req.opiskeluoikeudenTyypit.isEmpty) {
      Left(KoskiErrorCategory.badRequest.queryParam("Opiskeluoikeuden tyypit puuttuvat"))
    } else if (!req.opiskeluoikeudenTyypit.forall(application.koodistoViitePalvelu.validate("opiskeluoikeudentyyppi", _).isDefined)) {
      Left(KoskiErrorCategory.badRequest.queryParam("Tuntematon opiskeluoikeudentyyppi"))
    } else if (!allowVirtaOrYtr && req.opiskeluoikeudenTyypit.exists(virtaYtrTyypit.contains(_))) {
      Left(KoskiErrorCategory.badRequest.queryParam("Korkeakoulutus tai ylioppilastutkinto ei sallittu"))
    } else {
      Right(req)
    }
  }
}

trait LuovutuspalveluRequest {
  def opiskeluoikeudenTyypit: List[String]
  def v: Int
}

case class HetuRequestV1(v: Int, hetu: String, opiskeluoikeudenTyypit: List[String]) extends LuovutuspalveluRequest

case class OidRequestV1(v: Int, oid: String, opiskeluoikeudenTyypit: List[String]) extends LuovutuspalveluRequest

case class LuovutuspalveluResponseV1(henkilö: LuovutuspalveluHenkilöV1, opiskeluoikeudet: Seq[Opiskeluoikeus])

case class LuovutuspalveluHenkilöV1(oid: Henkilö.Oid, hetu: Option[Henkilö.Hetu], syntymäaika: Option[LocalDate],  turvakielto: Boolean)

case class BulkHetuRequestV1(v: Int, hetut: List[String], opiskeluoikeudenTyypit: List[String]) extends LuovutuspalveluRequest

