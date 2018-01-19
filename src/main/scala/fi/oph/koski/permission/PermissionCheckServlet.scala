package fi.oph.koski.permission

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSession, Unauthenticated}
import fi.oph.koski.log.LogUserContext
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus, Organisaatio}
import fi.oph.koski.servlet.{ApiServlet, NoCache}

case class PermissionCheckRequest(personOidsForSamePerson: List[Henkilö.Oid], organisationOids: List[Organisaatio.Oid], loggedInUserRoles: List[String])

case class PermissionCheckResponse(accessAllowed: Boolean, errorMessage: Option[String] = None)

class PermissionCheckServlet(implicit val application: KoskiApplication) extends ApiServlet with NoCache with Unauthenticated {
  post("/checkpermission") {
    withJsonBody({ body =>
      val request = JsonSerializer.extract[PermissionCheckRequest](body)
      renderObject(checkPermission(request))
    })()
  }

  private def checkPermission(request: PermissionCheckRequest): PermissionCheckResponse = {
    val opiskeluoikeudet: Seq[Opiskeluoikeus] = request.personOidsForSamePerson.flatMap(getOpiskeluoikeudet)
    val organisaatioOidit: Set[Organisaatio.Oid] = opiskeluoikeudet.filter(isRelevantForAccessCheck).flatMap(_.oppilaitos).map(_.oid).toSet
    val sallitutOrganisaatioOidit: Set[Organisaatio.Oid] = organisaatioOidit.intersect(request.organisationOids.toSet)
    PermissionCheckResponse(accessAllowed = sallitutOrganisaatioOidit.nonEmpty && hasSufficientRoles(request.loggedInUserRoles))
  }

  private def getOpiskeluoikeudet(oid: Henkilö.Oid): Seq[Opiskeluoikeus] = {
    HenkilöOid.validateHenkilöOid(oid)
      .toSeq
      .flatMap(application.opiskeluoikeusRepository.findByOppijaOid(_)(KoskiSession.systemUser))
  }

  private def isRelevantForAccessCheck(opiskeluoikeus: Opiskeluoikeus): Boolean = {
    opiskeluoikeus.tila.opiskeluoikeusjaksot.lastOption.forall(!_.opiskeluoikeusPäättynyt)
  }

  // Note 1: keep in sync with KoskiSession's hasHenkiloUiWriteAccess function
  // Note 2: Tämä logiikka saattaa kaivata päivitystä jos käyttöoikeusryhmien sisältö muuttuu.
  private def hasSufficientRoles(roles: List[String]): Boolean = {
    roles.contains("ROLE_APP_KOSKI") && (roles.contains("ROLE_APP_HENKILONHALLINTA_CRUD") || roles.contains("ROLE_APP_HENKILONHALLINTA_READ_UPDATE"))
  }
}
