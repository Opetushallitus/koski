package fi.oph.koski.virta

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession, KayttooikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class VirtaAccessChecker(käyttöoikeudet: KayttooikeusRepository) extends AccessChecker {
  def hasAccess(user: KoskiSession) = {
    user.hasGlobalReadAccess ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(user)
      .intersect(Set(lastentarhaopettajaopistot, yliopistot, sotilaskorkeakoulut, kesäyliopistot, väliaikaisetAmmattikorkeakoulut, ammattikorkeakoulut))
      .nonEmpty
  }
}
