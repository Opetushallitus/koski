package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.{DB, DatabaseExecutionContext, QueryMethods}
import fi.oph.koski.history.{OpiskeluoikeusHistory, YtrOpiskeluoikeusHistory}
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö.Oid
import org.json4s.jackson.JsonMethods
import org.json4s.{JArray, JValue}
import slick.jdbc.GetResult
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._

import java.time.LocalDateTime

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

  def getAll: Seq[OpiskeluoikeushistoriaVirhe] =
    runDbSync(
      sql"""
        SELECT *
        FROM opiskeluoikeushistoria_virheet
      """.as[OpiskeluoikeushistoriaVirhe]
    )

  def saveYtr(
    opiskeluoikeus: JValue,
    historia: YtrOpiskeluoikeusHistory,
    diff: JArray,
  ): Int = runDbSync(
    sql"""
        INSERT INTO ytr_opiskeluoikeushistoria_virheet(
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

  def getAllYtr: Seq[OpiskeluoikeushistoriaVirhe] =
    runDbSync(
      sql"""
        SELECT *
        FROM ytr_opiskeluoikeushistoria_virheet
      """.as[OpiskeluoikeushistoriaVirhe]
    )

  def truncate: Int =
    runDbSync(sql"""TRUNCATE opiskeluoikeushistoria_virheet""".asUpdate)

  implicit protected def getOpiskeluoikeushistoriaVirheRow: GetResult[OpiskeluoikeushistoriaVirhe] = GetResult(r => {
    OpiskeluoikeushistoriaVirhe(
      aikaleima = r.rs.getTimestamp("aikaleima").toLocalDateTime,
      opiskeluoikeus = r.getJson("opiskeluoikeus"),
      historia = r.getJson("historia"),
      diff = r.getJson("diff"),
    )
  })
}

case class OpiskeluoikeushistoriaVirhe(
  aikaleima: LocalDateTime,
  opiskeluoikeus: JValue,
  historia: JValue,
  diff: JValue,
)
