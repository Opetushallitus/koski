package fi.oph.koski.henkilo

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage}
import fi.oph.koski.opiskeluoikeus.{CompositeOpiskeluoikeusRepository, KoskiOpiskeluoikeusRepository}
import fi.oph.koski.schema.{Henkilö, HenkilötiedotJaOid}

private[henkilo] case class HenkilötiedotFacade(henkilöRepository: HenkilöRepository, kaikkiOpiskeluoikeudet: CompositeOpiskeluoikeusRepository, koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository, hetuValidator: Hetu) {
  def search(query: String)(implicit koskiSession: KoskiSession): HenkilötiedotSearchResponse = {
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, koskiSession, Map(hakuEhto -> query)))
    if (Hetu.validFormat(query).isRight) {
      searchByHetu(query)
    } else if (Henkilö.isValidHenkilöOid(query)) {
      searchByOid(query)
    } else {
      searchHenkilötiedot(query)
    }
  }

  // huom: tässä kutsussa ei ole organisaatiorajausta.
  def findByHetuOrCreateIfInYtrOrVirta(hetu: String)(implicit user: KoskiSession): Either[HttpStatus, List[HenkilötiedotJaOid]] = {
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> hetu)))
    hetuValidator.validate(hetu).right.map(henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(_)).map(_.map(_.toHenkilötiedotJaOid).toList)
  }

  // huom, tässä kutsussa ei ole organisaatiorajausta.
  def findByOid(oid: String)(implicit user: KoskiSession): Either[HttpStatus, List[HenkilötiedotJaOid]] = {
    AuditLog.log(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> oid)))
    HenkilöOid.validateHenkilöOid(oid)
      .map(henkilöRepository.findByOid)
      .map(_.map(_.toHenkilötiedotJaOid).toList)
  }

  // Sisällyttää vain henkilöt, joilta löytyy vähintään yksi (tälle käyttäjälle näkyvä) opiskeluoikeus Koskesta, ei tarkista Virta- eikä YTR-palvelusta
  private def searchHenkilötiedot(queryString: String)(implicit user: KoskiSession): HenkilötiedotSearchResponse = {
    val filtered = koskiOpiskeluoikeudet.filterOppijat(henkilöRepository.findHenkilötiedot(queryString).map(_.toHenkilötiedotJaOid))
    HenkilötiedotSearchResponse(filtered.sortBy(oppija => (oppija.sukunimi, oppija.etunimet)))
  }

  // Sisällyttää vain henkilöt, joilta löytyy vähintään yksi (tälle käyttäjälle näkyvä) opiskeluoikeus Koskesta, YTR:stä tai Virrasta
  private def searchByHetu(hetu: String)(implicit user: KoskiSession): HenkilötiedotSearchResponse = {
    hetuValidator.validate(hetu) match {
      case Right(_) =>
        val kaikkiHenkilöt = henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu, userForAccessChecks = Some(user)).map(_.toHenkilötiedotJaOid)
        val näytettävätHenkilöt = kaikkiOpiskeluoikeudet.filterOppijat(kaikkiHenkilöt.toList)
        val canAddNew = näytettävätHenkilöt.isEmpty && user.hasAnyWriteAccess
        HenkilötiedotSearchResponse(näytettävätHenkilöt, canAddNew, hetu = Some(hetu))
      case Left(status) =>
        HenkilötiedotSearchResponse(Nil, error = status.errorString)
    }
  }

  // Sisällyttää vain henkilöt, joilta löytyy vähintään yksi (tälle käyttäjälle näkyvä) opiskeluoikeus Koskesta, YTR:stä tai Virrasta
  private def searchByOid(oid: String)(implicit user: KoskiSession): HenkilötiedotSearchResponse = {
    val henkilöt = henkilöRepository.findByOid(oid).map(_.toHenkilötiedotJaOid).toList
    val oppijat = kaikkiOpiskeluoikeudet.filterOppijat(henkilöt)
    val canAddNew = henkilöt.nonEmpty && oppijat.isEmpty && user.hasAnyWriteAccess
    HenkilötiedotSearchResponse(oppijat, canAddNew = canAddNew, oid = Some(oid))
  }
}

case class HenkilötiedotSearchResponse(henkilöt: List[HenkilötiedotJaOid], canAddNew: Boolean = false, error: Option[String] = None, hetu: Option[String] = None, oid: Option[String] = None)
