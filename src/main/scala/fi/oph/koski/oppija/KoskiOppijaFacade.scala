package fi.oph.koski.oppija

import com.typesafe.config.Config
import fi.oph.koski.henkilo._
import fi.oph.koski.history.{KoskiOpiskeluoikeusHistoryRepository, YtrOpiskeluoikeusHistoryRepository}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.KoskiAuditLogMessageField.{opiskeluoikeusId, opiskeluoikeusVersio, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log._
import fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus._
import fi.oph.koski.opiskeluoikeus._
import fi.oph.koski.schema.PerusopetuksenOpiskeluoikeus.{käyttäytymisenArviointiTraversal, nuortenPerusopetuksenPakollistenOppiaineidenLaajuudetTraversal, oppimääränArvioinnitTraversal, päätasonSuorituksetTraversal}
import fi.oph.koski.schema._
import fi.oph.koski.util.{Timing, WithWarnings}
import fi.oph.koski.validation.KoskiGlobaaliValidator
import mojave.{Traversal, traversal}
import mojave._

import java.time.LocalDate

class KoskiOppijaFacade(
  henkilöRepository: HenkilöRepository,
  opiskeluoikeusRepository: CompositeOpiskeluoikeusRepository,
  ytrDownloadedOpiskeluoikeusRepository: YtrSavedOpiskeluoikeusRepository,
  historyRepository: KoskiOpiskeluoikeusHistoryRepository,
  ytrHistoryRepository: YtrOpiskeluoikeusHistoryRepository,
  globaaliValidator: KoskiGlobaaliValidator,
  config: Config,
  hetu: Hetu
) extends Logging with Timing {

  private val usingMockOids =
    config.hasPath("authentication-service.mockOid") &&
      config.getBoolean("authentication-service.mockOid")

  def findOppija
    (oid: String, findMasterIfSlaveOid: Boolean = false, useVirta: Boolean = true, useYtr: Boolean = true)
    (implicit user: KoskiSpecificSession)
  : Either[HttpStatus, WithWarnings[Oppija]] = {
    henkilöRepository.findByOid(oid, findMasterIfSlaveOid)
      .toRight(notFound(oid))
      .flatMap(henkilö => toOppija(henkilö, opiskeluoikeusRepository.findByOppija(henkilö, useVirta, useYtr)))
  }

  def findOppijaAndCombineWithOpiskeluoikeudet(
    oid: String,
    opiskeluoikeudet: Seq[Opiskeluoikeus],
    findMasterIfSlaveOid: Boolean = false
  )(implicit user: KoskiSpecificSession)
  : Either[HttpStatus, WithWarnings[Oppija]] = {
    henkilöRepository.findByOid(oid, findMasterIfSlaveOid)
      .toRight(notFound(oid))
      .flatMap(henkilö => toOppija(henkilö, WithWarnings(opiskeluoikeudet, Nil)))
  }

  // Palauttaa ainoastaan oppijan YTR:stä ladatut opiskeluoikeudet
  def findYtrDownloadedOppija
    (oid: String, findMasterIfSlaveOid: Boolean = false)
    (implicit user: KoskiSpecificSession)
  : Either[HttpStatus, WithWarnings[Oppija]] =
    henkilöRepository.findByOid(oid, findMasterIfSlaveOid)
      .toRight(notFound(oid))
      .flatMap(henkilö => toOppija(
        henkilö,
        WithWarnings(
          ytrDownloadedOpiskeluoikeusRepository.findByOppijaOids(henkilö.oid :: henkilö.linkitetytOidit),
          Seq.empty
        )
      ))

  def findYtrDownloadedOppijaVersionumerolla
    (oid: String, versionumero: Int, findMasterIfSlaveOid: Boolean = false)
      (implicit user: KoskiSpecificSession)
  : Either[HttpStatus, WithWarnings[Oppija]] = {
    val oppija = findYtrDownloadedOppija(oid, findMasterIfSlaveOid) match {
      case Left(status) => Left(status)
      case Right(WithWarnings(oppija, _)) =>
        val historiaOot = oppija.opiskeluoikeudet.flatMap(
          oo => ytrHistoryRepository.findVersion(oo.oid.get, versionumero).toOption
        )

        val uusiOppija = oppija.copy(
          opiskeluoikeudet = historiaOot
        )

        if (uusiOppija.opiskeluoikeudet.length == oppija.opiskeluoikeudet.length) {
          Right(WithWarnings(uusiOppija, Seq.empty))
        } else {
          Left(KoskiErrorCategory.notFound("Historiaversiota ei löydy"))
        }
      case _ =>
        Left(KoskiErrorCategory.notFound("Historiaversiota ei löydy"))
    }
    oppija.map(piilotaOppijanTietojaTarvittaessa)
  }

  def findOppijaHenkilö
    (oid: String, findMasterIfSlaveOid: Boolean = false, useVirta: Boolean = true, useYtr: Boolean = true)
    (implicit user: KoskiSpecificSession)
  : Either[HttpStatus, WithWarnings[OppijaYksilöintitiedolla]] = {
    henkilöRepository.findByOid(oid, findMasterIfSlaveOid)
      .toRight(notFound(oid))
      .flatMap(henkilö => withOpiskeluoikeudet(henkilö, opiskeluoikeusRepository.findByOppija(henkilö, useVirta, useYtr)))
      .map(_.map {
        case (henkilö, opiskeluoikeudet) =>
          OppijaYksilöintitiedolla(
            piilotaOppijanTietojaTarvittaessa(
              Oppija(henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö), opiskeluoikeudet)
            ),
            henkilö.yksilöity
          )
      })
  }

  def findUserOppija(implicit user: KoskiSpecificSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    henkilöRepository.findByOid(user.oid)
      .toRight(notFound(user.oid))
      .flatMap(henkilö => toOppija(henkilö, opiskeluoikeusRepository.findByCurrentUser(henkilö)))
  }

  def findHuollettavaOppija(oid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, WithWarnings[Oppija]] = {
    henkilöRepository.findByOid(oid)
      .toRight(notFound(oid))
      .flatMap(henkilö => toOppija(henkilö, opiskeluoikeusRepository.findHuollettavaByOppija(henkilö)))
  }

  def findOppijaByHetuOrCreateIfInYtrOrVirta
    (hetu: String, useVirta: Boolean = true, useYtr: Boolean = true)
    (implicit user: KoskiSpecificSession)
  : Either[HttpStatus, WithWarnings[Oppija]] = {
    val henkilö = if (useVirta || useYtr) {
      henkilöRepository.findByHetuOrCreateIfInYtrOrVirta(hetu, userForAccessChecks = Some(user))
    } else {
      henkilöRepository.opintopolku.findByHetu(hetu)
    }
    henkilö
      .toRight(notFound("(hetu)"))
      .flatMap(henkilö =>
        toOppija(
          henkilö,
          opiskeluoikeusRepository.findByOppija(henkilö, useVirta = useVirta, useYtr = useYtr),
          _ => "(hetu)"
        )
      )
  }

  def findVersion
    (oppijaOid: String, opiskeluoikeusOid: String, versionumero: Int)
    (implicit user: KoskiSpecificSession)
  : Either[HttpStatus, OppijaYksilöintitiedolla] = {
    opiskeluoikeusRepository.getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid).flatMap {
      case oids: List[Henkilö.Oid] if oids.contains(oppijaOid) =>
        historyRepository.findVersion(opiskeluoikeusOid, versionumero).flatMap { history =>
          henkilöRepository.findByOid(oppijaOid)
            .toRight(notFound(oppijaOid))
            .flatMap(henkilö => withOpiskeluoikeudet(henkilö, WithWarnings(List(history), Nil)))
            .flatMap(_.warningsToLeft)
            .map {
              case (henkilö, opiskeluoikeudet) =>
                OppijaYksilöintitiedolla(
                  piilotaOppijanTietojaTarvittaessa(
                    Oppija(henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö), opiskeluoikeudet)
                  ),
                  henkilö.yksilöity
                )
            }
        }
      case _ =>
        logger(user).warn(s"Yritettiin hakea opiskeluoikeuden $opiskeluoikeusOid versiota $versionumero väärällä oppija-oidilla $oppijaOid")
        Left(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    }
  }

  def findVersion
    (opiskeluoikeusOid: String, versionumero: Int)
      (implicit user: KoskiSpecificSession)
  : Either[HttpStatus, OppijaYksilöintitiedolla] = {
    opiskeluoikeusRepository.getMasterOppijaOidForOpiskeluoikeus(opiskeluoikeusOid).flatMap( oppijaOid => {
      historyRepository.findVersion(opiskeluoikeusOid, versionumero).flatMap { history =>
        henkilöRepository.findByOid(oppijaOid)
          .toRight(notFound(oppijaOid))
          .flatMap(henkilö => withOpiskeluoikeudet(henkilö, WithWarnings(List(history), Nil)))
          .flatMap(_.warningsToLeft)
          .map {
            case (henkilö, opiskeluoikeudet) =>
              OppijaYksilöintitiedolla(
                piilotaOppijanTietojaTarvittaessa(
                  Oppija(henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö), opiskeluoikeudet)
                ),
                henkilö.yksilöity
              )
          }
      }
    })
  }

  def createOrUpdate(
    oppija: Oppija,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean = false
  )(implicit user: KoskiSpecificSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val oppijaOid: Either[HttpStatus, PossiblyUnverifiedHenkilöOid] = oppija.henkilö match {
      case h: UusiHenkilö =>
        hetu.validate(h.hetu).right.flatMap { hetu =>
          henkilöRepository.findOrCreate(h).right.map(VerifiedHenkilöOid)
        }
      case h: TäydellisetHenkilötiedot if usingMockOids =>
        Right(VerifiedHenkilöOid(RemoteOpintopolkuHenkilöFacadeWithMockOids.oppijaWithMockOid(h)))
      case h: HenkilöWithOid =>
        Right(UnverifiedHenkilöOid(h.oid, henkilöRepository))
    }

    val maksuttomuusCheck = oppijaOid.map(_.verified).flatMap {
      case Some(henkilö) =>
        val henkilöMaster = henkilöRepository.findByOid(henkilö.oid, findMasterIfSlaveOid = true)
        val validation = HttpStatus.fold(oppija.tallennettavatOpiskeluoikeudet.map(opiskeluoikeus => {
          globaaliValidator.validateOpiskeluoikeus(
            opiskeluoikeus,
            henkilöMaster,
            henkilö.oid
          )
        }))
        if (validation.isOk) Right(Unit) else Left(validation)
      case None => Left(KoskiErrorCategory.notFound.oppijaaEiLöydy("Oppijaa " + oppijaOid.right.get.oppijaOid + " ei löydy."))
    }

    maksuttomuusCheck.flatMap { _ => timed("createOrUpdate", 250) {
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
            case _ => Right(HenkilönOpiskeluoikeusVersiot(
              OidHenkilö(oppijaOid.oppijaOid),
              opiskeluoikeusCreationResults.toList.map {
                case Right(r) => r
                case Left(_) => throw new RuntimeException("Unreachable match arm: Left")
              }
            ))
          }
        }
      }
    }}
  }

  private def invalidate(
    opiskeluoikeusOid: String,
    invalidationFn: Oppija => Either[HttpStatus, Oppija],
    updateFn: Oppija => Either[HttpStatus, HenkilönOpiskeluoikeusVersiot]
  )(implicit user: KoskiSpecificSession)
  : Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] =
    opiskeluoikeusRepository.findByOid(opiskeluoikeusOid).flatMap { row =>
      if (!OpiskeluoikeusAccessChecker.isInvalidatable(row.toOpiskeluoikeusUnsafe, user)) {
        Left(KoskiErrorCategory.forbidden("Mitätöinti ei sallittu"))
      } else {
        findOppija(row.oppijaOid, useVirta = false, useYtr = false).map(_.getIgnoringWarnings).flatMap(invalidationFn)
      }
    }.flatMap(updateFn)

  def invalidateOpiskeluoikeus
    (opiskeluoikeusOid: String)
    (implicit user: KoskiSpecificSession)
  : Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] =
    invalidate(
      opiskeluoikeusOid,
      cancelOpiskeluoikeus(opiskeluoikeusOid),
      oppija => createOrUpdate(oppija, allowUpdate = true)
    )

  def invalidatePäätasonSuoritus
    (opiskeluoikeusOid: String, päätasonSuoritus: PäätasonSuoritus, versionumero: Int)
    (implicit user: KoskiSpecificSession)
  : Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] =
    invalidate(
      opiskeluoikeusOid,
      cancelPäätasonSuoritus(opiskeluoikeusOid, päätasonSuoritus, versionumero),
      oppija => createOrUpdate(oppija, allowUpdate = true, allowDeleteCompleted = true)
    )

  private def createOrUpdateOpiskeluoikeus(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean = false
  )(implicit user: KoskiSpecificSession)
  : Either[HttpStatus, OpiskeluoikeusVersio] = {
    if (oppijaOid.oppijaOid == user.oid) {
      Left(KoskiErrorCategory.forbidden.omienTietojenMuokkaus())
    } else {
      val result = opiskeluoikeus match {
        case ytrOo: YlioppilastutkinnonOpiskeluoikeus =>
          ytrDownloadedOpiskeluoikeusRepository.createOrUpdate(oppijaOid, ytrOo)
        case _ =>
          opiskeluoikeusRepository.createOrUpdate(oppijaOid, opiskeluoikeus, allowUpdate, allowDeleteCompleted)
      }
      result.right.map { (result: CreateOrUpdateResult) =>
        applicationLog(oppijaOid, opiskeluoikeus, result)
        opiskeluoikeus match {
          case _: YlioppilastutkinnonOpiskeluoikeus =>
            auditLogYtr(oppijaOid, result)
          case _ =>
            auditLog(oppijaOid, result)
        }
        OpiskeluoikeusVersio(result.oid, result.versionumero, result.lähdejärjestelmänId)
      }
    }
  }

  private def applicationLog
    (oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus, result: CreateOrUpdateResult)
    (implicit user: KoskiSpecificSession)
  : Unit = {
    val verb = result match {
      case updated: Updated =>
        opiskeluoikeus match {
          case _:YlioppilastutkinnonOpiskeluoikeus =>
            "Päivitetty"
          case _ =>
            val tila = updated.old.tila.opiskeluoikeusjaksot.last
            if (tila.opiskeluoikeusPäättynyt) {
              s"Päivitetty päättynyt (${tila.tila.koodiarvo})"
            } else {
              "Päivitetty"
            }
        }
      case _: Created => "Luotu"
      case _: NotChanged => "Päivitetty (ei muutoksia)"
    }
    val tutkinto = opiskeluoikeus.suoritukset.map(_.koulutusmoduuli.tunniste).mkString(",")
    val oppilaitosTaiKoulutustoimija = opiskeluoikeus.getOppilaitosOrKoulutusToimija.oid
    logger(user).info(s"${verb} opiskeluoikeus ${result.id} (versio ${result.versionumero}) oppijalle ${oppijaOid} tutkintoon ${tutkinto} oppilaitoksessa/koulutustoimijalla ${oppilaitosTaiKoulutustoimija}")
  }

  private def auditLog
    (oppijaOid: PossiblyUnverifiedHenkilöOid, result: CreateOrUpdateResult)
    (implicit user: KoskiSpecificSession)
  : Unit = {
    auditLog(updatedOperation = OPISKELUOIKEUS_MUUTOS, createdOperation = OPISKELUOIKEUS_LISAYS)(oppijaOid, result)
  }

  private def auditLogYtr
    (oppijaOid: PossiblyUnverifiedHenkilöOid, result: CreateOrUpdateResult)
      (implicit user: KoskiSpecificSession)
  : Unit = {
    auditLog(updatedOperation = YTR_OPISKELUOIKEUS_MUUTOS, createdOperation = YTR_OPISKELUOIKEUS_LISAYS)(oppijaOid, result)
  }

  private def auditLog
    (updatedOperation: KoskiOperation.Value, createdOperation: KoskiOperation.Value)
    (oppijaOid: PossiblyUnverifiedHenkilöOid, result: CreateOrUpdateResult)
    (implicit user: KoskiSpecificSession) =
  {
    (result match {
      case _: Updated => Some(updatedOperation)
      case _: Created => Some(createdOperation)
      case _ => None
    }).foreach { operaatio =>
      AuditLog.log(KoskiAuditLogMessage(operaatio, user, Map(
        oppijaHenkiloOid -> oppijaOid.oppijaOid,
        opiskeluoikeusId -> result.id.toString,
        opiskeluoikeusVersio -> result.versionumero.toString
      )))
    }
  }

  private def cancelOpiskeluoikeus(opiskeluoikeusOid: String)(oppija: Oppija): Either[HttpStatus, Oppija] = {
    oppija.tallennettavatOpiskeluoikeudet.find(_.oid.exists(_ == opiskeluoikeusOid))
      .toRight(KoskiErrorCategory.notFound())
      .flatMap(invalidated)
      .map(oo => oppija.copy(opiskeluoikeudet = List(oo)))
  }

  private def cancelPäätasonSuoritus
    (opiskeluoikeusOid: String, päätasonSuoritus: PäätasonSuoritus, versionumero: Int)
    (oppija: Oppija)
  : Either[HttpStatus, Oppija] = {
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
    val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)
    val viimeisinTila = oo.tila.opiskeluoikeusjaksot.maxBy(f => f.alku)(localDateOrdering).alku
    val mitatointiPvm = List(viimeisinTila, LocalDate.now()).max(localDateOrdering)
    (oo.tila match {
      case t: AmmatillinenOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ AmmatillinenOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
      case t: NuortenPerusopetuksenOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ NuortenPerusopetuksenOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
      case t: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(mitatointiPvm, mitätöity)))
      case t: AikuistenPerusopetuksenOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ AikuistenPerusopetuksenOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
      case t: LukionOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ LukionOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
      case t: DIAOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ DIAOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
      case t: InternationalSchoolOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ InternationalSchoolOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
      case t: VapaanSivistystyönOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
      case t: TutkintokoulutukseenValmentavanOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ TutkintokoulutukseenValmentavanOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
      case t: KorkeakoulunOpiskeluoikeudenTila => Left(KoskiErrorCategory.badRequest())
      case t: YlioppilastutkinnonOpiskeluoikeudenTila => Left(KoskiErrorCategory.badRequest())
      case t: EBOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ EBOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
      case t: EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
      case t: MuunKuinSäännellynKoulutuksenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ MuunKuinSäännellynKoulutuksenOpiskeluoikeudenJakso(mitätöity, mitatointiPvm, None)))
      case t: TaiteenPerusopetuksenOpiskeluoikeudenTila =>
        Right(t.copy(opiskeluoikeusjaksot = t.opiskeluoikeusjaksot :+ TaiteenPerusopetuksenOpiskeluoikeusjakso(mitatointiPvm, mitätöity)))
    }).map(oo.withTila)
  }

  private def withoutPäätasonSuoritus
    (päätasonSuoritus: PäätasonSuoritus)
    (oo: KoskeenTallennettavaOpiskeluoikeus)
  : Either[HttpStatus, Opiskeluoikeus] =
    if (oo.suoritukset.length == 1) {
      Left(KoskiErrorCategory.forbidden.ainoanPäätasonSuorituksenPoisto())
    } else {
      (oo, päätasonSuoritus) match {
        case (_: PerusopetuksenOpiskeluoikeus
              | _: AikuistenPerusopetuksenOpiskeluoikeus
              | _: AmmatillinenOpiskeluoikeus
              | _: InternationalSchoolOpiskeluoikeus
              | _: EuropeanSchoolOfHelsinkiOpiskeluoikeus
              | _: IBOpiskeluoikeus
              | _: EsiopetuksenOpiskeluoikeus
              | _: TaiteenPerusopetuksenOpiskeluoikeus, _) => delete(päätasonSuoritus, oo)
        case (_, _: LukionOppiaineenOppimääränSuoritus2015) => delete(päätasonSuoritus, oo)
        case _ => Left(KoskiErrorCategory.forbidden(s"Suoritusten tyyppiä ${päätasonSuoritus.tyyppi.koodiarvo} poisto ei ole sallittu"))
      }
    }

  private def delete(poistettavaPäätasonSuoritus: PäätasonSuoritus, oo: KoskeenTallennettavaOpiskeluoikeus)
  : Either[HttpStatus, KoskeenTallennettavaOpiskeluoikeus] = {
    oo.suoritukset.find(_ == poistettavaPäätasonSuoritus) match {
      case None => Left(KoskiErrorCategory.notFound())
      case Some(poistettavaSuoritus) =>
        val suorituksetIlmanPoistettavaaSuoritusta = oo.suoritukset diff List(poistettavaSuoritus)
        Right(oo.withSuoritukset(suorituksetIlmanPoistettavaaSuoritusta))
    }
  }

  private def toOppija(
    henkilö: OppijaHenkilö,
    opiskeluoikeudet: => WithWarnings[Seq[Opiskeluoikeus]],
    tunniste: OppijaHenkilö => String = _.oid
  )(implicit user: KoskiSpecificSession)
  : Either[HttpStatus, WithWarnings[Oppija]] = {
    val oppija = opiskeluoikeudet match {
      case WithWarnings(Nil, Nil) => Left(notFound(tunniste(henkilö)))
      case oo: WithWarnings[Seq[Opiskeluoikeus]] =>
        writeViewingEventToAuditLog(user, henkilö.oid)
        Right(oo.map(Oppija(henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö), _)))
    }
    oppija.map(piilotaOppijanTietojaTarvittaessa)
  }

  private def withOpiskeluoikeudet(
    henkilö: LaajatOppijaHenkilöTiedot,
    opiskeluoikeudet: => WithWarnings[Seq[Opiskeluoikeus]],
    tunniste: OppijaHenkilö => String = _.oid
  )(implicit user: KoskiSpecificSession)
  : Either[HttpStatus, WithWarnings[(LaajatOppijaHenkilöTiedot, Seq[Opiskeluoikeus])]] = {
    opiskeluoikeudet match {
      case WithWarnings(Nil, Nil) => Left(notFound(tunniste(henkilö)))
      case oo: WithWarnings[Seq[Opiskeluoikeus]] =>
        writeViewingEventToAuditLog(user, henkilö.oid)
        Right(oo.map((henkilö, _)))
    }
  }

  private def notFound(tunniste: String): HttpStatus = KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia(
    "Oppijaa " + tunniste + " ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."
  )

  private def writeViewingEventToAuditLog(user: KoskiSpecificSession, oid: Henkilö.Oid): Unit = {
    if (!List(
      // Tallennettujen YTR-operaatioiden katsomiset tehdään tällä systeemikäyttäjätunnuksella, joten auditlokitus tehdään
      // servletissä erikseen sitä kutsuneen käyttäjän nimissä:
      KoskiSpecificSession.systemUserTallennetutYlioppilastutkinnonOpiskeluoikeudet,
      KoskiSpecificSession.systemUser // To prevent health checks from polluting the audit log
    ).contains(user)) {
      val operation = if (user.user.kansalainen && user.isUsersHuollettava(oid)) {
        KANSALAINEN_HUOLTAJA_OPISKELUOIKEUS_KATSOMINEN
      } else if (user.user.kansalainen) {
        KANSALAINEN_OPISKELUOIKEUS_KATSOMINEN
      } else if (user.user.isSuoritusjakoKatsominen) {
        KANSALAINEN_SUORITUSJAKO_KATSOMINEN
      } else if (user.oid == config.getString("suomi-fi-user-oid")) {
        KANSALAINEN_SUOMIFI_KATSOMINEN
      } else {
        OPISKELUOIKEUS_KATSOMINEN
      }
      AuditLog.log(KoskiAuditLogMessage(operation, user, Map(oppijaHenkiloOid -> oid)))
    }
  }

  def opinnotonOppija(oid: String)(implicit koskiSession: KoskiSpecificSession): Option[WithWarnings[Oppija]] =
    henkilöRepository.findByOid(oid)
      .map(henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot)
      .map(Oppija(_, Nil))
      .map(WithWarnings(_, Nil))
      .map(piilotaOppijanTietojaTarvittaessa)

  private def piilotaOppijanTietojaTarvittaessa(oppija: WithWarnings[Oppija])(implicit user: KoskiSpecificSession): WithWarnings[Oppija] = {
    if (user.user.kansalainen || user.user.huollettava || user.user.isSuoritusjakoKatsominen) {
      oppija.map(piilotetuillaTiedoilla)
    } else {
      oppija
    }
  }

  private def piilotaOppijanTietojaTarvittaessa(oppija: Oppija)(implicit user: KoskiSpecificSession): Oppija = {
    if (user.user.kansalainen || user.user.huollettava || user.user.isSuoritusjakoKatsominen) {
      piilotetuillaTiedoilla(oppija)
    } else {
      oppija
    }
  }

  private def piilotetuillaTiedoilla(oppija: Oppija)(implicit koskiSession: KoskiSpecificSession): Oppija = {
    val piilota = piilotaArvosanatKeskeneräisistäSuorituksista _ andThen
      piilotaSensitiivisetHenkilötiedot andThen
      piilotaKeskeneräisetPerusopetuksenPäättötodistukset andThen
      piilotaTietojaSuoritusjaosta andThen
      piilotaLaajuuksia

    piilota(oppija)
  }

  private def piilotaArvosanatKeskeneräisistäSuorituksista(oppija: Oppija) = {
    val keskeneräisetTaiLiianÄskettäinVahvistetut = traversal[Suoritus].filter { s =>
      s.vahvistus.isEmpty || !s.vahvistus.exists { v => v.päivä.plusDays(4).isBefore(LocalDate.now())}
    }.compose(päätasonSuorituksetTraversal)
    val piilotettavatOppiaineidenArvioinnit = (oppimääränArvioinnitTraversal ++ vuosiluokanArvioinnitTraversal ++ oppiaineenOppimääränArvioinnitTraversal).compose(keskeneräisetTaiLiianÄskettäinVahvistetut)
    val piilotettavaKäyttäytymisenArviointi = käyttäytymisenArviointiTraversal.compose(keskeneräisetTaiLiianÄskettäinVahvistetut)

    List(piilotettavaKäyttäytymisenArviointi, piilotettavatOppiaineidenArvioinnit).foldLeft(oppija) { (oppija, traversal) =>
      traversal.set(oppija)(None)
    }
  }

  private def piilotaLaajuuksia(oppija: Oppija)= {
    val  keskenTaiVahvistettuEnnenLeikkuriPäivää = traversal[Suoritus].filter { suoritus =>
      (suoritus.isInstanceOf[PerusopetuksenVuosiluokanSuoritus] || suoritus.isInstanceOf[NuortenPerusopetuksenOppimääränSuoritus]) &&
        suoritus.vahvistus.forall(_.päivä.isBefore(LocalDate.of(2020, 8, 1)))
    }.compose(päätasonSuorituksetTraversal)

    val piilotettavatLaajuudet = nuortenPerusopetuksenPakollistenOppiaineidenLaajuudetTraversal.compose(keskenTaiVahvistettuEnnenLeikkuriPäivää)

    piilotettavatLaajuudet.set(oppija)(None)
  }

  private def piilotaTietojaSuoritusjaosta(oppija: Oppija)(implicit koskiSession: KoskiSpecificSession) = {
    if (koskiSession.user.isSuoritusjakoKatsominen) {
      piilotaLukuvuosimaksutiedot(oppija)
    } else {
      oppija
    }
  }

  private def piilotaLukuvuosimaksutiedot(oppija: Oppija)(implicit koskiSession: KoskiSpecificSession) = {
    val korjatutOpiskeluoikeudet = oppija.opiskeluoikeudet.map {
      case oo: KorkeakoulunOpiskeluoikeus if oo.lisätiedot.nonEmpty => {
        val korjatutLukukausiIlmottautuminen = oo.lisätiedot.get.lukukausiIlmoittautuminen.flatMap(ilmo =>
          Some(ilmo.copy(
            ilmoittautumisjaksot = ilmo.ilmoittautumisjaksot.map(_.copy(
              maksetutLukuvuosimaksut = None
            ))
          ))
        )

        val korjatutLisätiedot = oo.lisätiedot.get.copy(
          maksettavatLukuvuosimaksut = None,
          lukukausiIlmoittautuminen = korjatutLukukausiIlmottautuminen
        )
        oo.copy(
          lisätiedot = Some(korjatutLisätiedot)
        )
      }
      case oo: Any => oo
    }
    oppija.copy(
      opiskeluoikeudet = korjatutOpiskeluoikeudet
    )
  }

  private def piilotaSensitiivisetHenkilötiedot(oppija: Oppija) = {
    val t: Traversal[Oppija, TäydellisetHenkilötiedot] = traversal[Oppija].field[Henkilö]("henkilö").ifInstanceOf[TäydellisetHenkilötiedot]
    t.modify(oppija)((th: TäydellisetHenkilötiedot) => th.copy(hetu = None, kansalaisuus = None, turvakielto = None))
  }

  private def piilotaKeskeneräisetPerusopetuksenPäättötodistukset(oppija: Oppija): Oppija = {
    def poistaKeskeneräisetPäättötodistukset = (suoritukset: List[PäätasonSuoritus]) => suoritukset.filter(_ match {
      case s: PerusopetuksenOppimääränSuoritus if !s.valmis => false
      case _ => true
    })

    def poistaOsasuoritukset = (suoritukset: List[PäätasonSuoritus]) => suoritukset.map(s =>
      shapeless.lens[PäätasonSuoritus].field[Option[List[Suoritus]]]("osasuoritukset").set(s)(None)
    )

    shapeless.lens[Oppija].field[Seq[Opiskeluoikeus]]("opiskeluoikeudet").modify(oppija)(_.map(oo => {
      val isKeskeneräinenPäättötodistusAinoaSuoritus = oo.suoritukset match {
        case (s: PerusopetuksenOppimääränSuoritus) :: Nil if s.kesken => true
        case _ => false
      }

      shapeless.lens[Opiskeluoikeus].field[List[PäätasonSuoritus]]("suoritukset").modify(oo)(
        if (isKeskeneräinenPäättötodistusAinoaSuoritus) poistaOsasuoritukset else poistaKeskeneräisetPäättötodistukset
      )
    }))
  }

  private lazy val mitätöity = Koodistokoodiviite("mitatoity", koodistoUri = "koskiopiskeluoikeudentila")

  def merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta(opiskeluoikeusOid: String): HttpStatus = {
    opiskeluoikeusRepository.merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta(opiskeluoikeusOid)
  }
}

case class HenkilönOpiskeluoikeusVersiot(henkilö: OidHenkilö, opiskeluoikeudet: List[OpiskeluoikeusVersio])

case class OpiskeluoikeusVersio(
  oid: Opiskeluoikeus.Oid,
  versionumero: Int,
  lähdejärjestelmänId: Option[LähdejärjestelmäId]
) extends Lähdejärjestelmällinen

case class OppijaYksilöintitiedolla(
  oppija: Oppija,
  yksilöity: Boolean
)
