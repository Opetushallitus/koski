package fi.oph.koski.omaopintopolkuloki


import java.util
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.organisaatio.Opetushallitus
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging
import fi.oph.koski.mydata.MyDataConfig
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.omaopintopolkuloki.AuditLogDynamoDB.AuditLogTableName
import fi.oph.koski.schema.Henkilö.Oid
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, QueryRequest}

import scala.jdk.CollectionConverters._

class AuditLogService(val application: KoskiApplication) extends Logging with MyDataConfig {
  private val organisaatioRepository = application.organisaatioRepository
  private val dynamoDB = AuditLogDynamoDB.buildDb(application.config)

  def queryLogsFromDynamo(masterOppijaOid: String): Either[HttpStatus, Seq[OrganisaationAuditLogit]] = {
    val kaikkiOppijanOidit = application.opintopolkuHenkilöFacade.findSlaveOids(masterOppijaOid).toSet + masterOppijaOid

    val queryResult = kaikkiOppijanOidit
      .toIterator
      .flatMap(runQuery)

    buildLogs(queryResult)
  }

  private def runQuery(oppijaOid: Oid): Iterator[util.Map[Oid, AttributeValue]] = {
    val queryRequest = querySpec(oppijaOid).build()
    val responses = dynamoDB.queryPaginator(queryRequest)
    responses.items().iterator().asScala
  }

  private def querySpec(oppijaOid: String) =
    QueryRequest.builder
      .tableName(AuditLogTableName)
      .keyConditionExpression("studentOid = :oid")
      .filterExpression(
        """not contains (organizationOid, :self) and
          | (contains (#rawEntry, :katsominen) or
          |  contains (#rawEntry, :muutoshistoria_katsominen) or
          |  contains (#rawEntry, :ytr_katsominen) or
          |  contains (#rawEntry, :oauth2_katsominen_kaikki_tiedot) or
          |  contains (#rawEntry, :oauth2_katsominen_suoritetut_tutkinnot) or
          |  contains (#rawEntry, :oauth2_katsominen_aktiiviset_ja_paattyneet_opinnot) or
          |  contains (#rawEntry, :suoritusjako_katsominen) or
          |  contains (#rawEntry, :suoritusjako_katsominen_suoritetut_tutkinnot) or
          |  contains (#rawEntry, :suoritusjako_katsominen_aktiiviset_ja_paattyneet_opinnot) or
          |  contains (#rawEntry, :varda_service) or
          |  contains (#rawEntry, :kitu_service))
          |  """.stripMargin)
      .expressionAttributeNames(Map("#rawEntry" -> "raw").asJava)
      .expressionAttributeValues({
        val valueMap = new util.HashMap[String, AttributeValue]()
        valueMap.put(":oid", AttributeValue.builder.s(oppijaOid).build)
        valueMap.put(":self", AttributeValue.builder.s("self").build)
        valueMap.put(":katsominen", AttributeValue.builder.s("\"OPISKELUOIKEUS_KATSOMINEN\"").build)
        valueMap.put(":muutoshistoria_katsominen", AttributeValue.builder.s("\"MUUTOSHISTORIA_KATSOMINEN\"").build)
        valueMap.put(":ytr_katsominen", AttributeValue.builder.s("\"YTR_OPISKELUOIKEUS_KATSOMINEN\"").build)
        valueMap.put(":suoritusjako_katsominen", AttributeValue.builder.s("\"KANSALAINEN_SUORITUSJAKO_KATSOMINEN\"").build)
        valueMap.put(":suoritusjako_katsominen_suoritetut_tutkinnot", AttributeValue.builder.s("\"KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT\"").build)
        valueMap.put(":suoritusjako_katsominen_aktiiviset_ja_paattyneet_opinnot", AttributeValue.builder.s("\"KANSALAINEN_SUORITUSJAKO_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT\"").build)
        valueMap.put(":oauth2_katsominen_kaikki_tiedot", AttributeValue.builder.s("\"OAUTH2_KATSOMINEN_KAIKKI_TIEDOT\"").build)
        valueMap.put(":oauth2_katsominen_suoritetut_tutkinnot", AttributeValue.builder.s("\"OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT\"").build)
        valueMap.put(":oauth2_katsominen_aktiiviset_ja_paattyneet_opinnot", AttributeValue.builder.s("\"OAUTH2_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT\"").build)
        valueMap.put(":varda_service", AttributeValue.builder.s("\"varda\"").build)
        valueMap.put(":kitu_service", AttributeValue.builder.s("\"kitu\"").build)
        valueMap
      })

  private def convertToAuditLogRow(item: util.Map[String, AttributeValue]): AuditlogRow = {
    val organizationOid = item.asScala.view.collectFirst {
      case ("organizationOid", value) if value.l() != null =>
        value.l().asScala.toList.collect {
          case v if v.s() != null => v.s()
        }
    }.getOrElse(List.empty[String])

    val raw = item.asScala.view.collectFirst {
      case ("raw", value) if value.s() != null => value.s()
    }.getOrElse("")

    val time = item.asScala.view.collectFirst {
      case ("time", value) if value.s() != null => value.s()
    }.getOrElse("")

    AuditlogRow(organizationOid, raw, time)
  }

  private def buildLogs(queryResult: Iterator[util.Map[Oid, AttributeValue]]): Either[HttpStatus, Seq[OrganisaationAuditLogit]] = {
    val timestampsGrouped = queryResult
      .map(item => {
        val parsedRow = convertToAuditLogRow(item)
        val parsedRaw = JsonSerializer.parse[AuditlogRaw](parsedRow.raw, ignoreExtras = true)
        val organisaatioOidit = parsedRow.organizationOid.sorted
        val timestampString = parsedRow.time
        val serviceName = parsedRaw.serviceName
        val isMyDataUse = parsedRaw.operation.startsWith("OAUTH2_KATSOMINEN") || parsedRow.organizationOid.headOption.exists(isMyDataOrg)
        // TODO: Jakolinkkien käyttöjen palauttaminen frontille on toteutettu valmiiksi, mutta oma-opintopolku-lokin DynamoDB-parsinta skippaa näiden
        // entryjen käsittelyn, minkä vuoksi tuotantoympäristöissä näitä entryjä ei vielä käytännössä DynamoDB:ssä ole.
        val isJakolinkkiUse = parsedRaw.operation.startsWith("KANSALAINEN_SUORITUSJAKO_KATSOMINEN")
        (organisaatioOidit, serviceName, isMyDataUse, isJakolinkkiUse, timestampString)
      })
      .toSeq
      .groupBy(x => (x._1, x._2, x._3, x._4))
      .view
      .mapValues(_.map(_._5))
      .toMap

    HttpStatus.foldEithers(
      timestampsGrouped
        .map {
          case ((orgs, serviceName, isMyDataUse, isJakolinkkiUse), timestamps) =>
            HttpStatus.foldEithers(orgs.map(toOrganisaatio))
              .map(orgs => OrganisaationAuditLogit(orgs, serviceName, isMyDataUse, isJakolinkkiUse, timestamps))
        }
        .toSeq
    )
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
  serviceName: String,
  operation: String
)

case class OrganisaationAuditLogit(
  organizations: Seq[Organisaatio],
  serviceName: String,
  isMyDataUse: Boolean,
  isJakolinkkiUse: Boolean,
  timestamps: Seq[String]
)

case class Organisaatio(
  oid: String,
  name: LocalizedString
)
