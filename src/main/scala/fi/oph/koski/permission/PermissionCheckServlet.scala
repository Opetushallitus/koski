package fi.oph.koski.permission

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilöOid
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, Unauthenticated}
import fi.oph.koski.schema.{Henkilö, Opiskeluoikeus, Organisaatio}
import fi.oph.koski.servlet.{KoskiSpecificApiServlet, NoCache}

import java.time.LocalDate

case class PermissionCheckRequest(personOidsForSamePerson: List[Henkilö.Oid], organisationOids: List[Organisaatio.Oid], loggedInUserRoles: List[String])

case class PermissionCheckResponse(accessAllowed: Boolean, errorMessage: Option[String] = None)

class PermissionCheckServlet(implicit val application: KoskiApplication) extends KoskiSpecificApiServlet with NoCache with Unauthenticated {
  post("/checkpermission") {
    withJsonBody({ body =>
      val request = JsonSerializer.extract[PermissionCheckRequest](body, ignoreExtras = true)
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
      .flatMap(o => application.oppijaFacade.findOppija(o)(KoskiSpecificSession.systemUser).toOption.toSeq)
      .flatMap(_.getIgnoringWarnings.opiskeluoikeudet)
  }

  private def isRelevantForAccessCheck(opiskeluoikeus: Opiskeluoikeus): Boolean = {
    opiskeluoikeus.tila
      .opiskeluoikeusjaksot
      .filterNot(_.alku.isAfter(LocalDate.now()))
      .lastOption
      .forall(!_.opiskeluoikeusPäättynyt)
  }

  // Note 1: keep in sync with KoskiSession's hasHenkiloUiWriteAccess function
  // Note 2: Tämä logiikka saattaa kaivata päivitystä jos käyttöoikeusryhmien sisältö muuttuu.
  private def hasSufficientRoles(roles: List[String]): Boolean = {
    roles.contains("ROLE_APP_KOSKI") && (roles.contains("ROLE_APP_OPPIJANUMEROREKISTERI_REKISTERINPITAJA") || roles.contains("ROLE_APP_OPPIJANUMEROREKISTERI_HENKILON_RU"))
  }
}
