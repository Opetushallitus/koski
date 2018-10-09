package fi.oph.koski.oppija

import java.time.LocalDate.now

import com.typesafe.config.Config
import fi.oph.koski.db.GlobalExecutionContext
import fi.oph.koski.henkilo._
import fi.oph.koski.history.OpiskeluoikeusHistoryRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{opiskeluoikeusId, opiskeluoikeusVersio, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, _}
import fi.oph.koski.opiskeluoikeus._
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedotIndexer
import fi.oph.koski.schema._
import fi.oph.koski.util.{Timing, WithWarnings}

class KoskiOppijaFacade(henkilöRepository: HenkilöRepository, henkilöCache: KoskiHenkilöCache, opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository, historyRepository: OpiskeluoikeusHistoryRepository, perustiedotIndexer: OpiskeluoikeudenPerustiedotIndexer, config: Config, hetu: Hetu) extends Logging with Timing with GlobalExecutionContext {
  private lazy val mockOids = config.hasPath("authentication-service.mockOid") && config.getBoolean("authentication-service.mockOid")

  def findOppija(oid: String)(implicit user: KoskiSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    henkilöRepository.findByOid(oid)
      .toRight(notFound(oid))
      .flatMap(henkilö => toOppija(henkilö, opiskeluoikeusRepository.findByOppijaOid(henkilö.oid)))
  }

  def findUserOppija(implicit user: KoskiSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    henkilöRepository.findByOid(user.oid)
      .toRight(notFound(user.oid))
      .flatMap(henkilö => toOppija(henkilö, opiskeluoikeusRepository.findByUserOid(user.oid)))
  }

