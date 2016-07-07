package fi.oph.koski.ytr

import fi.oph.koski.koskiuser.{AccessChecker, KoskiUser, UserOrganisationsRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class YtrAccessChecker(oppilaitostyypitRepository: UserOrganisationsRepository) extends AccessChecker {
  def hasAccess(user: KoskiUser) = {
    oppilaitostyypitRepository.getUserOppilaitostyypit(user.oid)
      .intersect(Set(lukio, perusJaLukioasteenKoulut, muutOppilaitokset, kansanopistot))
      .nonEmpty
  }
}
