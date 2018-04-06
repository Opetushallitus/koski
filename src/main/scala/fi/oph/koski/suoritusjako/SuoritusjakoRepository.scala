package fi.oph.koski.suoritusjako

import java.sql.Date
import java.sql.Timestamp.{valueOf => timestamp}
import java.time.LocalDateTime

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.SuoritusJako
import fi.oph.koski.db.{DatabaseExecutionContext, KoskiDatabaseMethods, SuoritusjakoRow}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging

class SuoritusjakoRepository(val db: DB) extends Logging with DatabaseExecutionContext with KoskiDatabaseMethods {
  def get(secret: String): Either[HttpStatus, SuoritusjakoRow] =
    runDbSync(SuoritusJako.filter(r => r.secret === secret && r.voimassaAsti >= Date.valueOf(LocalDateTime.now.toLocalDate)).result.headOption)
      .toRight(KoskiErrorCategory.notFound())

  // TODO: voimassaoloaika
  def put(secret: String, oppijaOid: String, suoritusIds: List[SuoritusIdentifier]): Unit =
    runDbSync(SuoritusJako.insertOrUpdate(SuoritusjakoRow(
      0,
      secret,
      oppijaOid,
      JsonSerializer.serializeWithRoot(suoritusIds),
      Date.valueOf(LocalDateTime.now.plusMonths(6).toLocalDate),
      now
    )))

  private def now = timestamp(LocalDateTime.now)
}
