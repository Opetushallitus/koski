package fi.oph.koski.suoritusjako

import java.sql.Date
import java.sql.Timestamp.{valueOf => timestamp}
import java.time.{LocalDate, LocalDateTime}

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

  def getAll(oppijaOid: String): Seq[SuoritusjakoRow] = {
    runDbSync(SuoritusJako.filter(r => r.oppijaOid === oppijaOid && r.voimassaAsti >= Date.valueOf(LocalDateTime.now.toLocalDate)).result)
  }

  // TODO: voimassaoloaika
  def put(secret: String, oppijaOid: String, suoritusIds: List[SuoritusIdentifier]): LocalDate = {
    val expirationDate = LocalDateTime.now.plusMonths(6).toLocalDate

    runDbSync(SuoritusJako.insertOrUpdate(SuoritusjakoRow(
      0,
      secret,
      oppijaOid,
      JsonSerializer.serializeWithRoot(suoritusIds),
      Date.valueOf(expirationDate),
      now
    )))

    expirationDate
  }

  def delete(oppijaOid: String, secret: String): HttpStatus = {
    val deleted = runDbSync(SuoritusJako
      .filter(r => r.oppijaOid === oppijaOid && r.secret === secret && r.voimassaAsti >= Date.valueOf(LocalDateTime.now.toLocalDate))
      .delete
    )

    if (deleted == 0) {
      KoskiErrorCategory.notFound()
    } else {
      HttpStatus.ok
    }
  }

  private def now = timestamp(LocalDateTime.now)
}
