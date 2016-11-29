package fi.oph.koski.koski

import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage}
import fi.oph.koski.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.schema.HenkilötiedotJaOid

case class HenkilötiedotFacade(oppijaRepository: OppijaRepository, opiskeluOikeusRepository: OpiskeluOikeusRepository) {
  def findHenkilötiedot(queryString: String)(implicit user: KoskiSession): Seq[HenkilötiedotJaOid] = {
    val oppijat: List[HenkilötiedotJaOid] = oppijaRepository.findOppijat(queryString)
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> queryString)))
    val filtered = opiskeluOikeusRepository.filterOppijat(oppijat)
    filtered.sortBy(oppija => (oppija.sukunimi, oppija.etunimet))
  }
}
