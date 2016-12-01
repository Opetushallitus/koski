package fi.oph.koski.ytr

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession, KayttooikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class YtrAccessChecker(käyttöoikeudet: KayttooikeusRepository) extends AccessChecker {
  def hasAccess(session: KoskiSession) = {
    session.hasGlobalReadAccess ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(session.user)
      .intersect(Set(lukio, perusJaLukioasteenKoulut, muutOppilaitokset, kansanopistot))
      .nonEmpty
  }
}
