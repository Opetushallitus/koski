package fi.oph.koski.ytr

import fi.oph.koski.koskiuser.{AccessChecker, KoskiUser, OppilaitostyypitRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class YtrAccessChecker(oppilaitostyypitRepository: OppilaitostyypitRepository) extends AccessChecker {
  def hasAccess(user: KoskiUser) = {
    oppilaitostyypitRepository.getUserOppilaitostyypit(user.oid)
      .intersect(Set(lukio, perusJaLukioasteenKoulut, muutOppilaitokset, kansanopistot))
      .nonEmpty
  }
}
