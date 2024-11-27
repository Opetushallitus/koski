package fi.oph.koski.massaluovutus.suoritusrekisteri

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.sql.Timestamp

@Title("Suoritusrekisterin kysely oppijoiden perusteella")
@Description("Palauttaa Suoritusrekisteriä varten räätälöidyt tiedot annettujen oppijoiden opiskeluoikeuksista.")
@Description("Vastauksen skeema on saatavana <a href=\"/koski/json-schema-viewer/?schema=suoritusrekisteri-result.json\">täältä.</a>")
case class SuoritusrekisteriOppijaOidsQuery(
  @EnumValues(Set("sure-oppijat"))
  `type`: String = "sure-oppijat",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  oppijaOids: Seq[String],
) extends SuoritusrekisteriQuery with Logging {
  def getOpiskeluoikeusIds(db: DB): Seq[(Int, Timestamp)] = {
    QueryMethods.runDbSync(
      db,
      sql"""
        SELECT id, aikaleima
        FROM opiskeluoikeus
        WHERE
          opiskeluoikeus.oppija_oid = any($oppijaOids) OR
          opiskeluoikeus.oppija_oid IN (
            SELECT oid
            FROM henkilo
            WHERE master_oid = any($oppijaOids)
          )
          AND koulutusmuoto = any(${SuoritusrekisteriQuery.opiskeluoikeudenTyypit})
      """.as[(Int, Timestamp)])
  }
}
