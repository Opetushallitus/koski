package fi.oph.koski.koskiuser

import fi.oph.koski.cache.{CachingStrategy, KeyValueCache}
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.util.Timing
import rx.lang.scala.Observable

class UserOrganisationsRepository(henkilöPalveluClient: AuthenticationServiceClient, organisaatioRepository: OrganisaatioRepository) extends Timing {

  private lazy val käyttöoikeusryhmätCache = henkilöPalveluClient.käyttöoikeusryhmät
  private lazy val userOrganisationsCache = new KeyValueCache[String, Observable[Set[(Oid, Käyttöoikeusryhmä)]]](
    CachingStrategy.cacheAllNoRefresh("userOrganisations", 3600, 100),
    oid => timedObservable("käyttäjänOrganisaatiot")(henkilöPalveluClient.käyttäjänKäyttöoikeusryhmät(oid)
      .map { (käyttöoikeudet: List[(String, Int)]) =>
        käyttöoikeudet.toSet.flatMap { tuple: (String, Int) =>
          tuple match {
            case (organisaatioOid: String, ryhmäId: Int) =>
              val oids: Set[String] = organisaatioRepository.getChildOids(organisaatioOid).toSet.flatten ++ Set(organisaatioOid)
              oids.flatMap(oid => käyttöoikeusryhmätCache.find(_.id == ryhmäId).flatMap(ryhmä => ryhmä.toKoskiKäyttöoikeusryhmä.map { r => (oid, r)}))
          }
        }
      })
  )

  def getUserOrganisations(oid: String): Observable[Set[(Oid, Käyttöoikeusryhmä)]] = userOrganisationsCache(oid)

  private lazy val oppilaitostyypitCache = new KeyValueCache[String, Set[String]](CachingStrategy.cacheAllNoRefresh("OppilaitostyypitRepository", 3600, 1000), oid =>
    userOrganisationsCache(oid).toBlocking.first
      .filter(_._2.orgAccessType.contains(AccessType.read))
      .map(_._1).flatMap(organisaatioRepository.getOrganisaatioHierarkia(_))
      .flatMap(_.oppilaitostyyppi)
  )

  def getUserOppilaitostyypit(oid: String): Set[String] = oppilaitostyypitCache(oid)
}