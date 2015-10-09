package fi.oph.tor.user

import fi.oph.tor.organisaatio.OrganisaatioPuu

trait UserContext {
  def organisaatioPuu: OrganisaatioPuu
}
