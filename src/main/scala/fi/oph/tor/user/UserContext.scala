package fi.oph.tor.user

import fi.oph.tor.organisaatio.OrganisaatioPuu
import fi.oph.tor.schema.Organisaatio

trait UserContext {
  def hasReadAccess(oppilaitos: Organisaatio): Boolean

  def organisaatioPuu: OrganisaatioPuu
}
