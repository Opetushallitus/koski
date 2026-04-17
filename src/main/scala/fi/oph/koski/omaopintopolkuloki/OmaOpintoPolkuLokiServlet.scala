package fi.oph.koski.omaopintopolkuloki

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.huoltaja.{HuollettavienHakuOnnistui}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.log.Logging
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

case class AuditlogRequest(
  hetu: Option[String]
)

class OmaOpintoPolkuLokiServlet(implicit val application: KoskiApplication) extends
  RequiresKansalainen with KoskiSpecificApiServlet with NoCache with Logging {

  val auditLogs = new AuditLogService(application)

  post("/auditlogs") {
    withJsonBody({ body =>
      val request = JsonSerializer.extract[AuditlogRequest](body)
      val requestedHetu = request.hetu

      val personOid: String = session.user.huollettavat.toList.flatMap {
        case r: HuollettavienHakuOnnistui => r.huollettavat
          .filter(_.hetu.exists(requestedHetu.contains))
          .map(h => h.oid)
          .flatMap {
            case o: Some[String] => List(o.get)
            case _ => List.empty
          }
        case _ => List.empty
      }.headOption.getOrElse(session.oid)

      val allowedOrganisaatiot = loadAllowedOrganisaatiot(personOid)

      renderEither(
        auditLogs.queryLogsFromDynamo(personOid, allowedOrganisaatiot)
      )
    })()
  }

  private def loadAllowedOrganisaatiot(personOid: String): AllowedOrganisaatiot = {
    val oppijaResult =
      if (personOid == session.oid) application.huoltajaService.findUserOppijaAllowEmpty(session)
      else application.huoltajaService.findHuollettavaOppija(personOid)(session)

    oppijaResult match {
      case Right(withWarnings) =>
        AuditLogService.allowedOrganisaatiot(withWarnings.getIgnoringWarnings)
      case Left(status) =>
        logger.warn(s"Opiskeluoikeuksien lataus oma-opintopolku-lokin organisaatiosuodatusta varten epäonnistui (personOid=$personOid): $status")
        AllowedOrganisaatiot(Set.empty, Set.empty)
    }
  }

  get("/whoami") {
    application.opintopolkuHenkilöFacade.findOppijaByOid(session.oid).map(h => {
      val huollettavat: List[OmaOpintopolkuLokiHenkiloTiedot] = session.user.huollettavat.toList.flatMap {
        case r: HuollettavienHakuOnnistui => r.huollettavat
          .map(hh =>
            OmaOpintopolkuLokiHenkiloTiedot(
              hetu = hh.hetu,
              etunimet = hh.etunimet,
              kutsumanimi = hh.etunimet, // fixme
              sukunimi = hh.sukunimi,
              huollettavat = None
            )
          )
        case _ => Seq.empty
      }
      OmaOpintopolkuLokiHenkiloTiedot(
        h.hetu,
        h.etunimet,
        h.kutsumanimi,
        h.sukunimi,
        huollettavat = if (huollettavat.isEmpty) None else Some(huollettavat))
    })
  }
}

case class OmaOpintopolkuLokiHenkiloTiedot(
  hetu: Option[String],
  etunimet: String,
  kutsumanimi: String,
  sukunimi: String,
  huollettavat: Option[List[OmaOpintopolkuLokiHenkiloTiedot]]
)
