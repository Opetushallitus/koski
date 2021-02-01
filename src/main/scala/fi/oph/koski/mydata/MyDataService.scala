package fi.oph.koski.mydata

import fi.oph.common.log.Logging
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{omaDataKumppani, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_MYDATA_LISAYS, KANSALAINEN_MYDATA_POISTO}
import fi.oph.koski.log.{AuditLog, AuditLogMessage}

class MyDataService(myDataRepository: MyDataRepository, val application: KoskiApplication) extends Logging with MyDataConfig {
  def put(asiakas: String, koskiSession: KoskiSession): Boolean = {
    def permissionAdded = myDataRepository.create(koskiSession.oid, asiakas)

    if (permissionAdded) {
      AuditLog.log(AuditLogMessage(KANSALAINEN_MYDATA_LISAYS, koskiSession, Map(
        oppijaHenkiloOid -> koskiSession.oid,
        omaDataKumppani -> asiakas
      )))
    }
    permissionAdded
  }

  def delete(asiakas: String, koskiSession: KoskiSession): HttpStatus = {
    val permissionDeleted = myDataRepository.delete(koskiSession.oid, asiakas)

    if (permissionDeleted == HttpStatus.ok) {
      AuditLog.log(AuditLogMessage(KANSALAINEN_MYDATA_POISTO, koskiSession, Map(
        oppijaHenkiloOid -> koskiSession.oid,
        omaDataKumppani -> asiakas
      )))
    }
    permissionDeleted
  }

  def getAll(oppijaOid: String): Seq[MyDataJakoItem] = {
    myDataRepository.getAll(oppijaOid).map(jako => MyDataJakoItem(jako.asiakas, getAsiakasName(jako.asiakas), jako.voimassaAsti.toLocalDate, jako.aikaleima, getAsiakasPurpose(jako.asiakas)))
  }

  def getAllValid(oppijaOid: String): Seq[MyDataJakoItem] = {
    myDataRepository.getAllValid(oppijaOid).map(jako => MyDataJakoItem(jako.asiakas, getAsiakasName(jako.asiakas), jako.voimassaAsti.toLocalDate, jako.aikaleima, getAsiakasPurpose(jako.asiakas)))
  }

  def hasAuthorizedMember(oppijaOid: String, memberId: String): Boolean = {
    getAllValid(oppijaOid).exists(auth => memberId == auth.asiakasId)
  }

  private def getAsiakasName(id: String): String = getConfigForMember(id).getString("name")
  private def getAsiakasPurpose(id: String): String = getConfigForMember(id).getString("purpose")
}
