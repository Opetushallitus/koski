package fi.oph.koski.henkilo

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage}
import fi.oph.koski.opiskeluoikeus.{CompositeOpiskeluoikeusRepository, OpiskeluoikeusRepository}
import fi.oph.koski.schema.HenkilötiedotJaOid

case class HenkilötiedotFacade(henkilöRepository: HenkilöRepository, kaikkiOpiskeluoikeudet: CompositeOpiskeluoikeusRepository, koskiOpiskeluoikeudet: OpiskeluoikeusRepository) {
  def search(query: String)(implicit koskiSession: KoskiSession): HenkilötiedotSearchResponse = {
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, koskiSession, Map(hakuEhto -> query)))
    if (Hetu.validFormat(query).isRight) {
      searchByHetu(query)
    } else if (HenkilöOid.isValidHenkilöOid(query)) {
      searchByOid(query)
    } else {
      searchHenkilötiedot(query)
    }
  }

  def findByHetu(hetu: String)(implicit user: KoskiSession): Either[HttpStatus, List[HenkilötiedotJaOid]] = {
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> hetu)))
    Hetu.validate(hetu).right.map(henkilöRepository.findOppijat)
  }

  def findByOid(oid: String)(implicit user: KoskiSession): Either[HttpStatus, List[HenkilötiedotJaOid]] = {
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> oid)))
    HenkilöOid.validateHenkilöOid(oid).right.map(henkilöRepository.findOppijat)
  }

  // Sisällyttää vain henkilöt, joilta löytyy vähintään yksi opiskeluoikeus koskesta, ei tarkista virta- eikä ytr-palvelusta
  private def searchHenkilötiedot(queryString: String)(implicit user: KoskiSession): HenkilötiedotSearchResponse = {
    val filtered = koskiOpiskeluoikeudet.filterOppijat(henkilöRepository.findOppijat(queryString))
    HenkilötiedotSearchResponse(filtered.sortBy(oppija => (oppija.sukunimi, oppija.etunimet)))
  }

  // Sisällyttää vain henkilöt, joilta löytyy vähintään yksi opiskeluoikeus koskesta, ytr:stä tai virrasta
  private def searchByHetu(hetu: String)(implicit user: KoskiSession): HenkilötiedotSearchResponse = {
    val henkilöt = kaikkiOpiskeluoikeudet.filterOppijat(henkilöRepository.findOppijat(hetu))
    Hetu.validate(hetu) match {
      case Right(_) =>
        val canAddNew = henkilöt.isEmpty && user.hasAnyWriteAccess
        HenkilötiedotSearchResponse(henkilöt, canAddNew, hetu = Some(hetu))
      case Left(status) =>
        henkilöt match {
          case Nil =>
            HenkilötiedotSearchResponse(henkilöt, error = status.errorString) // TODO: i18n for error messages here
          case _ =>
            HenkilötiedotSearchResponse(henkilöt)
        }
    }
  }

  // Sisällyttää vain henkilöt, joilta löytyy vähintään yksi opiskeluoikeus koskesta, ytr:stä tai virrasta
  private def searchByOid(oid: String)(implicit user: KoskiSession): HenkilötiedotSearchResponse = {
    val henkilöt = henkilöRepository.findOppijat(oid)
    val oppijat = kaikkiOpiskeluoikeudet.filterOppijat(henkilöt)
    val canAddNew = henkilöt.nonEmpty && oppijat.isEmpty && user.hasAnyWriteAccess
    HenkilötiedotSearchResponse(oppijat, canAddNew = canAddNew, oid = Some(oid))
  }
}

case class HenkilötiedotSearchResponse(henkilöt: List[HenkilötiedotJaOid], canAddNew: Boolean = false, error: Option[String] = None, hetu: Option[String] = None, oid: Option[String] = None)
