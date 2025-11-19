package fi.oph.koski.omaopintopolkuloki

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.log.Logging
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import fi.oph.koski.omaopintopolkuloki.AuditLogDynamoDB.AuditLogTableName
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeDefinition, AttributeValue, CreateTableRequest, DeleteTableRequest, KeySchemaElement, KeyType, ProvisionedThroughput, PutItemRequest, ScalarAttributeType}

import scala.jdk.CollectionConverters._

object AuditLogMockData extends Logging {

  def createTable(db: DynamoDbClient) = {
    logger.info("Creating AuditLog DynamoDB table")
    val tableDefinition = CreateTableRequest.builder()
      .attributeDefinitions(
        AttributeDefinition.builder.attributeName("studentOid").attributeType(ScalarAttributeType.S).build(),
        AttributeDefinition.builder.attributeName("id").attributeType(ScalarAttributeType.S).build(),
      ).keySchema(
        KeySchemaElement.builder.attributeName("studentOid").keyType(KeyType.HASH).build(),
        KeySchemaElement.builder.attributeName("id").keyType(KeyType.RANGE).build()
      )
      .provisionedThroughput(
        ProvisionedThroughput.builder.readCapacityUnits(20.longValue()).writeCapacityUnits(20.longValue()).build()
      )
      .tableName(AuditLogTableName).build

    if (db.listTables.tableNames().asScala.contains(AuditLogTableName)) {
      db.deleteTable(DeleteTableRequest.builder().tableName(AuditLogTableName).build())
    }
    db.createTable(tableDefinition)
    logger.info("Created AuditLog DynamoDB table")
  }

  def insertMockData(db: DynamoDbClient) = {
    logger.info("Inserting MockData to DynamoDB")
    data
      .zipWithIndex
      .map{ case (data, index) => Map(
        "studentOid" -> AttributeValue.builder.s(data.studentOid).build(),
        "time" -> AttributeValue.builder.s(data.time).build(),
        "organizationOid" -> AttributeValue.builder.l(data.organizationOid.map(o => AttributeValue.builder.s(o).build()).asJava).build(),
        "id" -> AttributeValue.builder.s(index.toString).build(),
        "raw" -> AttributeValue.builder.s(data.raw).build()
      ).asJava}
      .foreach(item => db.putItem(PutItemRequest.builder().tableName(AuditLogTableName).item(item).build()))
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
      studentOid = KoskiSpecificMockOppijat.ylioppilas.oid,
      time = "2019-05-19T11:21:42.123+03",
      organizationOid = List(MockOrganisaatiot.helsinginKaupunki),
      raw = rawAuditlog("YTR_OPISKELUOIKEUS_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.ylioppilas.oid,
      time = "2019-05-19T11:21:42.123+03",
      organizationOid = List(MockOrganisaatiot.stadinAmmattiopisto),
      raw = rawAuditlog("MUUTOSHISTORIA_KATSOMINEN")
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
      studentOid = KoskiSpecificMockOppijat.eskari.oid,
      time = "2026-01-12T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.jyväskylänYliopisto),
      raw = rawAuditlog("readData", "kitu")
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
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid,
      time = "2000-01-12T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.helsinginKaupunki),
      raw = rawAuditlog("KANSALAINEN_SUORITUSJAKO_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid,
      time = "2000-01-13T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.helsinginKaupunki),
      raw = rawAuditlog("KANSALAINEN_SUORITUSJAKO_KATSOMINEN_SUORITETUT_TUTKINNOT")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid,
      time = "2000-01-14T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.helsinginKaupunki),
      raw = rawAuditlog("KANSALAINEN_SUORITUSJAKO_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid,
      time = "2000-01-15T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.dvv),
      raw = rawAuditlog("OAUTH2_KATSOMINEN_KAIKKI_TIEDOT")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid,
      time = "2000-01-16T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.dvv),
      raw = rawAuditlog("OAUTH2_KATSOMINEN_SUORITETUT_TUTKINNOT")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.ylioppilasLukiolainen.oid,
      time = "2000-01-17T20:31:32.104+03",
      organizationOid = List(MockOrganisaatiot.dvv),
      raw = rawAuditlog("OAUTH2_KATSOMINEN_AKTIIVISET_JA_PAATTYNEET_OPINNOT")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.master.oid,
      time = "2018-07-20T21:38:35.104+03",
      organizationOid = List(MockOrganisaatiot.stadinAmmattiopisto),
      raw = rawAuditlog("OPISKELUOIKEUS_KATSOMINEN")
    ),
    MockData(
      studentOid = KoskiSpecificMockOppijat.slave.henkilö.oid,
      time = "2018-07-21T21:38:35.104+03",
      organizationOid = List(MockOrganisaatiot.helsinginKaupunki),
      raw = rawAuditlog("OPISKELUOIKEUS_KATSOMINEN")
    ),

  )

  private case class MockData(
    studentOid: String,
    time: String,
    organizationOid: List[String],
    raw: String
  )
}
