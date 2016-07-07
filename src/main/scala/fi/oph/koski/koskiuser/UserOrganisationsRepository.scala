package fi.oph.koski.koskiuser

import fi.oph.koski.cache.{CachingStrategy, KeyValueCache}
import fi.oph.koski.henkilo.{AuthenticationServiceClient, Käyttöoikeusryhmä => KOR}
import fi.oph.koski.organisaatio.{OrganisaatioHierarkia, OrganisaatioRepository}
import fi.oph.koski.util.Timing
import rx.lang.scala.Observable

class UserOrganisationsRepository(henkilöPalveluClient: AuthenticationServiceClient, organisaatioRepository: OrganisaatioRepository) extends Timing {

  private lazy val käyttöoikeusryhmätCache = henkilöPalveluClient.käyttöoikeusryhmät
  private lazy val userOrganisationsCache = new KeyValueCache[String, Observable[Set[OrganisaatioKäyttöoikeus]]](
    CachingStrategy.cacheAllNoRefresh("userOrganisations", 3600, 100),
    oid => timedObservable("käyttäjänOrganisaatiot")(henkilöPalveluClient.käyttäjänKäyttöoikeusryhmät(oid)
      .map { (käyttöoikeudet: List[(String, Int)]) =>
        käyttöoikeudet.toSet.flatMap { tuple: (String, Int) =>
          tuple match {
            case (organisaatioOid: String, ryhmäId: Int) =>
              def flatten(orgs: List[OrganisaatioHierarkia]): List[OrganisaatioHierarkia] = {
                orgs ++ orgs.flatMap { org => org :: flatten(org.children) }
              }
              val flattened: List[OrganisaatioHierarkia] = flatten(organisaatioRepository.getOrganisaatioHierarkia(oid).toList)

              flattened.flatMap { org =>
                val found: Option[KOR] = käyttöoikeusryhmätCache.find(_.id == ryhmäId)
                val käyttöoikeus: Option[OrganisaatioKäyttöoikeus] = found.flatMap(_.toKoskiKäyttöoikeusryhmä).map { ryhmä =>
                  OrganisaatioKäyttöoikeus(org.oid, org.oppilaitostyyppi, ryhmä)
                }
                käyttöoikeus
              }
          }
        }
      })
  )

  def käyttäjänKäyttöoikeudet(oid: String): Observable[Set[OrganisaatioKäyttöoikeus]] = userOrganisationsCache(oid)

  def käyttäjänOppilaitostyypit(oid: String): Set[String] = userOrganisationsCache(oid).toBlocking.first
    .filter(_.ryhmä.orgAccessType.contains(AccessType.read))
    .flatMap(_.oppilaitostyyppi)

}

case class OrganisaatioKäyttöoikeus(oid: String, oppilaitostyyppi: Option[String], ryhmä: Käyttöoikeusryhmä)