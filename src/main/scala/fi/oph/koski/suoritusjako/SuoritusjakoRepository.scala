package fi.oph.koski.suoritusjako

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
  def get(uuid: String): Either[HttpStatus, SuoritusjakoRow] =
    runDbSync(SuoritusJako.filter(r => r.uuid === uuid && r.voimassaAsti >= now).result.headOption)
      .toRight(KoskiErrorCategory.notFound())

  // TODO: voimassaoloaika
  def put(uuid: String, oppijaOid: String, suoritusIds: List[SuoritusIdentifier]): Unit =
    runDbSync(SuoritusJako.insertOrUpdate(SuoritusjakoRow(uuid, oppijaOid, JsonSerializer.serializeWithRoot(suoritusIds), timestamp(LocalDateTime.now.plusMonths(6)), now)))

  private def now = timestamp(LocalDateTime.now)
}
