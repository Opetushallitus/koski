package fi.oph.koski.virta

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession, KäyttöoikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class VirtaAccessChecker(käyttöoikeudet: KäyttöoikeusRepository) extends AccessChecker {
  def hasAccess(user: KoskiSession) = {
    user.hasGlobalReadAccess ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(user)
      .intersect(Set(lastentarhaopettajaopistot, yliopistot, sotilaskorkeakoulut, kesäyliopistot, väliaikaisetAmmattikorkeakoulut, ammattikorkeakoulut))
      .nonEmpty
  }
}
