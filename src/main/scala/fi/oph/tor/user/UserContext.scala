package fi.oph.tor.user

import fi.oph.tor.oppilaitos.OppilaitosOrId
import fi.oph.tor.organisaatio.OrganisaatioPuu

trait UserContext {
  def hasReadAccess(Oppilaitos: OppilaitosOrId): Boolean

  def organisaatioPuu: OrganisaatioPuu
}
