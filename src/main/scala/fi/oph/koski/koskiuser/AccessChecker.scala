package fi.oph.koski.koskiuser

trait AccessChecker {
  def hasAccess(user: KoskiUser): Boolean
}

object SkipAccesCheck extends AccessChecker {
  def hasAccess(user: KoskiUser) = true
}
