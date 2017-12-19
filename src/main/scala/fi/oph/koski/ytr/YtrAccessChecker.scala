package fi.oph.koski.ytr

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession, KäyttöoikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

/** Checks whether the user potentially has some access to YTR data. This is used for performance optimization: YTR
    fetch can be prevented if user has no access
*/
class YtrAccessChecker(käyttöoikeudet: KäyttöoikeusRepository) extends AccessChecker {
  def hasAccess(session: KoskiSession) = {
    session.hasGlobalReadAccess ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(session.user)
      .intersect(Set(lukio, perusJaLukioasteenKoulut, muutOppilaitokset, kansanopistot))
      .nonEmpty
  }
}
