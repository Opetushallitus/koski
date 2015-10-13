package fi.oph.tor.user

import fi.oph.tor.organisaatio.OrganisaatioPuu
import fi.vm.sade.utils.memoize._
class CachingUserRepository(repository: UserRepository) extends UserRepository {
  private val cache = TTLOptionalMemoize.memoize[String, OrganisaatioPuu]({oid => Some(repository.getUserOrganisations(oid))}, "organisaatiopuu", 1000 * 3600, 1000)
  override def getUserOrganisations(oid: String) = cache(oid).get
}
