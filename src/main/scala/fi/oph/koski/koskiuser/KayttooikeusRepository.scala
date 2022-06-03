package fi.oph.koski.koskiuser

import fi.oph.koski.cache.{CacheManager, ExpiringCache, KeyValueCache}
import fi.oph.koski.organisaatio.{OrganisaatioHierarkia, OrganisaatioRepository}
import fi.oph.koski.userdirectory.DirectoryClient
import fi.oph.koski.util.Timing

import scala.concurrent.duration.DurationInt

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
            case k: KäyttöoikeusOrg => organisaatioKäyttöoikeudet(username, k)
          }
        }
      case None =>
        if (!user.kansalainen) {
          logger.warn(s"User $username not found")
        }
        Set.empty
    }
  }

  private def organisaatioKäyttöoikeudet(username: String, käyttöoikeus: KäyttöoikeusOrg) = {
    val organisaatioHierarkia = organisaatioRepository.getOrganisaatioHierarkia(käyttöoikeus.organisaatio.oid)
    val flattened = OrganisaatioHierarkia.flatten(organisaatioHierarkia.toList)

    if (flattened.isEmpty) {
      logger.warn(s"Käyttäjän $username käyttöoikeus $käyttöoikeus kohdistuu organisaatioon ${käyttöoikeus.organisaatio.oid}, jota ei löydy")
    }

    val käyttöoikeudet = flattened.map { org =>
      käyttöoikeus.copy(organisaatio = org.toOrganisaatio, juuri = org.oid == käyttöoikeus.organisaatio.oid, oppilaitostyyppi = org.oppilaitostyyppi)
    }

    val hierarkianUlkopuolisetOikeudet = organisaatioHierarkia.toList.flatMap(hierarkianUlkopuolisetKäyttöoikeudet(käyttöoikeus, _)).filterNot { käyttöoikeus =>
      käyttöoikeudet.exists(_.organisaatio.oid == käyttöoikeus.ulkopuolinenOrganisaatio.oid)
    }

    käyttöoikeudet ++ hierarkianUlkopuolisetOikeudet
  }

  private def hierarkianUlkopuolisetKäyttöoikeudet(k: KäyttöoikeusOrg, organisaatioHierarkia: OrganisaatioHierarkia) =
    if (organisaatioHierarkia.varhaiskasvatuksenJärjestäjä && organisaatioHierarkia.toKoulutustoimija.isDefined) {
      organisaatioRepository.findAllVarhaiskasvatusToimipisteet.map {
        case (toimipiste, onVarhaiskasvatuksenToimipiste) =>
        KäyttöoikeusVarhaiskasvatusToimipiste(
          koulutustoimija = organisaatioHierarkia.toKoulutustoimija.get,
          ulkopuolinenOrganisaatio = toimipiste.toOidOrganisaatio,
          organisaatiokohtaisetPalveluroolit = k.organisaatiokohtaisetPalveluroolit,
          onVarhaiskasvatuksenToimipiste = onVarhaiskasvatuksenToimipiste
        )
      }
    } else {
      Nil
    }

  private lazy val käyttöoikeusCache = new KeyValueCache[AuthenticationUser, Set[Käyttöoikeus]](
    ExpiringCache("KäyttöoikeusRepository", 5.minutes, 100), haeKäyttöoikeudet
  )
}
