package fi.oph.koski.mydata

import java.sql.Date
import java.sql.Timestamp.{valueOf => timestamp}
import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.db.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.{MyDataJako, MyDataJakoTable}
import fi.oph.koski.db.{KoskiDatabaseMethods, MyDataJakoRow}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.log.Logging

class MyDataRepository(val db: DB) extends Logging with KoskiDatabaseMethods {
  def get(asiakas: String): Either[HttpStatus, MyDataJakoRow] =
    runDbSync(MyDataJako.filter(r => r.asiakas === asiakas && r.voimassaAsti >= Date.valueOf(LocalDate.now)).result.headOption)
      .toRight(KoskiErrorCategory.notFound())

  def getAll(oppijaOid: String): Seq[MyDataJakoRow] = {
    runDbSync(MyDataJako.filter(r => r.oppijaOid === oppijaOid).result)
  }

  def getAllValid(oppijaOid: String): Seq[MyDataJakoRow] = {
    runDbSync(MyDataJako.filter(r => r.oppijaOid === oppijaOid && r.voimassaAsti >= Date.valueOf(LocalDate.now)).result)
  }

  def create(oppijaOid: String, asiakas: String): Boolean = {
    val expirationDate = LocalDateTime.now.plusMonths(12).toLocalDate

    try {
      runDbSync(MyDataJako.insertOrUpdate(MyDataJakoRow(
        asiakas,
        oppijaOid,
        Date.valueOf(expirationDate),
        now
      )))

      true
    } catch {
      case t:Throwable => logger.error(t)(s"Failed to add permissions for ${asiakas} to access student info of ${oppijaOid}")
      false
    }
  }

  def delete(oppijaOid: String, asiakas: String): HttpStatus = {
    val deleted = runDbSync(MyDataJako.filter(myDataJakoFilter(oppijaOid, asiakas)).delete)
    if (deleted == 0) KoskiErrorCategory.notFound() else HttpStatus.ok
  }

  def update(oppijaOid: String, asiakas: String, expirationDate: LocalDate): HttpStatus = {
    val updated = runDbSync(
      MyDataJako.filter(myDataJakoFilter(oppijaOid, asiakas))
        .map(r => r.voimassaAsti)
        .update(Date.valueOf(expirationDate))
    )

    if (updated == 0) KoskiErrorCategory.notFound() else HttpStatus.ok
  }

  private val now = timestamp(LocalDateTime.now)
  private def myDataJakoFilter(oppijaOid: String, asiakas: String)(r: MyDataJakoTable) =
    r.oppijaOid === oppijaOid &&
      r.asiakas === asiakas
}
