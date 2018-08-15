package fi.oph.koski.mydata

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.oppijaHenkiloOid
import fi.oph.koski.log.KoskiOperation.KANSALAINEN_MYDATA_LISAYS
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}

class MyDataService(myDataRepository: MyDataRepository, implicit val application: KoskiApplication) extends Logging with MyDataConfig {
  def put(oppijaOid: String, asiakas: String)(implicit koskiSession: KoskiSession): Boolean = {
    def permissionAdded = myDataRepository.create(oppijaOid, asiakas)
    if (permissionAdded) {
      AuditLog.log(AuditLogMessage(KANSALAINEN_MYDATA_LISAYS, koskiSession, Map(oppijaHenkiloOid -> oppijaOid)))
    }
    permissionAdded
  }

  def delete(oppijaOid: String, asiakas: String): HttpStatus = {
    myDataRepository.delete(oppijaOid, asiakas)
  }

  def update(oppijaOid: String, asiakas: String, expirationDate: LocalDate): HttpStatus = {
    if (expirationDate.isBefore(LocalDate.now) || expirationDate.isAfter(LocalDate.now.plusYears(1))) {
      KoskiErrorCategory.badRequest()
    } else {
      myDataRepository.update(oppijaOid, asiakas, expirationDate)
    }
  }

  def getAll(oppijaOid: String): Seq[MyDataJakoItem] = {
    myDataRepository.getAll(oppijaOid).map(jako => MyDataJakoItem(jako.asiakas, getAsiakasName(jako.asiakas), jako.voimassaAsti.toLocalDate, jako.aikaleima))
  }

  def getAllValid(oppijaOid: String): Seq[MyDataJakoItem] = {
    myDataRepository.getAllValid(oppijaOid).map(jako => MyDataJakoItem(jako.asiakas, getAsiakasName(jako.asiakas), jako.voimassaAsti.toLocalDate, jako.aikaleima))
  }

  def hasAuthorizedMember(oppijaOid: String, memberId: String): Boolean = {
    getAllValid(oppijaOid).exists(auth => memberId == auth.asiakasId)
  }

  private def getAsiakasName(id: String): String = getConfigForMember(id).getString("name")
}
