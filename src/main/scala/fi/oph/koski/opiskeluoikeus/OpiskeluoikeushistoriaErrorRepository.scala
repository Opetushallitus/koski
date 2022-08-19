package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, DatabaseExecutionContext, QueryMethods}
import fi.oph.koski.history.OpiskeluoikeusHistory
import fi.oph.koski.log.Logging
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JValue}

class OpiskeluoikeushistoriaErrorRepository(val db: DB)
  extends DatabaseExecutionContext with QueryMethods with Logging
{
  def save(
    opiskeluoikeus: JValue,
    historia: OpiskeluoikeusHistory,
    diff: JArray,
  ): Int = runDbSync(
      sql"""
        INSERT INTO opiskeluoikeushistoria_virheet(
          opiskeluoikeus,
          historia,
          diff
        ) VALUES (
          ${JsonMethods.compact(opiskeluoikeus)}::jsonb,
          ${JsonMethods.compact(historia.asOpiskeluoikeusJson)}::jsonb,
          ${JsonMethods.compact(diff)}::jsonb
        )
        RETURNING id
      """.as[Int],
      allowNestedTransactions = true,
    ).head
}
