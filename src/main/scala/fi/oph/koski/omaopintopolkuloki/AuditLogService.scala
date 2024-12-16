package fi.oph.koski.omaopintopolkuloki


import java.util
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.omaopintopolkuloki.AuditLogDynamoDB.AuditLogTableName
import fi.oph.scalaschema.Serializer.format
import org.json4s.{Extraction, JValue}
import org.json4s.jackson.JsonMethods.compact
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.QueryResponse
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, QueryRequest}

import scala.collection.JavaConverters._

class AuditLogService(app: KoskiApplication) extends Logging {
  private val organisaatioRepository = app.organisaatioRepository
  private val dynamoDB = AuditLogDynamoDB.buildDb(app.config)

  def queryLogsFromDynamo(oppijaOid: String): Either[HttpStatus, Seq[OrganisaationAuditLogit]] = {
    runQuery(oppijaOid).flatMap(results => HttpStatus.foldEithers(buildLogs(results).toSeq))
  }

  private def runQuery(oppijaOid: String): Either[HttpStatus, Seq[util.Map[String, AttributeValue]]] = {
    val querySpec = QueryRequest.builder
      .tableName(AuditLogTableName)
      .keyConditionExpression("studentOid = :oid")
      .filterExpression("not contains (organizationOid, :self) and (contains (#rawEntry, :katsominen) or contains(#rawEntry, :varda_service))")
      .expressionAttributeNames(Map("#rawEntry" -> "raw").asJava)
      .expressionAttributeValues({
        val valueMap = new util.HashMap[String, AttributeValue]()
        valueMap.put(":oid", AttributeValue.builder.s(oppijaOid).build)
        valueMap.put(":self", AttributeValue.builder.s("self").build)
        valueMap.put(":katsominen", AttributeValue.builder.s("\"OPISKELUOIKEUS_KATSOMINEN\"").build)
        valueMap.put(":varda_service", AttributeValue.builder.s("\"varda\"").build)
        valueMap
      })

    try {
      Right(dynamoDB.query(querySpec.build()).items().asScala)
    } catch {
      case e: Exception => {
        logger.error(e)(s"AuditLogien haku epäonnistui oidille $oppijaOid")
        Left(KoskiErrorCategory.internalError())
      }
    }
  }
  private def convertToAuditLogRow(item: util.Map[String, AttributeValue]): AuditlogRow = {
    val organizationOid = item.asScala.view.collectFirst {
      case ("organizationOid", value) if value.ss() != null => value.ss().asScala.toList
    }.getOrElse(List.empty[String])

    val raw = item.asScala.view.collectFirst {
      case ("raw", value) if value.ss() != null => value.s()
    }.getOrElse("")

    val time = item.asScala.view.collectFirst {
      case ("time", value) if value.s() != null => value.s()
    }.getOrElse("")

    AuditlogRow(organizationOid, raw, time)
  }

  private def buildLogs(queryResults: Seq[util.Map[String, AttributeValue]]): Iterable[Either[HttpStatus, OrganisaationAuditLogit]] = {
    val timestampsGroupedByListOfOidsAndServiceName = queryResults.map(item => {
      val parsedRow = convertToAuditLogRow(item)
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
