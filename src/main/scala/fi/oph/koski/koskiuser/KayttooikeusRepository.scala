package fi.oph.koski.koskiuser

import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.organisaatio.{OrganisaatioHierarkia, OrganisaatioRepository}
import fi.oph.koski.schema.OidOrganisaatio
import fi.oph.koski.userdirectory.DirectoryClient
import fi.oph.koski.util.Timing

import scala.concurrent.duration._

class KäyttöoikeusRepository(organisaatioRepository: OrganisaatioRepository, directoryClient: DirectoryClient)(implicit cacheInvalidator: CacheManager) extends Timing {
  def käyttäjänKäyttöoikeudet(user: AuthenticationUser): Set[Käyttöoikeus] = käyttöoikeusCache(user)

  def käyttäjänOppilaitostyypit(user: AuthenticationUser): Set[String] = {
    val käyttöoikeudet: Set[Käyttöoikeus] = käyttöoikeusCache(user)
    käyttöoikeudet.collect { case KäyttöoikeusOrg(_, _, _, Some(oppilaitostyyppi)) => oppilaitostyyppi }
  }

  private def haeKäyttöoikeudet(user: AuthenticationUser): Set[Käyttöoikeus] = {
    val username = user.username
    directoryClient.findUser(username) match {
      case Some(ldapUser) =>
        ldapUser.käyttöoikeudet.toSet.flatMap { k: Käyttöoikeus =>
          k match {
            case k: KäyttöoikeusGlobal => List(k)
            case k: KäyttöoikeusViranomainen => List(k)
            case k: KäyttöoikeusOrg =>
              val organisaatioHierarkia = organisaatioRepository.getOrganisaatioHierarkia(k.organisaatio.oid)
              val flattened = OrganisaatioHierarkia.flatten(organisaatioHierarkia.toList)

              if (flattened.isEmpty) {
                logger.warn(s"Käyttäjän $username käyttöoikeus $k kohdistuu organisaatioon ${k.organisaatio.oid}, jota ei löydy")
              }

              val käyttöoikeudet = flattened.map { org =>
                k.copy(organisaatio = org.toOrganisaatio, juuri = org.oid == k.organisaatio.oid, oppilaitostyyppi = org.oppilaitostyyppi)
              }

              val hierarkianUlkopuolisetOikeudet = organisaatioHierarkia.toList.flatMap(hierarkianUlkopuolisetKäyttöoikeudet(k, _)).filterNot { käyttöoikeus =>
                käyttöoikeudet.exists(_.organisaatio.oid == käyttöoikeus.ulkopuolinenOrganisaatio.oid)
              }

              käyttöoikeudet ++ hierarkianUlkopuolisetOikeudet
          }
        }
      case None =>
        if (!user.kansalainen) {
          logger.warn(s"User $username not found")
        }
        Set.empty
    }
  }

  private def hierarkianUlkopuolisetKäyttöoikeudet(k: KäyttöoikeusOrg, organisaatioHierarkia: OrganisaatioHierarkia) =
    if (organisaatioHierarkia.varhaiskasvatuksenJärjestäjä && organisaatioHierarkia.toKoulutustoimija.isDefined) {
      organisaatioRepository.findAllVarhaiskasvatusToimipisteet.map { päiväkoti =>
        KäyttöoikeusVarhaiskasvatusToimipiste(
          koulutustoimija = organisaatioHierarkia.toKoulutustoimija.get,
          ulkopuolinenOrganisaatio = OidOrganisaatio(päiväkoti.oid),
          organisaatiokohtaisetPalveluroolit = k.organisaatiokohtaisetPalveluroolit
        )
      }
    } else {
      Nil
    }

  private lazy val käyttöoikeusCache = new KeyValueCache[AuthenticationUser, Set[Käyttöoikeus]](
    ExpiringCache("KäyttöoikeusRepository", 5.minutes, 100), haeKäyttöoikeudet
  )
}
