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
import fi.oph.koski.perustiedot.{OpiskeluoikeudenPerustiedot, OpiskeluoikeudenPerustiedotIndexer}
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing

class KoskiOppijaFacade(henkilöRepository: HenkilöRepository, henkilöCache: KoskiHenkilöCache, opiskeluoikeusRepository: OpiskeluoikeusRepository, historyRepository: OpiskeluoikeusHistoryRepository, perustiedotIndexer: OpiskeluoikeudenPerustiedotIndexer, config: Config) extends Logging with Timing with GlobalExecutionContext {
  private lazy val mockOids = config.hasPath("authentication-service.mockOid") && config.getBoolean("authentication-service.mockOid")
  def findOppija(oid: String)(implicit user: KoskiSession): Either[HttpStatus, Oppija] = toOppija(oid, opiskeluoikeusRepository.findByOppijaOid(oid))

  def findVersion(oppijaOid: String, opiskeluoikeusOid: String, versionumero: Int)(implicit user: KoskiSession): Either[HttpStatus, Oppija] = {
    opiskeluoikeusRepository.getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid).right.flatMap {
      case oids if oids.contains(oppijaOid) =>
        historyRepository.findVersion(opiskeluoikeusOid, versionumero).right.flatMap { history =>
          toOppija(oppijaOid, List(history))
        }
      case _ =>
        logger(user).warn(s"Yritettiin hakea opiskeluoikeuden $opiskeluoikeusOid versiota $versionumero väärällä oppija-oidilla $oppijaOid")
        Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    }
  }

  def findUserOppija(implicit user: KoskiSession): Either[HttpStatus, Oppija] = toOppija(user.oid, opiskeluoikeusRepository.findByUserOid(user.oid))

  def createOrUpdate(oppija: Oppija, allowUpdate: Boolean)(implicit user: KoskiSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val oppijaOid: Either[HttpStatus, PossiblyUnverifiedHenkilöOid] = oppija.henkilö match {
      case h:UusiHenkilö =>
        Hetu.validate(h.hetu, acceptSynthetic = false).right.flatMap { hetu =>
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
            createOrUpdateOpiskeluoikeus(oppijaOid, opiskeluoikeus, allowUpdate)
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

  def invalidateOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] =
    opiskeluoikeusRepository.findByOid(opiskeluoikeusOid).flatMap { row =>
      findOppija(row.oppijaOid).flatMap(cancelOpiskeluoikeus(opiskeluoikeusOid))
    }.flatMap(oppija => createOrUpdate(oppija, allowUpdate = true))

  private def createOrUpdateOpiskeluoikeus(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, allowUpdate: Boolean)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusVersio] = {
    if (oppijaOid.oppijaOid == user.oid) {
      Left(KoskiErrorCategory.forbidden.omienTietojenMuokkaus())
    } else {
      val result = opiskeluoikeusRepository.createOrUpdate(oppijaOid, opiskeluoikeus, allowUpdate)
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

  private def invalidated(oo: KoskeenTallennettavaOpiskeluoikeus): Either[HttpStatus, Opiskeluoikeus] = {
    (oo.tila match {
      case a: AmmatillinenOpiskeluoikeudenTila =>
        Right(a.copy(opiskeluoikeusjaksot = a.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(now, mitätöity)))
      case p: NuortenPerusopetuksenOpiskeluoikeudenTila =>
        Right(p.copy(opiskeluoikeusjaksot = p.opiskeluoikeusjaksot :+ NuortenPerusopetuksenOpiskeluoikeusjakso(now, mitätöity)))
      case l: LukionOpiskeluoikeudenTila =>
        Right(l.copy(opiskeluoikeusjaksot = l.opiskeluoikeusjaksot :+ LukionOpiskeluoikeusjakso(now, mitätöity)))
      case _ => Left(KoskiErrorCategory.badRequest())
    }).map(oo.withTila).map(_.withPäättymispäivä(now))
  }

  // Hakee oppijan oppijanumerorekisteristä ja liittää siihen opiskeluoikeudet. Opiskeluoikeudet haetaan vain, jos oppija löytyy.
  private def toOppija(oid: Henkilö.Oid, opiskeluoikeudet: => Seq[Opiskeluoikeus])(implicit user: KoskiSession): Either[HttpStatus, Oppija] = {
    def notFound = Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa " + oid + " ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
    henkilöRepository.findByOid(oid) match {
      case Some(oppija) =>
        opiskeluoikeudet match {
          case Nil => notFound
          case opiskeluoikeudet: Seq[Opiskeluoikeus] =>
            writeViewingEventToAuditLog(user, oid)
            Right(Oppija(oppija, opiskeluoikeudet))
        }
      case None => notFound
    }
  }

  private def writeViewingEventToAuditLog(user: KoskiSession, oid: Henkilö.Oid): Unit = {
    if (user != KoskiSession.systemUser) { // To prevent health checks from polluting the audit log
      val operation = if (user.user.kansalainen) KANSALAINEN_OPISKELUOIKEUS_KATSOMINEN else OPISKELUOIKEUS_KATSOMINEN
      AuditLog.log(AuditLogMessage(operation, user, Map(oppijaHenkiloOid -> oid)))
    }
  }

  private lazy val mitätöity = Koodistokoodiviite("mitatoity", koodistoUri = "koskiopiskeluoikeudentila")
}

case class HenkilönOpiskeluoikeusVersiot(henkilö: OidHenkilö, opiskeluoikeudet: List[OpiskeluoikeusVersio])
case class OpiskeluoikeusVersio(oid: Opiskeluoikeus.Oid, versionumero: Int, lähdejärjestelmänId: Option[LähdejärjestelmäId]) extends Lähdejärjestelmällinen
