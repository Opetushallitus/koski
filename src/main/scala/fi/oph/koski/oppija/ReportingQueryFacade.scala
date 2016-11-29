package fi.oph.koski.oppija

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.koski.db.OpiskeluOikeusRow
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.opiskeluoikeus.OpiskeluOikeusRepository
import fi.oph.koski.schema.Henkilö.{apply => _, _}
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import rx.lang.scala.Observable

case class ReportingQueryFacade(oppijaRepository: HenkilöRepository, opiskeluOikeusRepository: OpiskeluOikeusRepository) extends Logging {
  def findOppijat(params: List[(String, String)], user: KoskiSession): Either[HttpStatus, Observable[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])]] with Product with Serializable = {

    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_HAKU, user, Map(hakuEhto -> params.map { case (p,v) => p + "=" + v }.mkString("&"))))

    def dateParam(q: (String, String)): Either[HttpStatus, LocalDate] = q match {
      case (p, v) => try {
        Right(LocalDate.parse(v))
      } catch {
        case e: DateTimeParseException => Left(KoskiErrorCategory.badRequest.format.pvm("Invalid date parameter: " + p + "=" + v))
      }
    }

    val queryFilters: List[Either[HttpStatus, QueryFilter]] = params.map {
      case (p, v) if p == "opiskeluoikeusPäättynytAikaisintaan" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytAikaisintaan(_))
      case (p, v) if p == "opiskeluoikeusPäättynytViimeistään" => dateParam((p, v)).right.map(OpiskeluoikeusPäättynytViimeistään(_))
      case ("tutkinnonTila", v) => Right(TutkinnonTila(v))
      case (p, _) => Left(KoskiErrorCategory.badRequest.queryParam.unknown("Unsupported query parameter: " + p))
    }

    queryFilters.partition(_.isLeft) match {
      case (Nil, queries) =>
        val filters: List[QueryFilter] = queries.flatMap(_.right.toOption)
        Right(query(filters)(user))
      case (errors, _) =>
        Left(HttpStatus.fold(errors.map(_.left.get)))
    }
  }

  private def query(filters: List[QueryFilter])(implicit user: KoskiSession): Observable[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])] = {
    val oikeudetPerOppijaOid: Observable[(Oid, List[OpiskeluOikeusRow])] = opiskeluOikeusRepository.query(filters)
    oikeudetPerOppijaOid.tumblingBuffer(500).flatMap {
      oppijatJaOidit: Seq[(Oid, List[OpiskeluOikeusRow])] =>
        val oids: List[String] = oppijatJaOidit.map(_._1).toList

        val henkilöt: Map[String, TäydellisetHenkilötiedot] = oppijaRepository.findByOids(oids).map(henkilö => (henkilö.oid, henkilö)).toMap

        val oppijat: Iterable[(TäydellisetHenkilötiedot, List[OpiskeluOikeusRow])] = oppijatJaOidit.flatMap { case (oid, opiskeluOikeudet) =>
          henkilöt.get(oid) match {
            case Some(henkilö) =>
              Some((henkilö, opiskeluOikeudet))
            case None =>
              logger(user).warn("Oppijaa " + oid + " ei löydy henkilöpalvelusta")
              None
          }
        }
        Observable.from(oppijat)
    }
  }
}

trait QueryFilter

case class OpiskeluoikeusPäättynytAikaisintaan(päivä: LocalDate) extends QueryFilter
case class OpiskeluoikeusPäättynytViimeistään(päivä: LocalDate) extends QueryFilter
case class TutkinnonTila(tila: String) extends QueryFilter