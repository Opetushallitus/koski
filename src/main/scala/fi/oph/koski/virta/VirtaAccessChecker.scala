package fi.oph.koski.virta

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession, KayttooikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

class VirtaAccessChecker(käyttöoikeudet: KayttooikeusRepository) extends AccessChecker {
  def hasAccess(session: KoskiSession) = {
    session.hasGlobalReadAccess ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(session.user)
      .intersect(Set(lastentarhaopettajaopistot, yliopistot, sotilaskorkeakoulut, kesäyliopistot, väliaikaisetAmmattikorkeakoulut, ammattikorkeakoulut))
      .nonEmpty
  }
}
