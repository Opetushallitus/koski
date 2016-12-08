package fi.oph.koski.koskiuser

import fi.oph.koski.cache.{Cache, CacheManager, KeyValueCache}
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.organisaatio.{OrganisaatioHierarkia, OrganisaatioRepository}
import fi.oph.koski.util.Timing
import fi.vm.sade.security.ldap.DirectoryClient

class KäyttöoikeusRepository(authenticationServiceClient: AuthenticationServiceClient, organisaatioRepository: OrganisaatioRepository, directoryClient: DirectoryClient)(implicit cacheInvalidator: CacheManager) extends Timing {
  def käyttäjänKäyttöoikeudet(user: AuthenticationUser): Set[Käyttöoikeus] = käyttöoikeusCache(user)

  def käyttäjänOppilaitostyypit(user: AuthenticationUser): Set[String] = {
    val käyttöoikeudet: Set[Käyttöoikeus] = käyttöoikeusCache(user)
    käyttöoikeudet.collect { case KäyttöoikeusOrg(_, _, _, Some(oppilaitostyyppi)) => oppilaitostyyppi }
  }

  private def haeKäyttöoikeudet(user: AuthenticationUser): Set[Käyttöoikeus] = {
    val username = user.username
    directoryClient.findUser(username) match {
      case Some(ldapUser) =>
        LdapKayttooikeudet.käyttöoikeudet(ldapUser).toSet.flatMap { k: Käyttöoikeus =>
          k match {
            case k: KäyttöoikeusGlobal =>
              List(k)
            case k: KäyttöoikeusOrg =>
              val organisaatioHierarkia = organisaatioRepository.getOrganisaatioHierarkia(k.organisaatio.oid)
              val flattened = OrganisaatioHierarkia.flatten(organisaatioHierarkia.toList)
              if (flattened.isEmpty) {
                logger.warn(s"Käyttäjän $username käyttöoikeus ${k} kohdistuu organisaatioon ${k.organisaatio.oid}, jota ei löydy")
              }
              flattened.map { org =>
                k.copy(organisaatio = org.toOrganisaatio, juuri = org.oid == k.organisaatio.oid, oppilaitostyyppi = org.oppilaitostyyppi)
              }
          }
        }
      case None =>
        logger.warn(s"User $username not found from LDAP")
        Set.empty
    }
  }

  private lazy val käyttöoikeusCache = new KeyValueCache[AuthenticationUser, Set[Käyttöoikeus]](
    Cache.cacheAllNoRefresh("KäyttöoikeusRepository", 5 * 60, 100), haeKäyttöoikeudet
  )
}