  def findVersion(oppijaOid: String, opiskeluoikeusOid: String, versionumero: Int)(implicit user: KoskiSession): Either[HttpStatus, Oppija] = {
    opiskeluoikeusRepository.getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid).flatMap {
      case oids if oids.contains(oppijaOid) =>
        historyRepository.findVersion(opiskeluoikeusOid, versionumero).flatMap { history =>
          henkilöRepository.findByOid(oppijaOid)
            .toRight(notFound(oppijaOid))
            .flatMap(henkilö => toOppija(henkilö, WithWarnings(List(history), Nil)))
            .flatMap(_.warningsToLeft)
        }
      case _ =>
        logger(user).warn(s"Yritettiin hakea opiskeluoikeuden $opiskeluoikeusOid versiota $versionumero väärällä oppija-oidilla $oppijaOid")
        Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    }
  }

  def createOrUpdate(oppija: Oppija, allowUpdate: Boolean, allowDeleteCompleted: Boolean = false)(implicit user: KoskiSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val oppijaOid: Either[HttpStatus, PossiblyUnverifiedHenkilöOid] = oppija.henkilö match {
      case h:UusiHenkilö =>
        hetu.validate(h.hetu).right.flatMap { hetu =>
          henkilöRepository.findOrCreate(h).right.map(VerifiedHenkilöOid(_))
        }
      case h:TäydellisetHenkilötiedot if mockOids =>
        Right(VerifiedHenkilöOid(h))
      case h:HenkilöWithOid =>
        Right(UnverifiedHenkilöOid(h.oid, henkilöRepository))
    }

    timed("createOrUpdate") {
      val opiskeluoikeudet: Seq[KoskeenTallennettavaOpiskeluoikeus] = oppija.tallennettavatOpiskeluoikeudet

      oppijaOid.right.flatMap { oppijaOid: PossiblyUnverifiedHenkilöOid =>
        if (oppijaOid.oppijaOid == user.oid) {
          Left(KoskiErrorCategory.forbidden.omienTietojenMuokkaus())
        } else {
          val opiskeluoikeusCreationResults: Seq[Either[HttpStatus, OpiskeluoikeusVersio]] = opiskeluoikeudet.map { opiskeluoikeus =>
            createOrUpdateOpiskeluoikeus(oppijaOid, opiskeluoikeus, allowUpdate, allowDeleteCompleted)
          }

          opiskeluoikeusCreationResults.find(_.isLeft) match {
            case Some(Left(error)) => Left(error)
            case _ => Right(HenkilönOpiskeluoikeusVersiot(OidHenkilö(oppijaOid.oppijaOid), opiskeluoikeusCreationResults.toList.map {
              case Right(r) => r
              case Left(_) => throw new RuntimeException("Unreachable match arm: Left")
            }))
          }
        }
      }
    }
  }

  def invalidate(opiskeluoikeusOid: String, invalidationFn: Oppija => Either[HttpStatus, Oppija], updateFn: Oppija => Either[HttpStatus, HenkilönOpiskeluoikeusVersiot])(implicit user: KoskiSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] =
    opiskeluoikeusRepository.findByOid(opiskeluoikeusOid).flatMap { row =>
      if (!OpiskeluoikeusAccessChecker.isInvalidatable(row.toOpiskeluoikeus, user)) {
        Left(KoskiErrorCategory.forbidden("Mitätöinti ei sallittu"))
      } else {
        findOppija(row.oppijaOid).map(_.getIgnoringWarnings).flatMap(invalidationFn)
      }
    }.flatMap(updateFn)

  def invalidateOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] =
    invalidate(opiskeluoikeusOid, cancelOpiskeluoikeus(opiskeluoikeusOid), oppija => createOrUpdate(oppija, allowUpdate = true))

  def invalidatePäätasonSuoritus(opiskeluoikeusOid: String, päätasonSuoritus: PäätasonSuoritus, versionumero: Int)(implicit user: KoskiSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] =
    invalidate(opiskeluoikeusOid, cancelPäätasonSuoritus(opiskeluoikeusOid, päätasonSuoritus, versionumero), oppija => createOrUpdate(oppija, allowUpdate = true, allowDeleteCompleted = true))

  private def createOrUpdateOpiskeluoikeus(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowUpdate: Boolean, allowDeleteCompleted: Boolean = false)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusVersio] = {
    if (oppijaOid.oppijaOid == user.oid) {
      Left(KoskiErrorCategory.forbidden.omienTietojenMuokkaus())
    } else {
      val result = opiskeluoikeusRepository.createOrUpdate(oppijaOid, opiskeluoikeus, allowUpdate, allowDeleteCompleted)
      result.right.map { (result: CreateOrUpdateResult) =>
        applicationLog(oppijaOid, opiskeluoikeus, result)
        auditLog(oppijaOid, result)
        if (result.changed && opiskeluoikeus.lähdejärjestelmänId.isEmpty) {
          // Currently we don't trigger an immediate update to elasticsearch, as we've a 1 sec poll interval anyway. This is where we would do it.
        }
        OpiskeluoikeusVersio(result.oid, result.versionumero, result.lähdejärjestelmänId)
      }
    }
  }

  private def applicationLog(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, result: CreateOrUpdateResult)(implicit user: KoskiSession): Unit = {
    val verb = result match {
      case updated: Updated =>
        val tila = updated.old.tila.opiskeluoikeusjaksot.last
        if (tila.opiskeluoikeusPäättynyt) {
          s"Päivitetty päättynyt (${tila.tila.koodiarvo})"
        } else {
          "Päivitetty"
        }
      case _: Created => "Luotu"
      case _: NotChanged => "Päivitetty (ei muutoksia)"
    }
    val tutkinto = opiskeluoikeus.suoritukset.map(_.koulutusmoduuli.tunniste).mkString(",")
    val oppilaitos = opiskeluoikeus.getOppilaitos.oid
    logger(user).info(s"${verb} opiskeluoikeus ${result.id} (versio ${result.versionumero}) oppijalle ${oppijaOid} tutkintoon ${tutkinto} oppilaitoksessa ${oppilaitos}")
  }

  private def auditLog(oppijaOid: PossiblyUnverifiedHenkilöOid, result: CreateOrUpdateResult)(implicit user: KoskiSession): Unit = {
    (result match {
      case _: Updated => Some(OPISKELUOIKEUS_MUUTOS)
      case _: Created => Some(OPISKELUOIKEUS_LISAYS)
      case _ => None
    }).foreach { operaatio =>
      AuditLog.log(AuditLogMessage(operaatio, user,
        Map(oppijaHenkiloOid -> oppijaOid.oppijaOid, opiskeluoikeusId -> result.id.toString, opiskeluoikeusVersio -> result.versionumero.toString))
      )
    }
  }

  private def cancelOpiskeluoikeus(opiskeluoikeusOid: String)(oppija: Oppija): Either[HttpStatus, Oppija] = {
    oppija.tallennettavatOpiskeluoikeudet.find(_.oid.exists(_ == opiskeluoikeusOid))
      .toRight(KoskiErrorCategory.notFound())
      .flatMap(invalidated)
      .map(oo => oppija.copy(opiskeluoikeudet = List(oo)))
  }

  private def cancelPäätasonSuoritus(opiskeluoikeusOid: String, päätasonSuoritus: PäätasonSuoritus, versionumero: Int)(oppija: Oppija): Either[HttpStatus, Oppija] = {
    oppija.tallennettavatOpiskeluoikeudet.find(_.oid.exists(_ == opiskeluoikeusOid))
      .toRight(KoskiErrorCategory.notFound())
      .flatMap(oo => oo.versionumero match {
        case Some(v) if v == versionumero => Right(oo)
        case Some(_) => Left(KoskiErrorCategory.conflict.versionumero())
        case _ => Left(KoskiErrorCategory.badRequest())
      })
      .flatMap(withoutPäätasonSuoritus(päätasonSuoritus))
      .map(oo => oppija.copy(opiskeluoikeudet = List(oo)))
  }

  private def invalidated(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, Opiskeluoikeus] = {
    (oo.tila match {
      case t: AmmatillinenOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(now, mitätöity)))
      case t: NuortenPerusopetuksenOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ NuortenPerusopetuksenOpiskeluoikeusjakso(now, mitätöity)))
      case t: AikuistenPerusopetuksenOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ AikuistenPerusopetuksenOpiskeluoikeusjakso(now, mitätöity)))
      case t: LukionOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ LukionOpiskeluoikeusjakso(now, mitätöity)))
      case t: KorkeakoulunOpiskeluoikeudenTila => Left(KoskiErrorCategory.badRequest())
      case t: YlioppilastutkinnonOpiskeluoikeudenTila => Left(KoskiErrorCategory.badRequest())
    }).map(oo.withTila).map(_.withPäättymispäivä(now))
  }

  private def withoutPäätasonSuoritus(päätasonSuoritus: PäätasonSuoritus)(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, Opiskeluoikeus] = {
    if (oo.suoritukset.length == 1) {
      Left(KoskiErrorCategory.forbidden.ainoanPäätasonSuorituksenPoisto())
    } else {
      oo match {
        case _: PerusopetuksenOpiskeluoikeus | _: AikuistenPerusopetuksenOpiskeluoikeus =>
          val poistetullaSuorituksella = oo.suoritukset.filterNot(_ == päätasonSuoritus)
          if (poistetullaSuorituksella.length == oo.suoritukset.length) {
            Left(KoskiErrorCategory.notFound())
          } else if (poistetullaSuorituksella.length != oo.suoritukset.length - 1) {
            Left(KoskiErrorCategory.internalError())
          } else {
            Right(oo.withSuoritukset(poistetullaSuorituksella))
          }
        case _ => Left(KoskiErrorCategory.badRequest())
      }
    }
  }

  private def toOppija(henkilö: TäydellisetHenkilötiedot, opiskeluoikeudet: => WithWarnings[Seq[Opiskeluoikeus]])(implicit user: KoskiSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    opiskeluoikeudet match {
      case WithWarnings(Nil, Nil) => Left(notFound(henkilö.oid))
      case oo: WithWarnings[Seq[Opiskeluoikeus]] =>
        writeViewingEventToAuditLog(user, henkilö.oid)
        Right(oo.map(Oppija(henkilö, _)))
    }
  }

  private def notFound(oid: String) = KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa " + oid + " ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.")

  private def writeViewingEventToAuditLog(user: KoskiSession, oid: Henkilö.Oid): Unit = {
    if (user != KoskiSession.systemUser) { // To prevent health checks from polluting the audit log
      val operation = user match {
        case _ if user.user.kansalainen => KANSALAINEN_OPISKELUOIKEUS_KATSOMINEN
        case _ if user.user.isSuoritusjakoKatsominen => KANSALAINEN_SUORITUSJAKO_KATSOMINEN
        case _ => OPISKELUOIKEUS_KATSOMINEN
      }
      AuditLog.log(AuditLogMessage(operation, user, Map(oppijaHenkiloOid -> oid)))
    }
  }

  private lazy val mitätöity = Koodistokoodiviite("mitatoity", koodistoUri = "koskiopiskeluoikeudentila")
}

case class HenkilönOpiskeluoikeusVersiot(henkilö: OidHenkilö, opiskeluoikeudet: List[OpiskeluoikeusVersio])
case class OpiskeluoikeusVersio(oid: Opiskeluoikeus.Oid, versionumero: Int, lähdejärjestelmänId: Option[LähdejärjestelmäId]) extends Lähdejärjestelmällinen
