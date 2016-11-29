package fi.oph.koski.oppija

import fi.oph.koski.henkilo._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{opiskeluOikeusId, opiskeluOikeusVersio, oppijaHenkiloOid}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, _}
import fi.oph.koski.opiskeluoikeus._
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing

class KoskiOppijaFacade(oppijaRepository: HenkilöRepository, opiskeluOikeusRepository: OpiskeluOikeusRepository) extends Logging with Timing {
  def findOppija(oid: String)(implicit user: KoskiSession): Either[HttpStatus, Oppija] = toOppija(opiskeluOikeusRepository.findByOppijaOid)(user)(oid)

  def findUserOppija(implicit user: KoskiSession): Either[HttpStatus, Oppija] = toOppija(opiskeluOikeusRepository.findByUserOid)(user)(user.oid)

  def createOrUpdate(oppija: Oppija)(implicit user: KoskiSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val oppijaOid: Either[HttpStatus, PossiblyUnverifiedHenkilöOid] = oppija.henkilö match {
      case h:UusiHenkilö =>
        Hetu.validate(h.hetu, acceptSynthetic = false).right.flatMap { hetu =>
          oppijaRepository.findOrCreate(h).right.map(VerifiedHenkilöOid(_))
        }
      case h:HenkilöWithOid => Right(UnverifiedHenkilöOid(h.oid, oppijaRepository))
    }

    timed("createOrUpdate") {
      val opiskeluoikeudet: Seq[KoskeenTallennettavaOpiskeluoikeus] = oppija.tallennettavatOpiskeluoikeudet

      oppijaOid.right.flatMap { oppijaOid: PossiblyUnverifiedHenkilöOid =>
        if (oppijaOid.oppijaOid == user.oid) {
          Left(KoskiErrorCategory.forbidden.omienTietojenMuokkaus())
        } else {
          val opiskeluOikeusCreationResults: Seq[Either[HttpStatus, OpiskeluoikeusVersio]] = opiskeluoikeudet.map { opiskeluoikeus =>
            createOrUpdateOpiskeluoikeus(oppijaOid, opiskeluoikeus)
          }

          opiskeluOikeusCreationResults.find(_.isLeft) match {
            case Some(Left(error)) => Left(error)
            case _ => Right(HenkilönOpiskeluoikeusVersiot(OidHenkilö(oppijaOid.oppijaOid), opiskeluOikeusCreationResults.toList.map {
              case Right(r) => r
            }))
          }
        }
      }
    }
  }

  private def createOrUpdateOpiskeluoikeus(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusVersio] = {
    def applicationLog(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluOikeus: Opiskeluoikeus, result: CreateOrUpdateResult): Unit = {
      val (verb, content) = result match {
        case _: Updated => ("Päivitetty", Json.write(result.diff))
        case _: Created => ("Luotu", Json.write(opiskeluOikeus))
        case _: NotChanged => ("Päivitetty", "ei muutoksia")
      }
      logger(user).info(verb + " opiskeluoikeus " + result.id + " (versio " + result.versionumero + ")" + " oppijalle " + oppijaOid +
        " tutkintoon " + opiskeluOikeus.suoritukset.map(_.koulutusmoduuli.tunniste).mkString(",") +
        " oppilaitoksessa " + opiskeluOikeus.oppilaitos.oid + ": " + content)
    }

    def auditLog(oppijaOid: PossiblyUnverifiedHenkilöOid, result: CreateOrUpdateResult): Unit = {
      (result match {
        case _: Updated => Some(OPISKELUOIKEUS_MUUTOS)
        case _: Created => Some(OPISKELUOIKEUS_LISAYS)
        case _ => None
      }).foreach { operaatio =>
        AuditLog.log(AuditLogMessage(operaatio, user,
          Map(oppijaHenkiloOid -> oppijaOid.oppijaOid, opiskeluOikeusId -> result.id.toString, opiskeluOikeusVersio -> result.versionumero.toString))
        )
      }
    }

    if (oppijaOid.oppijaOid == user.oid) {
      Left(KoskiErrorCategory.forbidden.omienTietojenMuokkaus())
    } else {
      val result = opiskeluOikeusRepository.createOrUpdate(oppijaOid, opiskeluoikeus)
      result.right.map { result =>
        applicationLog(oppijaOid, opiskeluoikeus, result)
        auditLog(oppijaOid, result)
        OpiskeluoikeusVersio(result.id, result.versionumero)
      }
    }
  }

  private def toOppija(findFunc: String => Seq[Opiskeluoikeus])(implicit user: KoskiSession): String => Either[HttpStatus, Oppija] = oid => {
    def notFound = Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa " + oid + " ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
    val result = oppijaRepository.findByOid(oid) match {
      case Some(oppija) =>
        findFunc(oppija.oid) match {
          case Nil => notFound
          case opiskeluoikeudet: Seq[Opiskeluoikeus] => Right(Oppija(oppija, opiskeluoikeudet.sortBy(_.id)))
        }
      case None => notFound
    }

    result.right.foreach((oppija: Oppija) => writeViewingEventToAuditLog(user, oid))
    result
  }

  private def writeViewingEventToAuditLog(user: KoskiSession, oid: Henkilö.Oid): Unit = {
    if (user != KoskiSession.systemUser) { // To prevent health checks from pollutings the audit log
      AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, user, Map(oppijaHenkiloOid -> oid)))
    }
  }
}

case class HenkilönOpiskeluoikeusVersiot(henkilö: OidHenkilö, opiskeluoikeudet: List[OpiskeluoikeusVersio])
case class OpiskeluoikeusVersio(id: Opiskeluoikeus.Id, versionumero: Int)