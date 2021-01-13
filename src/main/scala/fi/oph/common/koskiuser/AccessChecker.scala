package fi.oph.common.koskiuser

trait AccessChecker {
  def hasAccess(user: KoskiSession): Boolean
  def hasGlobalAccess(user: KoskiSession): Boolean
}

object SkipAccessCheck extends AccessChecker {
  def hasAccess(user: KoskiSession): Boolean = true
  def hasGlobalAccess(user: KoskiSession): Boolean = true
}
