package fi.oph.koski.virta

import fi.oph.koski.koskiuser.{AccessChecker, KoskiUser, OppilaitostyypitRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class VirtaAccessChecker(oppilaitostyypitRepository: OppilaitostyypitRepository) extends AccessChecker {
  def hasAccess(user: KoskiUser) = {
    oppilaitostyypitRepository.getUserOppilaitostyypit(user.oid)
      .intersect(Set(lastentarhaopettajaopistot, yliopistot, sotilaskorkeakoulut, kesäyliopistot, väliaikaisetAmmattikorkeakoulut, ammattikorkeakoulut))
      .nonEmpty
  }
}
