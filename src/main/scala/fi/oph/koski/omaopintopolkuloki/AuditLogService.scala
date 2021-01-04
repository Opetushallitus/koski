package fi.oph.koski.omaopintopolkuloki


import java.util

import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.omaopintopolkuloki.AuditLogDynamoDB.AuditLogTableName

import scala.collection.JavaConverters._

class AuditLogService(organisaatioRepository: OrganisaatioRepository, dynamoDB: DynamoDB) extends Logging {

  def queryLogsFromDynamo(oppijaOid: String): Either[HttpStatus, List[OrganisaationAuditLogit]] = {
    val auditLogTable = dynamoDB.getTable(AuditLogTableName)
    val querySpec = new QuerySpec()
      .withKeyConditionExpression("studentOid = :oid")
      .withFilterExpression("not contains (organizationOid, :self) and contains (#rawEntry, :katsominen)")
      .withNameMap(Map("#rawEntry" -> "raw").asJava)
      .withValueMap({
        val valueMap = new util.HashMap[String, Object]()
        valueMap.put(":oid", oppijaOid)
        valueMap.put(":self", "self")
        valueMap.put(":katsominen", "\"OPISKELUOIKEUS_KATSOMINEN\"")
        valueMap
      })

    val queryItems = try {
      Right(auditLogTable.query(querySpec).asScala.toIterator.toList)
    } catch {
      case e: Exception => {
        logger.error(e)(s"AuditLogien haku epäonnistui oidille $oppijaOid")
        Left(KoskiErrorCategory.internalError())
      }
    }

    val timestampsGroupedByListOfOids = queryItems.map(_.map(item => {
      val organisaatioOidit = item.getList[String]("organizationOid").asScala.sorted.toList
      val timestamp = item.getString("time")
      (organisaatioOidit -> timestamp)
    }).groupBy(_._1).mapValues(_.map(_._2)))

    timestampsGroupedByListOfOids.map(_.map { case (oids, timestamps) =>
      HttpStatus.foldEithers(oids.map(toOrganisaatio)).map(org => (org -> timestamps))
    }).flatMap(HttpStatus.foldEithers).map(_.map { case (orgs, timestamps) =>
      OrganisaationAuditLogit(orgs, timestamps)
    })
  }

  private def toOrganisaatio(oid: String): Either[HttpStatus, Organisaatio] = {
    val nimi = organisaatioRepository.getOrganisaatio(oid)
      .flatMap(_.nimi)
      .map(name => Organisaatio(oid, name))
      .toRight(KoskiErrorCategory.internalError())
    nimi.left.foreach(_ => logger.error(s"AuditLogissa olevaa organisaatiota $oid ei löytynyt organisaatiopalvelusta"))
    nimi
  }
}

case class OrganisaationAuditLogit(
  organizations: List[Organisaatio],
  timestamps: List[String]
)

case class Organisaatio(
  oid: String,
  name: LocalizedString
)
