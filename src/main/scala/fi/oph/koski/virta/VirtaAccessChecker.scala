package fi.oph.koski.virta

import fi.oph.koski.koskiuser.{AccessChecker, KoskiUser, UserOrganisationsRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class VirtaAccessChecker(oppilaitostyypitRepository: UserOrganisationsRepository) extends AccessChecker {
  def hasAccess(user: KoskiUser) = {
    oppilaitostyypitRepository.getUserOppilaitostyypit(user.oid)
      .intersect(Set(lastentarhaopettajaopistot, yliopistot, sotilaskorkeakoulut, kesäyliopistot, väliaikaisetAmmattikorkeakoulut, ammattikorkeakoulut))
      .nonEmpty
  }
}
