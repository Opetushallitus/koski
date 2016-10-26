package fi.oph.koski.healthcheck

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.AmmatillinenExampleData.perustutkintoOpiskeluoikeus
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiUser.systemUser
import fi.oph.koski.koskiuser.{AccessType, KoskiUser}
import fi.oph.koski.schema.{OidHenkilö, Oppija}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

class HealthCheckApiServlet(val application: KoskiApplication) extends ApiServlet with NoCache {
  private implicit val user = systemUser
  private implicit val accessType = AccessType.write
  private val oid = application.config.getString("healthcheck.oppija.oid")
  private lazy val oppija: Oppija = application.validator.validateAsJson(Oppija(OidHenkilö(oid), List(perustutkintoOpiskeluoikeus()))).right.get

  get() {
    HeathChecker(application).healthcheck match {
      case Left(HttpStatus(404, _)) =>
        logger.info(s"Healtcheck user not found creating one with oid $oid")
        application.facade.createOrUpdate(oppija) match {
          case Left(status) =>
            logger.error(s"Problem creating healthchech oppija ${status.toString}")
            renderStatus(status.statusCode)
          case _ => renderStatus(200)
        }
      case Left(status) => renderStatus(status.statusCode)
      case _ => renderStatus(200)
    }
  }

  override def koskiUserOption: Option[KoskiUser] = Some(systemUser)

  private def renderStatus(sc: Int): Unit = renderObject(Map("status" -> sc))
}

