package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.cache.{CachingProxy, KoskiCache}
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.koskiuser.Käyttöoikeusryhmät.OrganisaatioKäyttöoikeusryhmä
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.util.Timing
import rx.lang.scala.Observable

object UserOrganisationsRepository {
  def apply(config: Config, organisaatioRepository: OrganisaatioRepository) = {
    CachingProxy(KoskiCache.cacheStrategy("UserOrganisationsRepository"), if (config.hasPath("opintopolku.virkailija.username")) {
      new RemoteUserOrganisationsRepository(AuthenticationServiceClient(config), organisaatioRepository)
    } else {
      MockUsers
    })
  }
}

trait UserOrganisationsRepository {
  def getUserOrganisations(oid: String): Observable[Set[OrganisaatioKäyttöoikeusryhmä]]
}

import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.organisaatio.OrganisaatioRepository
import org.http4s.EntityDecoderInstances

class RemoteUserOrganisationsRepository(henkilöPalveluClient: AuthenticationServiceClient, organisaatioRepository: OrganisaatioRepository)
                                       extends UserOrganisationsRepository with EntityDecoderInstances with Timing {
  private lazy val käyttöoikeusryhmät = henkilöPalveluClient.käyttöoikeusryhmät

  def getUserOrganisations(oid: String) = {
    timedObservable("käyttäjänOrganisaatiot")(henkilöPalveluClient.käyttäjänKäyttöoikeusryhmät(oid)
      .map { (käyttöoikeudet: List[(String, Int)]) =>
        käyttöoikeudet.toSet.flatMap { tuple: (String, Int) =>
          tuple match {
            case (organisaatioOid: String, ryhmäId: Int) =>
              val oids: Set[String] = organisaatioRepository.getChildOids(organisaatioOid).toSet.flatten ++ Set(organisaatioOid)
              oids.flatMap(oid => käyttöoikeusryhmät.find(_.id == ryhmäId).flatMap(ryhmä => ryhmä.toKoskiKäyttöoikeusryhmä.map { r => (oid, r)}))
          }
        }
      })
  }
}

