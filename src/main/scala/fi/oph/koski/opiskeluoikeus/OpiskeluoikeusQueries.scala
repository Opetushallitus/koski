package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{GlobalExecutionContext, HenkilöRow, OpiskeluoikeusRow}
import fi.oph.koski.koskiuser.{KoskiSession, RequiresAuthentication}
import fi.oph.koski.log.KoskiMessageField.{apply => _, _}
import fi.oph.koski.log.KoskiOperation._
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusSortOrder.Ascending
import fi.oph.koski.schema.Henkilö.{apply => _, _}
import fi.oph.koski.schema.TäydellisetHenkilötiedot
import fi.oph.koski.servlet.{ApiServlet, ObservableSupport}
import org.scalatra._
import rx.lang.scala.Observable

trait OpiskeluoikeusQueries extends ApiServlet with RequiresAuthentication with Logging with GlobalExecutionContext with ObservableSupport with GZipSupport {
  def application: KoskiApplication

  def query(params: Map[String, String]): Observable[(TäydellisetHenkilötiedot, List[OpiskeluoikeusRow])] = {
    logger(koskiSession).info("Haetaan opiskeluoikeuksia: " + Option(request.getQueryString).getOrElse("ei hakuehtoja"))

    OpiskeluoikeusQueryFilter.parse(params.toList)(application.koodistoViitePalvelu, application.organisaatioRepository, koskiSession) match {
      case Right(filters) =>
        AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_HAKU, koskiSession, Map(hakuEhto -> params.toList.map { case (p,v) => p + "=" + v }.mkString("&"))))
        query(filters)(koskiSession)
      case Left(status) =>
        haltWithStatus(status)
    }
  }

  private def query(filters: List[OpiskeluoikeusQueryFilter])(implicit user: KoskiSession): Observable[(TäydellisetHenkilötiedot, List[OpiskeluoikeusRow])] = {
    val oikeudetPerOppijaOid: Observable[(Oid, List[OpiskeluoikeusRow])] = streamingQueryGroupedByOid(filters)
    oikeudetPerOppijaOid.tumblingBuffer(500).flatMap {
      oppijatJaOidit: Seq[(Oid, List[OpiskeluoikeusRow])] =>
        val oids: List[String] = oppijatJaOidit.map(_._1).toList

        val henkilöt: Map[String, TäydellisetHenkilötiedot] = application.henkilöRepository.findByOids(oids).map(henkilö => (henkilö.oid, henkilö)).toMap

        val oppijat: Iterable[(TäydellisetHenkilötiedot, List[OpiskeluoikeusRow])] = oppijatJaOidit.flatMap { case (oid, opiskeluOikeudet) =>
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

  def streamingQueryGroupedByOid(filters: List[OpiskeluoikeusQueryFilter])(implicit user: KoskiSession): Observable[(Oid, List[(OpiskeluoikeusRow)])] = {
    val rows = application.opiskeluoikeusQueryRepository.streamingQuery(filters, Some(Ascending(OpiskeluoikeusSortOrder.oppijaOid)), None)

    val groupedByPerson: Observable[List[(OpiskeluoikeusRow, HenkilöRow)]] = rows
      .tumblingBuffer(rows.map(_._1.oppijaOid).distinctUntilChanged.drop(1))
      .map(_.toList)

    groupedByPerson.flatMap {
      case oikeudet@(firstRow :: _) =>
        val oppijaOid = firstRow._1.oppijaOid
        assert(oikeudet.map(_._1.oppijaOid).toSet == Set(oppijaOid), "Usean ja/tai väärien henkilöiden tietoja henkilöllä " + oppijaOid + ": " + oikeudet)
        Observable.just((oppijaOid, oikeudet.toList.map(_._1)))
      case _ =>
        Observable.empty
    }
  }
}




