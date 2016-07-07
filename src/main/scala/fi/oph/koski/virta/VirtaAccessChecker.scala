package fi.oph.koski.virta

import fi.oph.koski.koskiuser.{AccessChecker, KoskiUser, KäyttöoikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class VirtaAccessChecker(käyttöoikeudet: KäyttöoikeusRepository) extends AccessChecker {
  def hasAccess(user: KoskiUser) = {
    käyttöoikeudet.käyttäjänOppilaitostyypit(user.oid)
      .intersect(Set(lastentarhaopettajaopistot, yliopistot, sotilaskorkeakoulut, kesäyliopistot, väliaikaisetAmmattikorkeakoulut, ammattikorkeakoulut))
      .nonEmpty
  }
}
