package fi.oph.koski.koskiuser

trait AccessChecker {
  def hasAccess(user: KoskiSpecificSession): Boolean
  def hasGlobalAccess(user: KoskiSpecificSession): Boolean
}

object SkipAccessCheck extends AccessChecker {
  def hasAccess(user: KoskiSpecificSession): Boolean = true
  def hasGlobalAccess(user: KoskiSpecificSession): Boolean = true
}
