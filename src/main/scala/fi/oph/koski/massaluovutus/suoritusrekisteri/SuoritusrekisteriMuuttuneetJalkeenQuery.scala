package fi.oph.koski.massaluovutus.suoritusrekisteri

import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{DB, QueryMethods}
import fi.oph.koski.log._
import fi.oph.koski.massaluovutus.QueryFormat
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.scalaschema.annotation.{Description, Title}

import java.sql.Timestamp
import java.time.LocalDateTime

@Title("Suoritusrekisterin kysely päivämäärän perusteella")
@Description("Palauttaa Suoritusrekisteriä varten räätälöidyt tiedot annettujen oppijoiden ja koulutusmuodon mukaisista opiskeluoikeuksista tietyn ajanhetken jälkeen.")
@Description("Vastauksen skeema on saatavana <a href=\"/koski/json-schema-viewer/?schema=suoritusrekisteri-result.json\">täältä.</a>")
case class SuoritusrekisteriMuuttuneetJalkeenQuery(
  @EnumValues(Set("sure"))
  `type`: String = "sure",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  muuttuneetJälkeen: LocalDateTime,
) extends SuoritusrekisteriQuery with Logging {
  def getOpiskeluoikeusIds(db: DB): Seq[(Int, Timestamp)] =
    QueryMethods.runDbSync(
      db,
      sql"""
        SELECT id, aikaleima
        FROM opiskeluoikeus
        WHERE aikaleima >= ${Timestamp.valueOf(muuttuneetJälkeen)}
          AND koulutusmuoto = any(${SuoritusrekisteriQuery.opiskeluoikeudenTyypit})
        ORDER BY aikaleima
      """.as[(Int, Timestamp)])
}
