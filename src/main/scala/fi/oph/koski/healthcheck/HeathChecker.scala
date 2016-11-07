package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.AccessType
import fi.oph.koski.koskiuser.KoskiSession._
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{OidHenkilö, Oppija}

case class HeathChecker(application: KoskiApplication) extends Logging {
  private implicit val user = systemUser
  private implicit val accessType = AccessType.write
  private val oid = application.config.getString("healthcheck.oppija.oid")
  private lazy val oppija: Oppija = application.validator.validateAsJson(Oppija(OidHenkilö(oid), List(perustutkintoOpiskeluoikeus()))).right.get

  def healthcheck: HttpStatus = try {
    application.facade.findOppija(oid) match {
      case Left(HttpStatus(404, _)) =>
        logger.info(s"Healtcheck user not found creating one with oid $oid")
        application.facade.createOrUpdate(oppija) match {
          case Left(status) =>
            logger.error(s"Problem creating healthchech oppija ${status.toString}")
            status
          case _ =>
            HttpStatus.ok
        }
      case Left(status) => status
      case _ => HttpStatus.ok
    }
  } catch {
    case e: Exception =>
      logger.error(e)("healthcheck failed")
      KoskiErrorCategory.internalError("healthcheck failed")
  }
}
