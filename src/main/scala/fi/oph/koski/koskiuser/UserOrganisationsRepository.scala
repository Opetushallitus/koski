package fi.oph.koski.koskiuser

import com.typesafe.config.Config
import fi.oph.koski.cache.{CachingProxy, KoskiCache}
import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.util.Timing
import rx.lang.scala.Observable

object UserOrganisationsRepository {
  def apply(config: Config, organisaatioRepository: OrganisaatioRepository) = {
    CachingProxy(KoskiCache.cacheStrategy, if (config.hasPath("opintopolku.virkailija.username")) {
      new RemoteUserOrganisationsRepository(AuthenticationServiceClient(config), organisaatioRepository, KäyttöoikeusRyhmät(config))
    } else {
      MockUsers
    })
  }
}

trait UserOrganisationsRepository {
  def getUserOrganisations(oid: String): Observable[Set[String]]
}

import fi.oph.koski.henkilo.AuthenticationServiceClient
import fi.oph.koski.organisaatio.OrganisaatioRepository
import org.http4s.EntityDecoderInstances

class RemoteUserOrganisationsRepository(henkilöPalveluClient: AuthenticationServiceClient, organisaatioRepository: OrganisaatioRepository, käyttöoikeusRyhmät: KäyttöoikeusRyhmät)
                                       extends UserOrganisationsRepository with EntityDecoderInstances with Timing {
  def getUserOrganisations(oid: String): Observable[Set[String]] = {
    timedObservable("käyttäjänOrganisaatiot")(henkilöPalveluClient.käyttäjänOrganisaatiot(oid, käyttöoikeusRyhmät.readWrite)
      .map { oids =>
        oids.toSet
          .flatMap { organisaatioOid: String =>
          organisaatioRepository.getChildOids(organisaatioOid).toSet.flatten ++ Set(organisaatioOid)
        }
      })
  }
}

