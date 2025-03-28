package fi.oph.koski.suoritusjako

import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate, LocalDateTime}
import fi.oph.koski.db.{DB, KoskiTables, QueryMethods, SuoritusjakoRow}
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiTables.{SuoritusJako, SuoritusjakoTable}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.Logging

class SuoritusjakoRepository(val db: DB) extends Logging with QueryMethods {
  def get(secret: String): Either[HttpStatus, SuoritusjakoRow] =
    runDbSync(SuoritusJako.filter(r => r.secret === secret && r.voimassaAsti >= Date.valueOf(LocalDate.now)).result.headOption)
      .toRight(KoskiErrorCategory.notFound())

  def getAll(oppijaOid: String): Seq[SuoritusjakoRow] = {
    runDbSync(SuoritusJako.filter(r => r.oppijaOid === oppijaOid && r.voimassaAsti >= Date.valueOf(LocalDate.now)).result)
  }

  def create(secret: String, oppijaOid: String, suoritusIds: List[SuoritusIdentifier], kokonaisuudet: List[SuoritusjakoPayload]): Either[HttpStatus, Suoritusjako] = {
    val expirationDate = LocalDate.now.plusMonths(6)
    val maxSuoritusjakoCount = 100
    val timestamp = Timestamp.from(Instant.now())

    val currentSuoritusjakoCount = runDbSync(SuoritusJako
      .filter(r => r.oppijaOid === oppijaOid && r.voimassaAsti >= Date.valueOf(LocalDate.now)).length.result
    )

    if (currentSuoritusjakoCount < maxSuoritusjakoCount) {
      val row = runDbSync(SuoritusJako
        .returning(SuoritusJako)
        .insertOrUpdate(SuoritusjakoRow(
          0,
          secret,
          oppijaOid,
          JsonSerializer.serializeWithRoot(suoritusIds),
          JsonSerializer.serializeWithRoot(kokonaisuudet),
          Date.valueOf(expirationDate),
          timestamp
        ))
      )

      row.map(x => Suoritusjako(x.secret, x.voimassaAsti.toLocalDate, x.aikaleima, x.jaonTyyppi)).toRight(KoskiErrorCategory.notFound())
    } else {
      Left(KoskiErrorCategory.forbidden.liianMontaSuoritusjakoa())
    }
  }

  def delete(oppijaOid: String, secret: String): HttpStatus = {
    val deleted = runDbSync(SuoritusJako.filter(suoritusjakoFilter(oppijaOid, secret)).delete)
    if (deleted == 0) KoskiErrorCategory.notFound() else HttpStatus.ok
  }

  def deleteAllForOppija(oppijaOid: String): HttpStatus = {
    val deleted = runDbSync(SuoritusJako.filter(_.oppijaOid === oppijaOid).delete)
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


  def updateAikaleimaForTest(secret: String, timestamp: Timestamp) = {
    runDbSync(
      SuoritusJako.filter(_.secret === secret).map(_.aikaleima).update(timestamp)
    )
  }
}
