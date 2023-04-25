package fi.oph.koski.suoritusjako


import java.time.LocalDate
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.editor.EditorModel
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.{AuditLog, KoskiAuditLogMessage}
import fi.oph.koski.log.KoskiOperation.{KANSALAINEN_SUORITUSJAKO_KATSOMINEN, KANSALAINEN_SUORITUSJAKO_LISAYS}
import fi.oph.koski.log.KoskiAuditLogMessageField.oppijaHenkiloOid
import fi.oph.koski.omattiedot.OmatTiedotEditorModel
import fi.oph.koski.opiskeluoikeus.CompositeOpiskeluoikeusRepository
import fi.oph.koski.oppija.KoskiOppijaFacade
import fi.oph.koski.schema.{Opiskeluoikeus, Oppija, Suoritus}
import fi.oph.koski.util.WithWarnings
import fi.oph.koski.util.ChainingSyntax.chainingOps

class SuoritusjakoServiceV2(suoritusjakoRepositoryV2: SuoritusjakoRepositoryV2, oppijaFacade: KoskiOppijaFacade, henkilöRepository: HenkilöRepository, opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository, application: KoskiApplication) {

  def createSuoritusjako(opiskeluoikeudet: List[Opiskeluoikeus])(implicit user: KoskiSpecificSession): HttpStatus = {
    HttpStatus.foldEithers(opiskeluoikeudet.map(validateIsUsersOpiskeluoikeus))
      .map(opiskeluoikeudet => suoritusjakoRepositoryV2.createSuoritusjako(opiskeluoikeudet))
      .map(ok => {
        AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_LISAYS, user, Map(oppijaHenkiloOid -> user.oid)))
        ok
      })
       .merge
  }

  def findSuoritusjako(secret: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, EditorModel] = {
    SuoritusjakoSecret.validate(secret)
      .flatMap(suoritusjakoRepositoryV2.findBySecret)
      .flatMap { case (oppijaOid, opiskeluoikeudet) =>
        oppijaFacade.findOppijaAndCombineWithOpiskeluoikeudet(oppijaOid, opiskeluoikeudet)
          .tap(_ => AuditLog.log(KoskiAuditLogMessage(KANSALAINEN_SUORITUSJAKO_KATSOMINEN, user, Map(oppijaHenkiloOid -> oppijaOid))))
      }
      .map(oppija => OmatTiedotEditorModel.toEditorModel(oppija, oppija)(application, user))
  }

  def listActivesByUser(user: KoskiSpecificSession): Seq[Suoritusjako] = {
    suoritusjakoRepositoryV2.listActivesByOppijaOid(user.oid)
  }

  def updateExpirationDate(secret: String, voimassaAsti: LocalDate)(implicit user: KoskiSpecificSession): HttpStatus = {
    if (voimassaAsti.isBefore(LocalDate.now) || voimassaAsti.isAfter(LocalDate.now.plusYears(1))) {
      KoskiErrorCategory.badRequest()
    } else {
      suoritusjakoRepositoryV2.updateExpirationDate(user.oid, secret, voimassaAsti)
    }
  }

  def deleteSuoritujako(secret: String)(implicit user: KoskiSpecificSession): HttpStatus = {
    suoritusjakoRepositoryV2.deleteSuoritusjako(user.oid, secret)
  }

  private def validateIsUsersOpiskeluoikeus(jaettuOpiskeluoikeus: Opiskeluoikeus)(implicit user: KoskiSpecificSession): Either[HttpStatus, Opiskeluoikeus] = {
    henkilöRepository.findByOid(user.oid)
      .toRight(KoskiErrorCategory.internalError())
      .map(henkilö => opiskeluoikeusRepository.findByCurrentUser(henkilö))
      .map(opiskeluoikeudetWithWarnings => opiskeluoikeudetWithWarnings.map(_.filter(opiskeluoikeus => isPartOfOriginalOpiskeluoikeus(jaettuOpiskeluoikeus, opiskeluoikeus)).toList))
      .flatMap {
        case WithWarnings(List(validiJaettuOpiskeluoikeus), _) => Right(validiJaettuOpiskeluoikeus)
        case WithWarnings(_, warnings) if warnings.nonEmpty => Left(HttpStatus.fold(warnings))
        case _ => Left(KoskiErrorCategory.badRequest())
      }
  }

  private def isPartOfOriginalOpiskeluoikeus(jaettu: Product, original: Product): Boolean = {
    jaettu.productIterator.zip(original.productIterator).forall {
      case suoritukset(jaetut, kaikki) => jaetut.forall(j => kaikki.exists(k => isPartOfOriginalOpiskeluoikeus(j, k)))
      case suorituksetOption(Some(jaetut), Some(kaikki)) => jaetut.forall((j => kaikki.exists(k => isPartOfOriginalOpiskeluoikeus(j, k))))
      case (None, _) => true
      case (a, b) => a == b
    }
  }

  private def isPartOfOriginalOpiskeluoikeus(x: Any, y: Any): Boolean = (x, y) match {
    case (x: Product, y: Product) => isPartOfOriginalOpiskeluoikeus(x, y)
    case _ => false
  }

  lazy val suoritukset = shapeless.TypeCase[(List[Suoritus], List[Suoritus])]
  lazy val suorituksetOption = shapeless.TypeCase[(Option[List[Suoritus]], Option[List[Suoritus]])]
}
