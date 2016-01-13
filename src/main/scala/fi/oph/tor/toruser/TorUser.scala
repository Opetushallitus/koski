package fi.oph.tor.toruser

import fi.oph.tor.organisaatio.InMemoryOrganisaatioRepository

case class TorUser(oid: String, userOrganisations: InMemoryOrganisaatioRepository)