package fi.oph.koski.log

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.http.{HttpStatus, JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.KoskiOperation.KoskiOperation
import fi.oph.koski.log.KoskiMessageField.oppijaHenkiloOid
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.json4s.JsonAST.JValue

import scala.annotation.tailrec
import scala.util._

class LocalAuditLogServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache {

  before() {
    if (request.getRemoteHost != "127.0.0.1") {
      haltWithStatus(KoskiErrorCategory.forbidden(""))
    }
  }

  post("/") {
    withJsonBody { json =>
      extractAndValidate(json) match {
        case Left(status) => haltWithStatus(status)
        case Right(oiditJaOperaatio) => auditlogOids(oiditJaOperaatio)
      }
    }()
  }

  private def extractAndValidate(json: JValue) = {
    JsonSerializer.validateAndExtract[LocalAuditLogRequest](json)
      .left.map(errors => KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(errors)))
      .flatMap(validateRequest)
  }

  private def validateRequest(logRequest: LocalAuditLogRequest) = {
    for {
      oids <- toEitherList(logRequest.oids.map(HenkilöOid.validateHenkilöOid).toList)
      operation <- validateOperation(logRequest.operation)
    } yield (OiditJaOperaatio(oids, operation))
  }

  private def toEitherList(eithers: List[Either[HttpStatus, Oid]]): Either[HttpStatus, List[Oid]] = {
    @tailrec
    def helper(list: List[Either[HttpStatus, Oid]], oids: List[Oid]): Either[HttpStatus, List[Oid]] = {
      list match {
        case Nil => Right(oids)
        case x :: xs => x match {
          case Left(status) => Left(status)
          case Right(oid) => helper(xs, oids :+ oid)
        }
      }
    }

    helper(eithers, Nil)
  }

  private def validateOperation(operation: String): Either[HttpStatus, KoskiOperation] = Try(KoskiOperation.withName(operation)) match {
    case Success(op) => Right(op)
    case _ => Left(KoskiErrorCategory.badRequest.queryParam(s"Operaatiota $operation ei löydy"))
  }

  private def auditlogOids(o: OiditJaOperaatio) = {
    o.oids.foreach(oid => AuditLog.log(AuditLogMessage(o.operation, koskiSession, Map(oppijaHenkiloOid -> oid))))
    HttpStatus.ok
  }
}

case class LocalAuditLogRequest(oids: Seq[String], operation: String)

case class OiditJaOperaatio(oids: Seq[Oid], operation: KoskiOperation)
