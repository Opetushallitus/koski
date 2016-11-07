package fi.oph.koski.koskiuser

trait AccessChecker {
  def hasAccess(user: KoskiSession): Boolean
}

object SkipAccessCheck extends AccessChecker {
  def hasAccess(user: KoskiSession) = true
}
