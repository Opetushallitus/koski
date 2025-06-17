package fi.oph.koski.virta

import fi.oph.koski.koskiuser.{AccessChecker, KoskiSpecificSession, KäyttöoikeusRepository, OoPtsMask}
import fi.oph.koski.organisaatio.Oppilaitostyyppi._
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi

/** Checks whether the user potentially has some access to Virta data. This is used for performance optimization: Virta
    fetch can be prevented if user has no access
*/
class VirtaAccessChecker(käyttöoikeudet: KäyttöoikeusRepository) extends AccessChecker {
  def hasAccess(user: KoskiSpecificSession): Boolean = {
    hasGlobalAccess(user) ||
    käyttöoikeudet.käyttäjänOppilaitostyypit(user.user)
      .intersect(Set(lastentarhaopettajaopistot, yliopistot, sotilaskorkeakoulut, kesäyliopistot, väliaikaisetAmmattikorkeakoulut, ammattikorkeakoulut))
      .nonEmpty
  }

  override def hasGlobalAccess(user: KoskiSpecificSession): Boolean =
    user.hasGlobalReadAccess || (user.hasGlobalKoulutusmuotoReadAccess && user.allowedOpiskeluoikeusTyypit.contains(OoPtsMask(OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)))
}
