package fi.oph.koski.omaopintopolkuloki

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.huoltaja.{Huollettava, HuollettavienHakuOnnistui}
import fi.oph.koski.koskiuser.RequiresKansalainen
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

class OmaOpintoPolkuLokiServlet(implicit val application: KoskiApplication) extends
  RequiresKansalainen with KoskiSpecificApiServlet with NoCache {

  val auditLogs = new AuditLogService(application)

  get("/auditlogs") {
    renderEither(
      auditLogs.queryLogsFromDynamo(session.oid)
    )
  }

  get("/auditlogs/:hetu") {
    val requestedHetu = params("hetu")

    val huollettava: Option[Huollettava] = session.user.huollettavat.toList.flatMap {
      case r: HuollettavienHakuOnnistui => r.huollettavat
        .filter(_.hetu.contains(requestedHetu))
      case _ => Seq.empty
    }.headOption

    val oid = huollettava.map(_.oid.getOrElse("")).getOrElse("")

    renderEither(
      auditLogs.queryLogsFromDynamo(if (huollettava.isDefined) oid else session.oid)
    )
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
        huollettavat = if (huollettavat.isEmpty) None else Some(huollettavat)) // eikö tälle ole abstraktiota?
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
