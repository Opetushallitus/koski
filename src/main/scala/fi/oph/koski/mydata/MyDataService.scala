package fi.oph.koski.mydata

import java.time.LocalDate

import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.oppijaHenkiloOid
import fi.oph.koski.log.KoskiOperation.KANSALAINEN_MYDATA_LISAYS
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.util.WithWarnings

class MyDataService(myDataRepository: MyDataRepository) extends Logging {
  def put(oppijaOid: String, asiakas: String)(implicit koskiSession: KoskiSession) = {
    myDataRepository.create(oppijaOid, asiakas)
    AuditLog.log(AuditLogMessage(KANSALAINEN_MYDATA_LISAYS, koskiSession, Map(oppijaHenkiloOid -> oppijaOid)))
  }

  def delete(oppijaOid: String, asiakas: String): HttpStatus = {
    myDataRepository.delete(oppijaOid, asiakas)
  }

  def update(oppijaOid: String, secret: String, expirationDate: LocalDate): HttpStatus = {
    if (expirationDate.isBefore(LocalDate.now) || expirationDate.isAfter(LocalDate.now.plusYears(1))) {
      KoskiErrorCategory.badRequest()
    } else {
      myDataRepository.update(oppijaOid, secret, expirationDate)
    }
  }

  def getAll(oppijaOid: String): Seq[MyDataJakoItem] = {
    myDataRepository.getAll(oppijaOid).map(jako => MyDataJakoItem(jako.asiakas, jako.voimassaAsti.toLocalDate))
  }

}
