package fi.oph.koski.koskiuser

import fi.oph.koski.servlet.{ApiServlet, NoCache}

class UserServlet(implicit val application: UserAuthenticationContext) extends ApiServlet with AuthenticationSupport with NoCache {
  get("/") {
    renderEither(getUser.right.map { user =>
      koskiSessionOption.map { session =>
        UserWithAccessRights(user.name, session.hasAnyWriteAccess, session.hasLocalizationWriteAccess, session.hasGlobalReadAccess)
      }.getOrElse(UserWithAccessRights(user.name))
    })
  }
}

case class UserWithAccessRights(name: String, hasWriteAccess: Boolean = false, hasLocalizationWriteAccess: Boolean = false, hasGlobalReadAccess: Boolean = false)
