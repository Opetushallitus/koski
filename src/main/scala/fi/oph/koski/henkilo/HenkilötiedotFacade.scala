package fi.oph.koski.henkilo

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusRepository
import fi.oph.koski.schema.HenkilötiedotJaOid

case class HenkilötiedotFacade(henkilöRepository: HenkilöRepository, OpiskeluoikeusRepository: OpiskeluoikeusRepository) {
  // Hakee oppijoita kyselyllä. Sisällyttää vain henkilöt, joilta löytyy vähintään yksi opiskeluoikeus, johon käyttäjällä katseluoikeus
  def findHenkilötiedot(queryString: String)(implicit user: KoskiSession): Seq[HenkilötiedotJaOid] = {
    // TODO: ei löydä nimihaulla henkilöitä, joilla on opiskeluoikeuksia vain Koskessa (vs. Virta, YTR)
    val oppijat: List[HenkilötiedotJaOid] = henkilöRepository.findOppijat(queryString)
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> queryString)))
    val filtered = OpiskeluoikeusRepository.filterOppijat(oppijat)
    filtered.sortBy(oppija => (oppija.sukunimi, oppija.etunimet))
  }

  def findByHetu(hetu: String)(implicit user: KoskiSession): Either[HttpStatus, List[HenkilötiedotJaOid]] = {
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> hetu)))
    Hetu.validate(hetu).right.map(henkilöRepository.findOppijat)
  }

  def findByOid(oid: String)(implicit user: KoskiSession): Either[HttpStatus, List[HenkilötiedotJaOid]] = {
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> oid)))
    HenkilöOid.validateHenkilöOid(oid).right.map(henkilöRepository.findOppijat)
  }
}
