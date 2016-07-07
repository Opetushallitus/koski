package fi.oph.koski.ytr

import fi.oph.koski.koskiuser.{AccessChecker, KoskiUser, KäyttöoikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class YtrAccessChecker(käyttöoikeudet: KäyttöoikeusRepository) extends AccessChecker {
  def hasAccess(user: KoskiUser) = {
    käyttöoikeudet.käyttäjänOppilaitostyypit(user.oid)
      .intersect(Set(lukio, perusJaLukioasteenKoulut, muutOppilaitokset, kansanopistot))
      .nonEmpty
  }
}
