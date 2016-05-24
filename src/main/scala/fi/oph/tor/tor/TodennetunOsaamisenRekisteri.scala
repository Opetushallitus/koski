package fi.oph.tor.tor

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.tor.http.{HttpStatus, TorErrorCategory}
import fi.oph.tor.json.Json
import fi.oph.tor.log.AuditLog.{log => auditLog}
import fi.oph.tor.log.TorMessageField.{hakuEhto, opiskeluOikeusId, opiskeluOikeusVersio, oppijaHenkiloOid}
import fi.oph.tor.log.TorOperation._
import fi.oph.tor.log._
import fi.oph.tor.opiskeluoikeus._
import fi.oph.tor.oppija._
import fi.oph.tor.schema.Henkilö.Oid
import fi.oph.tor.schema._
import fi.oph.tor.toruser.TorUser
import fi.oph.tor.util.Timing
import org.json4s._
import rx.lang.scala.Observable

class TodennetunOsaamisenRekisteri(oppijaRepository: OppijaRepository,
                                   opiskeluOikeusRepository: OpiskeluOikeusRepository) extends Logging with Timing {

  def findOppijat(params: List[(String, String)], user: TorUser): Either[HttpStatus, Observable[Oppija]] with Product with Serializable = {

    auditLog(AuditLogMessage(OPISKELUOIKEUS_HAKU, user, Map(hakuEhto -> params.map { case (p,v) => p + "=" + v }.mkString("&"))))

    def dateParam(q: (String, String)): Either[HttpStatus, LocalDate] = q match {
      case (p, v) => try {
        Right(LocalDate.parse(v))
      } catch {
        case e: DateTimeParseException => Left(TorErrorCategory.badRequest.format.pvm("Invalid date parameter: " + p + "=" + v))
      }
    }

    val queryFilters: List[Either[HttpStatus, QueryFilter]] = params.map {
      case (p, v) if p == "opiskeluoikeusPäättynytAikaisintaan" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytAikaisintaan(_))
      case (p, v) if p == "opiskeluoikeusPäättynytViimeistään" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytViimeistään(_))
      case ("tutkinnonTila", v) => Right(TutkinnonTila(v))
      case (p, _) => Left(TorErrorCategory.badRequest.queryParam.unknown("Unsupported query parameter: " + p))
    }

    queryFilters.partition(_.isLeft) match {
      case (Nil, queries) =>
        val filters: List[QueryFilter] = queries.flatMap(_.right.toOption)
        Right(query(filters)(user))
      case (errors, _) =>
        Left(HttpStatus.fold(errors.map(_.left.get)))
    }
  }

  def findOppijat(query: String)(implicit user: TorUser): Seq[TaydellisetHenkilötiedot] = {
    val oppijat: List[TaydellisetHenkilötiedot] = oppijaRepository.findOppijat(query)
    auditLog(AuditLogMessage(OPPIJA_HAKU, user, Map(hakuEhto -> query)))
    val filtered = opiskeluOikeusRepository.filterOppijat(oppijat)
    filtered.sortBy(oppija => (oppija.sukunimi, oppija.etunimet))
  }

  def createOrUpdate(oppija: Oppija)(implicit user: TorUser): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] = {

    def applicationLog(oppijaOid: PossiblyUnverifiedOppijaOid, opiskeluOikeus: Opiskeluoikeus, result: CreateOrUpdateResult): Unit = {
      val (verb, content) = result match {
        case _: Updated => ("Päivitetty", Json.write(result.diff))
        case _: Created => ("Luotu", Json.write(opiskeluOikeus))
        case _: NotChanged => ("Päivitetty", "ei muutoksia")
      }
      logger(user).info(verb + " opiskeluoikeus " + result.id + " (versio " + result.versionumero + ")" + " oppijalle " + oppijaOid +
        " tutkintoon " + opiskeluOikeus.suoritukset.map(_.koulutusmoduuli.tunniste).mkString(",") +
        " oppilaitoksessa " + opiskeluOikeus.oppilaitos.oid + ": " + content)
    }

    def accessLog(oppijaOid: PossiblyUnverifiedOppijaOid, result: CreateOrUpdateResult): Unit = {
      (result match {
        case _: Updated => Some(OPISKELUOIKEUS_MUUTOS)
        case _: Created => Some(OPISKELUOIKEUS_LISAYS)
        case _ => None
      }).foreach { operaatio =>
        auditLog(AuditLogMessage(operaatio, user,
          Map(oppijaHenkiloOid -> oppijaOid.oppijaOid, opiskeluOikeusId -> result.id.toString, opiskeluOikeusVersio -> result.versionumero.toString))
        )
      }
    }

    timed("createOrUpdate") {
      val oppijaOid: Either[HttpStatus, PossiblyUnverifiedOppijaOid] = oppija.henkilö match {
        case h:UusiHenkilö => oppijaRepository.findOrCreate(h).right.map(VerifiedOppijaOid(_))
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

  def findTorOppija(oid: String)(implicit user: TorUser): Either[HttpStatus, Oppija] = {
    def notFound = Left(TorErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa " + oid + " ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))

    val result = oppijaRepository.findByOid(oid) match {
      case Some(oppija) =>
        opiskeluOikeusRepository.findByOppijaOid(oppija.oid) match {
          case Nil => notFound
          case opiskeluoikeudet: Seq[Opiskeluoikeus] => Right(Oppija(oppija, opiskeluoikeudet))
        }
      case None =>
        notFound
    }
    result.right.foreach((oppija: Oppija) => auditLog(AuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, user, Map(oppijaHenkiloOid -> oid))))
    result
  }

  def findOpiskeluOikeus(id: Int)(implicit user: TorUser): Either[HttpStatus, (TaydellisetHenkilötiedot, Opiskeluoikeus)] = {
    val result: Option[(TaydellisetHenkilötiedot, Opiskeluoikeus)] = opiskeluOikeusRepository.findById(id) flatMap { case (oo, oppijaOid) =>
      oppijaRepository.findByOid(oppijaOid).map((_, oo))
    }
    result match {
      case Some((henkilö, oo)) =>
        auditLog(AuditLogMessage(OPISKELUOIKEUS_KATSOMINEN, user, Map(oppijaHenkiloOid -> henkilö.oid)))
        Right((henkilö, oo))
      case _ =>
        Left(TorErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
    }
  }


  private def query(filters: List[QueryFilter])(implicit user: TorUser): Observable[Oppija] = {
    val oikeudetPerOppijaOid: Observable[(Oid, List[Opiskeluoikeus])] = opiskeluOikeusRepository.query(filters)
    oikeudetPerOppijaOid.tumblingBuffer(500).flatMap {
      oppijatJaOidit: Seq[(Oid, List[Opiskeluoikeus])] =>
        val oids: List[String] = oppijatJaOidit.map(_._1).toList

        val henkilöt: Map[String, TaydellisetHenkilötiedot] = oppijaRepository.findByOids(oids).map(henkilö => (henkilö.oid, henkilö)).toMap

        val torOppijat: Iterable[Oppija] = oppijatJaOidit.flatMap { case (oid, opiskeluOikeudet) =>
          henkilöt.get(oid) match {
            case Some(henkilö) =>
              Some(Oppija(henkilö, opiskeluOikeudet))
            case None =>
              logger(user).warn("Oppijaa " + oid + " ei löydy henkilöpalvelusta")
              None
          }
        }
        Observable.from(torOppijat)
    }
  }
}

case class HenkilönOpiskeluoikeusVersiot(henkilö: OidHenkilö, opiskeluoikeudet: List[OpiskeluoikeusVersio])
case class OpiskeluoikeusVersio(id: Opiskeluoikeus.Id, versionumero: Int)


trait QueryFilter

case class OpiskeluoikeusPäättynytAikaisintaan(päivä: LocalDate) extends QueryFilter
case class OpiskeluoikeusPäättynytViimeistään(päivä: LocalDate) extends QueryFilter
case class TutkinnonTila(tila: String) extends QueryFilter
case class ValidationResult(oid: Henkilö.Oid, errors: List[AnyRef])
case class HistoryInconsistency(message: String, diff: JValue)