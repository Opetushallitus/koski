package fi.oph.koski.suoritusjako

import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate}

import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.{SuoritusJako, SuoritusjakoTable}
import fi.oph.koski.db.{DatabaseExecutionContext, KoskiDatabaseMethods, SuoritusjakoRow}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging

class SuoritusjakoRepository(val db: DB) extends Logging with DatabaseExecutionContext with KoskiDatabaseMethods {
  def get(secret: String): Either[HttpStatus, SuoritusjakoRow] =
    runDbSync(SuoritusJako.filter(r => r.secret === secret && r.voimassaAsti >= Date.valueOf(LocalDate.now)).result.headOption)
      .toRight(KoskiErrorCategory.notFound())

  def getAll(oppijaOid: String): Seq[SuoritusjakoRow] = {
    runDbSync(SuoritusJako.filter(r => r.oppijaOid === oppijaOid && r.voimassaAsti >= Date.valueOf(LocalDate.now)).result)
  }

  def create(secret: String, oppijaOid: String, suoritusIds: List[SuoritusIdentifier]): Either[HttpStatus, Suoritusjako] = {
    val expirationDate = LocalDate.now.plusMonths(6)
    val maxSuoritusjakoCount = 100
    val timestamp = Timestamp.from(Instant.now())

    val currentSuoritusjakoCount = runDbSync(SuoritusJako
      .filter(r => r.oppijaOid === oppijaOid && r.voimassaAsti >= Date.valueOf(LocalDate.now)).length.result
    )

    if (currentSuoritusjakoCount < maxSuoritusjakoCount) {
      runDbSync(SuoritusJako.insertOrUpdate(SuoritusjakoRow(
        0,
        secret,
        oppijaOid,
        JsonSerializer.serializeWithRoot(suoritusIds),
        Date.valueOf(expirationDate),
        timestamp
      )))

      Right(Suoritusjako(secret, expirationDate, timestamp))
    } else {
      Left(KoskiErrorCategory.unprocessableEntity.liianMontaSuoritusjakoa())
    }
  }

  def delete(oppijaOid: String, secret: String): HttpStatus = {
    val deleted = runDbSync(SuoritusJako.filter(suoritusjakoFilter(oppijaOid, secret)).delete)
    if (deleted == 0) KoskiErrorCategory.notFound() else HttpStatus.ok
  }

  def update(oppijaOid: String, secret: String, expirationDate: LocalDate): HttpStatus = {
    val updated = runDbSync(
      SuoritusJako.filter(suoritusjakoFilter(oppijaOid, secret))
        .map(r => r.voimassaAsti)
        .update(Date.valueOf(expirationDate))
    )

    if (updated == 0) KoskiErrorCategory.notFound() else HttpStatus.ok
  }

  private def suoritusjakoFilter(oppijaOid: String, secret: String)(r: SuoritusjakoTable) =
    r.oppijaOid === oppijaOid &&
      r.secret === secret &&
      r.voimassaAsti >= Date.valueOf(LocalDate.now)
}
