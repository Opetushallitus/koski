package fi.oph.koski.massaluovutus.suoritusrekisteri

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits.getKoskiOpiskeluoikeusRow
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, KoskiOpiskeluoikeusRow, KoskiTables, QueryMethods}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.koskiuser.Rooli.{OPHKATSELIJA, OPHPAAKAYTTAJA}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.{MassaluovutusQueryParameters, MassaluovutusQueryPriority, QueryFormat, QueryResultWriter}
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.schema.{KoskeenTallennettavaOpiskeluoikeus, KoskiSchema}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.sql.Timestamp
import java.time.LocalDateTime

@Title("Suoritusrekisterin kysely")
@Description("Palauttaa Suoritusrekisteriä varten räätälöidyt tiedot annettujen oppijoiden ja koulutusmuodon mukaisista opiskeluoikeuksista.")
case class SuoritusrekisteriQuery(
  @EnumValues(Set("sure"))
  `type`: String = "sure",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  muuttuneetJälkeen: LocalDateTime,
) extends MassaluovutusQueryParameters with Logging {
  override def priority: Int = MassaluovutusQueryPriority.high

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: KoskiSpecificSession): Either[String, Unit] = {
    muuttuneetOpiskeluoikeudet(application.replicaDatabase.db).foreach { oid =>
      getOpiskeluoikeus(application, oid).foreach { response =>
        val ooTyyppi = response.opiskeluoikeus.tyyppi.koodiarvo
        val ptsTyyppi = response.opiskeluoikeus.suoritukset.map(_.tyyppi.koodiarvo).mkString("-")
        writer.putJson(s"$ooTyyppi-$ptsTyyppi-${response.opiskeluoikeus.oid}", response)
      }
    }
    Right(())
  }

  override def queryAllowed(application: KoskiApplication)(implicit user: KoskiSpecificSession): Boolean =
    user.hasRole(OPHKATSELIJA) || user.hasRole(OPHPAAKAYTTAJA)


  private def muuttuneetOpiskeluoikeudet(db: DB): Seq[Int] =
    QueryMethods.runDbSync(
      db,
      sql"""
        SELECT id
        FROM opiskeluoikeus
        WHERE aikaleima >= ${Timestamp.valueOf(muuttuneetJälkeen)}
          AND koulutusmuoto = any(${SuoritusrekisteriQuery.opiskeluoikeudenTyypit})
      """.as[Int])

  private def getOpiskeluoikeus(application: KoskiApplication, id: Int): Option[SureResponse] =
    QueryMethods.runDbSync(
      application.replicaDatabase.db,
      sql"""
         SELECT *
         FROM opiskeluoikeus
         WHERE id = $id
      """.as[KoskiOpiskeluoikeusRow]
    ).headOption.flatMap(toResponse(application))

  private def toResponse(application: KoskiApplication)(row: KoskiOpiskeluoikeusRow): Option[SureResponse] = {
    val json = KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima)
    application.validatingAndResolvingExtractor.extract[KoskeenTallennettavaOpiskeluoikeus](KoskiSchema.strictDeserialization)(json) match {
      case Right(oo: KoskeenTallennettavaOpiskeluoikeus) =>
        SureOpiskeluoikeus(oo).map(SureResponse(row.oppijaOid, row.aikaleima.toLocalDateTime, _))
      case Left(errors) =>
        logger.warn(s"Error deserializing opiskeluoikeus: ${errors}")
        None
    }
  }

}

object SuoritusrekisteriQuery {
  def opiskeluoikeudenTyypit: List[String] = List(
    "perusopetus",
    "aikuistenperusopetus",
    "ammatillinenkoulutus",
    "tuva",
    "vapaansivistystyonkoulutus",
    "diatutkinto",
    "ebtutkinto",
    "ibtutkinto",
    "internationalschool",
  )
}
