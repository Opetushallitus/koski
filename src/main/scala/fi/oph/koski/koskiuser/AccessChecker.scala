package fi.oph.koski.koskiuser

trait AccessChecker {
  def hasAccess(user: KoskiUser): Boolean
}

object SkipAccessCheck extends AccessChecker {
  def hasAccess(user: KoskiUser) = true
}
