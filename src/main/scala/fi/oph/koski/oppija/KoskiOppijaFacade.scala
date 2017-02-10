package fi.oph.koski.oppija

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
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing

class KoskiOppijaFacade(henkilöRepository: HenkilöRepository, OpiskeluoikeusRepository: OpiskeluoikeusRepository, historyRepository: OpiskeluoikeusHistoryRepository, perustiedotRepository: OpiskeluoikeudenPerustiedotRepository, config: Config) extends Logging with Timing with GlobalExecutionContext {
  private lazy val mockOids = config.hasPath("authentication-service.mockOid") && config.getBoolean("authentication-service.mockOid")
  def findOppija(oid: String)(implicit user: KoskiSession): Either[HttpStatus, Oppija] = toOppija(OpiskeluoikeusRepository.findByOppijaOid)(user)(oid)

  def findVersion(oid: String, opiskeluoikeusId: Int, versionumero: Int)(implicit user: KoskiSession): Either[HttpStatus, Oppija] = {
    // TODO: tarkista, että opiskeluoikeus kuuluu tälle oppijalla
    historyRepository.findVersion(opiskeluoikeusId, versionumero).right.flatMap { history =>
      toOppija(_ => List(history))(user)(oid)
    }
  }

  def findUserOppija(implicit user: KoskiSession): Either[HttpStatus, Oppija] = toOppija(OpiskeluoikeusRepository.findByUserOid)(user)(user.oid)

  def createOrUpdate(oppija: Oppija)(implicit user: KoskiSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {
    val oppijaOid: Either[HttpStatus, PossiblyUnverifiedHenkilöOid] = oppija.henkilö match {
      case h:UusiHenkilö =>
        Hetu.validate(h.hetu, acceptSynthetic = false).right.flatMap { hetu =>
          henkilöRepository.findOrCreate(h).right.map(VerifiedHenkilöOid(_))
        }
      case h:NimitiedotJaOid if mockOids =>
        Right(VerifiedHenkilöOid(TäydellisetHenkilötiedot(h.oid, "010101-123N", h.etunimet, h.kutsumanimi, h.sukunimi, None, None)))
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
            createOrUpdateOpiskeluoikeus(oppijaOid, opiskeluoikeus)
          }

          opiskeluoikeusCreationResults.find(_.isLeft) match {
            case Some(Left(error)) => Left(error)
            case _ => Right(HenkilönOpiskeluoikeusVersiot(OidHenkilö(oppijaOid.oppijaOid), opiskeluoikeusCreationResults.toList.map {
              case Right(r) => r
            }))
          }
        }
      }
    }
  }

  private def createOrUpdateOpiskeluoikeus(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): Either[HttpStatus, OpiskeluoikeusVersio] = {
    def applicationLog(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluoikeus: Opiskeluoikeus, result: CreateOrUpdateResult): Unit = {
      val verb = result match {
        case _: Updated => "Päivitetty"
        case _: Created => "Luotu"
        case _: NotChanged => "Päivitetty (ei muutoksia)"
      }
      logger(user).info(verb + " opiskeluoikeus " + result.id + " (versio " + result.versionumero + ")" + " oppijalle " + oppijaOid +
        " tutkintoon " + opiskeluoikeus.suoritukset.map(_.koulutusmoduuli.tunniste).mkString(",") +
        " oppilaitoksessa " + opiskeluoikeus.getOppilaitos.oid)
    }

    def auditLog(oppijaOid: PossiblyUnverifiedHenkilöOid, result: CreateOrUpdateResult): Unit = {
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

    if (oppijaOid.oppijaOid == user.oid) {
      Left(KoskiErrorCategory.forbidden.omienTietojenMuokkaus())
    } else {
      val result = OpiskeluoikeusRepository.createOrUpdate(oppijaOid, opiskeluoikeus)
      result.right.map { (result: CreateOrUpdateResult) =>
        applicationLog(oppijaOid, opiskeluoikeus, result)
        auditLog(oppijaOid, result)

        val nimitiedotJaOid = oppijaOid.verified.map(_.nimitiedotJaOid).getOrElse(throw new RuntimeException(s"Oppijaa {${oppijaOid.oppijaOid}} ei löydy")) // TODO: päivitystapauksessa ei haeta henkilöä, päivitetään muut tiedon elasticsearchiin
        val oo = opiskeluoikeus

        if (result.changed) {
          val perustiedot = OpiskeluoikeudenPerustiedot.makePerustiedot(result.id, result.data, oo.luokka, nimitiedotJaOid)
          perustiedotRepository.update(perustiedot)
        }

        OpiskeluoikeusVersio(result.id, result.versionumero)
      }
    }
  }

  private def toOppija(findFunc: String => Seq[Opiskeluoikeus])(implicit user: KoskiSession): String => Either[HttpStatus, Oppija] = oid => {
    def notFound = Left(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa " + oid + " ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
    val result = henkilöRepository.findByOid(oid) match {
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