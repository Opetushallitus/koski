package fi.oph.tor.user

import fi.oph.tor.oppilaitos.Oppilaitos
import fi.oph.tor.organisaatio.OrganisaatioPuu

trait UserContext {
  def hasReadAccess(Oppilaitos: Oppilaitos): Boolean

  def organisaatioPuu: OrganisaatioPuu
}
