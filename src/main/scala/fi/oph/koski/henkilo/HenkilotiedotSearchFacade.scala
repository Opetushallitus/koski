package fi.oph.koski.henkilo

import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.KoskiAuditLogMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.opiskeluoikeus.{CompositeOpiskeluoikeusRepository, KoskiOpiskeluoikeusRepository}
import fi.oph.koski.schema.{Henkilö, HenkilötiedotJaOid}
import fi.oph.koski.util.Timing

private[henkilo] case class HenkilötiedotSearchFacade(henkilöRepository: HenkilöRepository, kaikkiOpiskeluoikeudet: CompositeOpiskeluoikeusRepository, koskiOpiskeluoikeudet: KoskiOpiskeluoikeusRepository, hetuValidator: Hetu) extends Timing {
  def searchOrPossiblyCreateIfInYtrOrVirta(query: String)(implicit koskiSession: KoskiSpecificSession): HenkilötiedotSearchResponse = {
    AuditLog.log(KoskiAuditLogMessage(OPPIJA_HAKU, koskiSession, Map(hakuEhto -> query)))
    if (Hetu.validFormat(query).isRight) {
      searchByHetuOrPossiblyCreateIfInYtrOrVirta(query)
    } else if (Henkilö.isValidHenkilöOid(query)) {
      searchMasterByOid(query)
    } else {
      searchHenkilötiedot(query)
    }
  }

  // huom: tässä kutsussa ei ole organisaatiorajausta.
  def findByHetuOrCreateIfInYtrOrVirta(hetu: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, List[HenkilötiedotJaOid]] = {
    AuditLog.log(KoskiAuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> hetu)))
    hetuValidator.validate(hetu).right.map(henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(_)).map(_.map(_.toHenkilötiedotJaOid).toList)
  }

  // huom, tässä kutsussa ei ole organisaatiorajausta.
  def findByOid(oid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, List[HenkilötiedotJaOid]] = {
    AuditLog.log(KoskiAuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> oid)))
    HenkilöOid.validateHenkilöOid(oid)
      .map(henkilöRepository.findByOid(_))
      .map(_.map(_.toHenkilötiedotJaOid).toList)
  }

  // Sisällyttää vain henkilöt, joilta löytyy vähintään yksi (tälle käyttäjälle näkyvä) opiskeluoikeus Koskesta, ei tarkista Virta- eikä YTR-palvelusta
  private def searchHenkilötiedot(queryString: String)(implicit user: KoskiSpecificSession): HenkilötiedotSearchResponse = {
    val filtered = timed("searchHenkilötiedot:koskiOpiskeluoikeudet.filterOppijat", 0) {
      koskiOpiskeluoikeudet.filterOppijat(henkilöRepository.findByOids(queryString)).map(_.toHenkilötiedotJaOid)
    }
    HenkilötiedotSearchResponse(filtered.sorted(HenkilötiedotJaOid.orderingByName))
  }

  // Sisällyttää vain henkilöt, joilta löytyy vähintään yksi (tälle käyttäjälle näkyvä) opiskeluoikeus Koskesta, YTR:stä tai Virrasta
  private def searchByHetuOrPossiblyCreateIfInYtrOrVirta(hetu: String)(implicit user: KoskiSpecificSession): HenkilötiedotSearchResponse = {
    hetuValidator.validate(hetu) match {
      case Right(_) =>
        val kaikkiHenkilöt = timed("searchByHetuOrPossiblyCreateIfInYtrOrVirta:henkilöRepository.findByHetuOrCreateIfInYtrOrVirta", 0) {
          henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu, userForAccessChecks = Some(user)).toList
        }
        val näytettävätHenkilöt = timed("searchByHetuOrPossiblyCreateIfInYtrOrVirta:kaikkiOpiskeluoikeudet.filterOppijat", 0) {
          kaikkiOpiskeluoikeudet.filterOppijat(kaikkiHenkilöt).map(_.toHenkilötiedotJaOid)
        }
        val canAddNew = näytettävätHenkilöt.isEmpty && user.hasAnyWriteAccess
        HenkilötiedotSearchResponse(näytettävätHenkilöt, canAddNew, hetu = Some(hetu))
      case Left(status) =>
        HenkilötiedotSearchResponse(Nil, error = status.errorString)
    }
  }

  // Sisällyttää vain henkilöt, joilta löytyy vähintään yksi (tälle käyttäjälle näkyvä) opiskeluoikeus Koskesta, YTR:stä tai Virrasta
  private def searchMasterByOid(oid: String)(implicit user: KoskiSpecificSession): HenkilötiedotSearchResponse = {
    val henkilöt = timed("searchMasterByOid:henkilöRepository.findByOid", 0) {
      henkilöRepository.findByOid(oid, findMasterIfSlaveOid = true).toList
    }
    val oppijat = timed("searchMasterByOid:kaikkiOpiskeluoikeudet.filterOppijat", 0) {
      kaikkiOpiskeluoikeudet.filterOppijat(henkilöt).map(_.toHenkilötiedotJaOid)
    }
    val canAddNew = henkilöt.nonEmpty && oppijat.isEmpty && user.hasAnyWriteAccess
    HenkilötiedotSearchResponse(oppijat, canAddNew = canAddNew, oid = Some(oid))
  }
}

case class HenkilötiedotSearchResponse(henkilöt: List[HenkilötiedotJaOid], canAddNew: Boolean = false, error: Option[String] = None, hetu: Option[String] = None, oid: Option[String] = None)
