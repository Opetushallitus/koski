package fi.oph.koski.ytr

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession, KäyttöoikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class YtrAccessChecker(käyttöoikeudet: KäyttöoikeusRepository) extends AccessChecker {
  def hasAccess(user: KoskiSession) = {
    user.hasGlobalReadAccess ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(user)
      .intersect(Set(lukio, perusJaLukioasteenKoulut, muutOppilaitokset, kansanopistot))
      .nonEmpty
  }
}
