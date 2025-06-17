package fi.oph.koski.ytr

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSpecificSession, KäyttöoikeusRepository, OoPtsMask}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi

/** Checks whether the user potentially has some access to YTR data. This is used for performance optimization: YTR
    fetch can be prevented if user has no access
*/
class YtrAccessChecker(käyttöoikeudet: KäyttöoikeusRepository) extends AccessChecker {
  def hasAccess(user: KoskiSpecificSession): Boolean = {
    hasGlobalAccess(user) ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(user.user)
      .intersect(Set(lukio, perusJaLukioasteenKoulut, muutOppilaitokset, kansanopistot))
      .nonEmpty
  }

  def hasGlobalAccess(user: KoskiSpecificSession): Boolean =
    user.hasGlobalReadAccess || (user.hasGlobalKoulutusmuotoReadAccess && user.allowedOpiskeluoikeusTyypit.contains(OoPtsMask(OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)))
}
