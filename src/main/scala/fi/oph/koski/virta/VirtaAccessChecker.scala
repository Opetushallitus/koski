package fi.oph.koski.virta

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSession, KäyttöoikeusRepository}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._

/** Checks whether the user potentially has some access to Virta data. This is used for performance optimization: Virta
    fetch can be prevented if user has no access
*/
class VirtaAccessChecker(käyttöoikeudet: KäyttöoikeusRepository) extends AccessChecker {
  def hasAccess(session: KoskiSession) = {
    session.hasGlobalReadAccess ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(session.user)
      .intersect(Set(lastentarhaopettajaopistot, yliopistot, sotilaskorkeakoulut, kesäyliopistot, väliaikaisetAmmattikorkeakoulut, ammattikorkeakoulut))
      .nonEmpty
  }
}
