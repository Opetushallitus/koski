package fi.oph.koski.ytr.download

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.{KoskiTables, QueryMethods}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificAuthenticationSupport, KoskiSpecificSession, RequiresVirkailijaOrPalvelukäyttäjä}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}
import fi.oph.koski.sso.KoskiSpecificSSOSupport
import slick.dbio.DBIO
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import org.json4s.JsonAST.{JBool, JObject}

import java.time.LocalDate

class YtrTestServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with KoskiSpecificAuthenticationSupport with KoskiSpecificSSOSupport with RequiresVirkailijaOrPalvelukäyttäjä with NoCache with QueryMethods {
  val db = application.masterDatabase.db

  private val downloadService = application.ytrDownloadService

  get("/clear") {
    implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUserTallennetutYlioppilastutkinnonOpiskeluoikeudet
    implicit val accessType: AccessType.Value = AccessType.write

    logger.info("Clearing YTR data")

    // TODO: TOR-1639 Kun raportointikannan luonti tehdään, tuhoa myös sen generointiin liittyvät taulut
    runDbSync(DBIO.sequence(Seq(
      KoskiTables.YtrOpiskeluoikeusHistoria.delete,
      KoskiTables.YtrOpiskeluOikeudet.delete
    )))

    renderObject(JObject("ok" -> JBool(true)))
  }

  get("/download") {
    logger.info("Download YTR started")
    downloadService.download(
      birthmonthStart = getOptionalStringParam("birthmonthStart")
        .map(m => {
          LocalDate.parse(m + "-01")
          m
        }),
      birthmonthEnd = getOptionalStringParam("birthmonthEnd")
        .map(m => {
          LocalDate.parse(m + "-01")
          m
        }),
      modifiedSince = getOptionalStringParam("modifiedSince").map(LocalDate.parse),
      force = getOptionalBooleanParam("force").getOrElse(false),
      onEnd = () => {
        logger.info("Download YTR done")
      }
    )
    renderObject(downloadService.status.getDownloadStatusJson)
  }
}
