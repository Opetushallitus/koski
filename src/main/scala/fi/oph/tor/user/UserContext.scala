package fi.oph.tor.user

import fi.oph.tor.organisaatio.OrganisaatioPuu

trait UserContext {
  def hasReadAccess(organisaatioId: String): Boolean

  def organisaatioPuu: OrganisaatioPuu
}
