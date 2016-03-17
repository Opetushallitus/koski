package fi.oph.tor.tor

import fi.oph.tor.http.{TorErrorCategory, HttpStatus}
import fi.oph.tor.json.Json
import fi.oph.tor.log.TorOperation.{OPISKELUOIKEUS_LISAYS, OPISKELUOIKEUS_MUUTOS}
import fi.oph.tor.opiskeluoikeus._
import fi.oph.tor.oppija._
import fi.oph.tor.schema.Henkilö.Oid
import fi.oph.tor.schema._
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.log._
import fi.oph.tor.util.Timing
import rx.lang.scala.Observable

class TodennetunOsaamisenRekisteri(oppijaRepository: OppijaRepository,
                                   opiskeluOikeusRepository: OpiskeluOikeusRepository) extends Logging with Timing {

  def findOppijat(filters: List[QueryFilter])(implicit user: TorUser): Observable[TorOppija] = {
    val oikeudetPerOppijaOid: Observable[(Oid, List[OpiskeluOikeus])] = opiskeluOikeusRepository.query(filters)
    oikeudetPerOppijaOid.tumblingBuffer(500).flatMap {
      oppijatJaOidit: Seq[(Oid, List[OpiskeluOikeus])] =>
        val oids: List[String] = oppijatJaOidit.map(_._1).toList

        val henkilöt: Map[String, FullHenkilö] = oppijaRepository.findByOids(oids).map(henkilö => (henkilö.oid, henkilö)).toMap

        val torOppijat: Iterable[TorOppija] = oppijatJaOidit.flatMap { case (oid, opiskeluOikeudet) =>
          henkilöt.get(oid) match {
            case Some(henkilö) =>
              Some(TorOppija(henkilö, opiskeluOikeudet))
            case None =>
              logger.warn("Oppijaa " + oid + " ei löydy henkilöpalvelusta")
              None
          }
        }
        Observable.from(torOppijat)
    }
  }

  def findOppijat(query: String)(implicit user: TorUser): Seq[FullHenkilö] = {
    val oppijat: List[FullHenkilö] = oppijaRepository.findOppijat(query)
    val filtered = opiskeluOikeusRepository.filterOppijat(oppijat)
    filtered.sortBy(oppija => (oppija.sukunimi, oppija.etunimet))
  }

  def createOrUpdate(oppija: TorOppija)(implicit user: TorUser): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {

    def applicationLog(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: OpiskeluOikeus, result: CreateOrUpdateResult): Unit = {
      val (verb, content) = result match {
        case _: Updated => ("Päivitetty", Json.write(result.diff))
        case _: Created => ("Luotu", Json.write(opiskeluOikeus))
        case _: NotChanged => ("Päivitetty", "ei muutoksia")
      }
      logger.info(verb + " opiskeluoikeus " + result.id + " (versio " + result.versionumero + ")" + " oppijalle " + oppijaOid + " tutkintoon " + opiskeluOikeus.suoritus.koulutusmoduulitoteutus.koulutusmoduuli.tunniste + " oppilaitoksessa " + opiskeluOikeus.oppilaitos.oid + ": " + content)
    }

    def accessLog(oppijaOid: PossiblyUnverifiedOppijaOid, result: CreateOrUpdateResult): Unit = {
      (result match {
        case _: Updated => Some(OPISKELUOIKEUS_MUUTOS)
        case _: Created => Some(OPISKELUOIKEUS_LISAYS)
        case _ => None
      }).foreach { operaatio =>
        AuditLog.log(AuditLogMessage(operaatio, user,
          Map(TorMessageField.oppijaHenkiloOid -> oppijaOid.oppijaOid, TorMessageField.opiskeluOikeusId -> result.id.toString, TorMessageField.opiskeluOikeusVersio -> result.versionumero.toString))
        )
      }
    }

    timed("createOrUpdate") {
      val oppijaOid: Either[HttpStatus, PossiblyUnverifiedOppijaOid] = oppija.henkilö match {
        case h:NewHenkilö => oppijaRepository.findOrCreate(h).right.map(VerifiedOppijaOid(_))
        case h:HenkilöWithOid => Right(UnverifiedOppijaOid(h.oid, oppijaRepository))
      }

      oppijaOid.right.flatMap { oppijaOid: PossiblyUnverifiedOppijaOid =>
        val opiskeluOikeusCreationResults: Seq[Either[HttpStatus, CreateOrUpdateResult]] = oppija.opiskeluoikeudet.map { opiskeluOikeus =>
          val result = opiskeluOikeusRepository.createOrUpdate(oppijaOid, opiskeluOikeus)
          result match {
            case Right(result) =>
              applicationLog(oppijaOid, opiskeluOikeus, result)
              accessLog(oppijaOid, result)
            case _ =>
          }
          result
        }

        opiskeluOikeusCreationResults.find(_.isLeft) match {
          case Some(Left(error)) => Left(error)
          case _ => Right(HenkilönOpiskeluoikeusVersiot(OidHenkilö(oppijaOid.oppijaOid), opiskeluOikeusCreationResults.toList.map {
            case Right(result:CreateOrUpdateResult) => OpiskeluoikeusVersio(result.id, result.versionumero)
          }))
        }
      }
    }
  }

  def findTorOppija(oid: String)(implicit user: TorUser): Either[HttpStatus, TorOppija] = {
    def notFound = Left(TorErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa " + oid + " ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))

    val result = oppijaRepository.findByOid(oid) match {
      case Some(oppija) =>
        opiskeluOikeusRepository.findByOppijaOid(oppija.oid) match {
          case Nil => notFound
          case opiskeluoikeudet => Right(TorOppija(oppija, opiskeluoikeudet))
        }
      case None =>
        notFound
    }
    result.right.foreach((oppija: TorOppija) => AuditLog.log(AuditLogMessage(TorOperation.OPISKELUOIKEUS_KATSOMINEN, user, Map(TorMessageField.oppijaHenkiloOid -> oid))))
    result
  }
}

case class HenkilönOpiskeluoikeusVersiot(henkilö: OidHenkilö, opiskeluoikeudet: List[OpiskeluoikeusVersio])
case class OpiskeluoikeusVersio(id: OpiskeluOikeus.Id, versionumero: Int)