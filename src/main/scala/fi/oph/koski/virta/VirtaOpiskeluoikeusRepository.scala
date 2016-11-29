package fi.oph.koski.virta

import fi.oph.koski.cache.{CacheManager, GlobalCacheManager}
import fi.oph.koski.henkilo.MockOpintopolkuHenkilöRepository
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.{AccessChecker, SkipAccessCheck}
import fi.oph.koski.log.Logging
import fi.oph.koski.henkilo.HenkilöRepository
import fi.oph.koski.oppilaitos.{MockOppilaitosRepository, OppilaitosRepository}
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator

case class VirtaOpiskeluoikeusRepository(virta: VirtaClient, oppijaRepository: HenkilöRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, accessChecker: AccessChecker, validator: Option[KoskiValidator] = None)(implicit cacheInvalidator: CacheManager)
  extends HetuBasedOpiskeluoikeusRepository[KorkeakoulunOpiskeluoikeus](oppijaRepository, oppilaitosRepository, koodistoViitePalvelu, accessChecker, validator) with Logging {

  private val converter = VirtaXMLConverter(oppijaRepository, oppilaitosRepository, koodistoViitePalvelu)

  override def opiskeluoikeudetByHetu(hetu: String) = virta.opintotiedot(VirtaHakuehtoHetu(hetu)).toList
    .flatMap(xmlData => converter.convertToOpiskeluoikeudet(xmlData))
}

object MockVirtaOpiskeluoikeusRepository extends VirtaOpiskeluoikeusRepository(MockVirtaClient, MockOpintopolkuHenkilöRepository, MockOppilaitosRepository, MockKoodistoViitePalvelu, SkipAccessCheck)(GlobalCacheManager)