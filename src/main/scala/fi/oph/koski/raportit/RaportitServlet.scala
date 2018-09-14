package fi.oph.koski.raportit

import java.time.LocalDate
import java.time.format.DateTimeParseException

import fi.oph.koski.config.{Environment, KoskiApplication}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.RequiresVirkailijaOrPalvelukäyttäjä
import fi.oph.koski.log.KoskiMessageField.hakuEhto
import fi.oph.koski.log.KoskiOperation.OPISKELUOIKEUS_RAPORTTI
import fi.oph.koski.log.{AuditLog, AuditLogMessage, Logging}
import fi.oph.koski.organisaatio.OrganisaatioOid
import fi.oph.koski.servlet.{ApiServlet, NoCache}
import org.scalatra.{ContentEncodingSupport, Cookie, CookieOptions}

class RaportitServlet(implicit val application: KoskiApplication) extends ApiServlet with RequiresVirkailijaOrPalvelukäyttäjä with Logging with NoCache with ContentEncodingSupport {

  private lazy val raportointiDatabase = application.raportointiDatabase

  get("/mahdolliset-raportit/:oppilaitosOid") {
    val oppilaitosOid = OrganisaatioOid.validateOrganisaatioOid(getStringParam("oppilaitosOid")) match {
      case Left(error) => haltWithStatus(error)
      case Right(oid) => oid
    }
    val koulutusmuodot = raportointiDatabase.oppilaitoksenKoulutusmuodot(oppilaitosOid)
    if (koulutusmuodot.contains("ammatillinenkoulutus")) Seq("opiskelijavuositiedot") else Seq.empty
  }

  get("/opiskelijavuositiedot") {
    
    val loadCompleted = raportointiDatabase.fullLoadCompleted(raportointiDatabase.statuses)
    if (loadCompleted.isEmpty) {
      haltWithStatus(KoskiErrorCategory.unavailable.raportit())
    }

    if (!koskiSession.hasRaportitAccess) {
      haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
    }

    val oppilaitosOid = OrganisaatioOid.validateOrganisaatioOid(getStringParam("oppilaitosOid")) match {
      case Left(error) => haltWithStatus(error)
      case Right(oid) if !koskiSession.hasReadAccess(oid) => haltWithStatus(KoskiErrorCategory.forbidden.organisaatio())
      case Right(oid) => oid
    }
    val (alku, loppu) = try {
      (LocalDate.parse(getStringParam("alku")), LocalDate.parse(getStringParam("loppu")))
    } catch {
      case e: DateTimeParseException => haltWithStatus(KoskiErrorCategory.badRequest.format.pvm())
    }
    if (loppu.isBefore(alku)) {
      haltWithStatus(KoskiErrorCategory.badRequest.format.pvm("loppu ennen alkua"))
    }
    val password = params.get("password")
    val downloadToken = params.get("downloadToken")

    // temporary restriction
    if (application.config.getStringList("oppijavuosiraportti.enabledForUsers").indexOf(koskiSession.username) < 0) {
      haltWithStatus(KoskiErrorCategory.forbidden("Ei sallittu tälle käyttäjälle"))
    }

    AuditLog.log(AuditLogMessage(OPISKELUOIKEUS_RAPORTTI, koskiSession, Map(hakuEhto -> s"raportti=opiskelijavuositiedot&oppilaitosOid=$oppilaitosOid&alku=$alku&loppu=$loppu")))

    val rows = Opiskelijavuositiedot.buildRaportti(raportointiDatabase, oppilaitosOid, alku, loppu)

    if (Environment.isLocalDevelopmentEnvironment && params.contains("text")) {
      contentType = "text/plain"
      response.writer.print(rows.map(_.toString).mkString("\n\n"))
    } else {
      contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      response.setHeader("Content-Disposition", s"""attachment; filename="${Opiskelijavuositiedot.filename(oppilaitosOid, alku, loppu)}"""")
      downloadToken.foreach { t => response.addCookie(Cookie("koskiDownloadToken", t)(CookieOptions(path = "/", maxAge = 600))) }
      ExcelWriter.writeExcel(
        WorkbookSettings(Opiskelijavuositiedot.title(oppilaitosOid, alku, loppu), password),
        Seq(
          DataSheet("Opiskeluoikeudet", rows, Opiskelijavuositiedot.columnSettings),
          DocumentationSheet("Ohjeet", Opiskelijavuositiedot.documentation(oppilaitosOid, alku, loppu, loadCompleted.get))
        ),
        response.getOutputStream
      )
    }
  }
}
