package fi.oph.koski.permission

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.Unauthenticated
import fi.oph.koski.schema.{Henkilö, Organisaatio}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

case class PermissionCheckRequest(personOidsForSamePerson: List[Henkilö.Oid], organisationOids: List[Organisaatio.Oid], loggedInUserRoles: List[String])

case class PermissionCheckResponse(accessAllowed: Boolean, errorMessage: Option[String] = None)

class PermissionCheckServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with Unauthenticated {
  post("/checkpermission") {
    withJsonBody({ body =>
      logger.warn(s"checkpermission called with ${JsonSerializer.writeWithRoot(body)}")
      val request = JsonSerializer.extract[PermissionCheckRequest](body)
      val testPersonOid = "1.2.246.562.24.17144103694"
      renderObject(PermissionCheckResponse(accessAllowed = request.personOidsForSamePerson.contains(testPersonOid)))
    })()
  }
}
