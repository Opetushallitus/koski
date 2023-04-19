package fi.oph.koski.omaopintopolkuloki


import java.util
import com.amazonaws.services.dynamodbv2.document.{Item}
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.organisaatio.{Opetushallitus}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.omaopintopolkuloki.AuditLogDynamoDB.AuditLogTableName

import scala.collection.JavaConverters._

class AuditLogService(app: KoskiApplication) extends Logging {
  private val organisaatioRepository = app.organisaatioRepository
  private val dynamoDB = AuditLogDynamoDB.buildDb(app.config)

  def queryLogsFromDynamo(oppijaOid: String): Either[HttpStatus, Seq[OrganisaationAuditLogit]] = {
    runQuery(oppijaOid).flatMap(results => HttpStatus.foldEithers(buildLogs(results).toSeq))
  }

  private def runQuery(oppijaOid: String): Either[HttpStatus, Seq[Item]] = {
    val auditLogTable = dynamoDB.getTable(AuditLogTableName)
    val querySpec = new QuerySpec()
      .withKeyConditionExpression("studentOid = :oid")
      .withFilterExpression("not contains (organizationOid, :self) and (contains (#rawEntry, :katsominen) or contains(#rawEntry, :varda_service))")
      .withNameMap(Map("#rawEntry" -> "raw").asJava)
      .withValueMap({
        val valueMap = new util.HashMap[String, Object]()
        valueMap.put(":oid", oppijaOid)
        valueMap.put(":self", "self")
        valueMap.put(":katsominen", "\"OPISKELUOIKEUS_KATSOMINEN\"")
        valueMap.put(":varda_service", "\"varda\"")
        valueMap
      })

    try {
      Right(auditLogTable.query(querySpec).asScala.toIterator.toList)
    } catch {
      case e: Exception => {
        logger.error(e)(s"AuditLogien haku epäonnistui oidille $oppijaOid")
        Left(KoskiErrorCategory.internalError())
      }
    }
  }

  private def buildLogs(queryResults: Seq[Item]): Iterable[Either[HttpStatus, OrganisaationAuditLogit]] = {
    val timestampsGroupedByListOfOidsAndServiceName = queryResults.map(item => {
      val parsedRow = JsonSerializer.parse[AuditlogRow](item.toJSON, ignoreExtras = true)
      val parsedRaw = JsonSerializer.parse[AuditlogRaw](parsedRow.raw, ignoreExtras = true)
      val organisaatioOidit = parsedRow.organizationOid
      val timestampString = parsedRow.time
      val serviceName = parsedRaw.serviceName
      (organisaatioOidit, serviceName, timestampString)
    }).groupBy(x => (x._1, x._2)).mapValues(_.map(_._3))

    timestampsGroupedByListOfOidsAndServiceName.map { case ((orgs, serviceName), timestamps) =>
      HttpStatus.foldEithers(orgs.map(toOrganisaatio))
        .map(orgs => OrganisaationAuditLogit(orgs, serviceName, timestamps))
    }
  }

  private def toOrganisaatio(oid: String): Either[HttpStatus, Organisaatio] = {
    val nimi = organisaatioRepository.getOrganisaatio(oid)
      .flatMap(_.nimi)
      .map(name => Organisaatio(oid, name))
      .orElse(isOpetushallitus(oid))
      .toRight(KoskiErrorCategory.internalError())
    nimi.left.foreach(_ => logger.error(s"AuditLogissa olevaa organisaatiota $oid ei löytynyt organisaatiopalvelusta. Ks. oletettava syy TOR-1050."))
    nimi
  }

  private def isOpetushallitus(oid: String) = {
    if (oid == Opetushallitus.organisaatioOid) {
      Some(Organisaatio(Opetushallitus.organisaatioOid, Opetushallitus.nimi))
    } else {
      None
    }
  }
}

case class AuditlogRow (
  organizationOid: List[String],
  raw: String,
  time: String
)
case class AuditlogRaw (
  serviceName: String
)

case class OrganisaationAuditLogit(
  organizations: Seq[Organisaatio],
  serviceName: String,
  timestamps: Seq[String]
)

case class Organisaatio(
  oid: String,
  name: LocalizedString
)
