package fi.oph.koski.omaopintopolkuloki

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import fi.oph.koski.omaopintopolkuloki.AuditLogDynamoDB.AuditLogTableName

import scala.collection.JavaConverters._

object AuditLogMockData extends Logging {

  def createTable(db: AmazonDynamoDB) = {
    logger.info("Creating AuditLog DynamoDB table")
    val tableDefinition = new CreateTableRequest()
      .withAttributeDefinitions(
        new AttributeDefinition("studentOid", ScalarAttributeType.S),
        new AttributeDefinition("id", ScalarAttributeType.S)
      )
      .withKeySchema(
        new KeySchemaElement("studentOid", KeyType.HASH),
        new KeySchemaElement("id", KeyType.RANGE)
      )
      .withProvisionedThroughput(
        new ProvisionedThroughput(20.longValue(), 20.longValue())
      )
      .withTableName(AuditLogTableName)

    if (db.listTables.getTableNames.asScala.contains(AuditLogTableName)) {
      db.deleteTable(AuditLogTableName)
    }
    db.createTable(tableDefinition)
    logger.info("Created AuditLog DynamoDB table")
  }

  def insertMockData(db: AmazonDynamoDB) = {
    logger.info("Inserting MockData to DynamoDB")
    data
      .zipWithIndex
      .map{ case (data, index) => Map(
        "studentOid" -> new AttributeValue(data.studentOid),
        "time" -> new AttributeValue(data.time),
        "organizationOid" -> new AttributeValue(data.organizationOid.asJava),
        "id" -> new AttributeValue(index.toString),
        "raw" -> new AttributeValue(data.raw)
      ).asJava}
      .foreach(item => db.putItem(AuditLogTableName, item))
    logger.info("Done inserting MockData to DynamoDB")
  }

  private def rawAuditlog(operation: String, serviceName: String = "koski"): String = {
    s"""
      |{
      | \"operation\": \"${operation}\",
      | \"target\": {
      |   \"oppijaHenkiloOid\": \"123.123.123\"
      | },
      | \"user\": {
      |   \"oid": "678.678.678",
      |   \"ip\": \"127.0.0.1\"
      | },
      | \"serviceName\": \"${serviceName}\"
      |}
    """.stripMargin
  }

  private lazy val data = List(
    MockData(
      studentOid = KoskiSpecificMockOppijat.amis.oid,
      time = "2018-07-19T21:38:35.104+03",
      organizationOid = List(MockOrganisaatiot.helsinginKaupunki, MockOrganisaatiot.stadinAmmattiopisto),
      raw = rawAuditlog("OPISKELUOIKEUS_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.amis.oid,
      time = "2018-07-19T21:38:35.104+03",
      organizationOid = List(MockOrganisaatiot.stadinAmmattiopisto, MockOrganisaatiot.helsinginKaupunki),
      raw = rawAuditlog("OPISKELUOIKEUS_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.amis.oid,
      time = "2019-05-19T11:21:42.123+03",
      organizationOid = List(MockOrganisaatiot.helsinginKaupunki, MockOrganisaatiot.stadinAmmattiopisto),
      raw = rawAuditlog("OPISKELUOIKEUS_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.amis.oid,
      time = "2020-01-12T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.helsinginKaupunki),
      raw = rawAuditlog("OPISKELUOIKEUS_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.amis.oid,
      time = "2020-01-12T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.ressunLukio),
      raw = rawAuditlog("OPISKELUOIKEUS_HAKU")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.amis.oid,
      time = "2020-01-12T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.ressunLukio),
      raw = rawAuditlog("OPISKELUOIKEUS_LISAYS")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.amis.oid,
      time = "2020-01-12T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.ressunLukio),
      raw = rawAuditlog("OPISKELUOIKEUS_MUUTOS")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.amis.oid,
      time = "2001-01-12T20:31:32.104+03",
      organizationOid = List("self"),
      raw = rawAuditlog("KANSALAINEN_OPISKELUOIKEUS_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.eskari.oid,
      time = "2020-01-12T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.päiväkotiTouhula),
      raw = rawAuditlog("dataAccess", "varda")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.ammattilainen.oid,
      time = "2001-01-12T20:31:32.104+03",
      organizationOid = List("self"),
      raw = rawAuditlog("KANSALAINEN_YLIOPPILASKOE_HAKU")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.ammattilainen.oid,
      time = "2001-01-12T20:31:32.104+03",
      organizationOid = List("self"),
      raw = rawAuditlog("KANSALAINEN_SUOMIFI_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.lukiolainen.oid,
      time = "2000-01-12T20:31:32.104+03",
      organizationOid = List("huoltaja"),
      raw = rawAuditlog("KANSALAINEN_HUOLTAJA_OPISKELUOIKEUS_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.lukiolainen.oid,
      time = "2000-01-12T20:31:32.104+03",
      organizationOid = List("huoltaja"),
      raw = rawAuditlog("KANSALAINEN_HUOLTAJA_YLIOPPILASKOE_HAKU")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.virtaEiVastaa.oid,
      time = "2000-01-12T20:31:32.104+03",
      organizationOid = List("123123123123123", MockOrganisaatiot.helsinginKaupunki),
      raw = rawAuditlog("OPISKELUOIKEUS_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.aikuisOpiskelija.oid,
      time = "2000-01-12T20:31:32.104+03",
      organizationOid = List(Opetushallitus.organisaatioOid),
      raw = rawAuditlog("OPISKELUOIKEUS_KATSOMINEN")
    )
  )

  private case class MockData(
    studentOid: String,
    time: String,
    organizationOid: List[String],
    raw: String
  )
}
