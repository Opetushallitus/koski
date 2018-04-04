package fi.oph.koski.koskiuser

import fi.oph.koski.servlet.{ApiServlet, NoCache}
import fi.oph.scalaschema.annotation.SyntheticProperty

class UserServlet(implicit val application: UserAuthenticationContext) extends ApiServlet with AuthenticationSupport with NoCache {
  get("/") {
    renderEither(getUser.right.map { user =>
      koskiSessionOption.map { session => {
        UserWithAccessRights(user.name, user.oid, session.hasAnyWriteAccess, session.hasLocalizationWriteAccess, session.hasGlobalReadAccess, session.hasAnyReadAccess, session.hasHenkiloUiWriteAccess, session.hasGlobalReadAccess || session.orgKäyttöoikeudet.nonEmpty)
      }
      }.getOrElse(UserWithAccessRights(user.name, user.oid))
    })
  }
}

case class UserWithAccessRights(name: String, oid: String, hasWriteAccess: Boolean = false, hasLocalizationWriteAccess: Boolean = false, hasGlobalReadAccess: Boolean = false, hasAnyReadAccess: Boolean = false, hasHenkiloUiWriteAccess: Boolean = false, showOppijalistaus: Boolean = false)

