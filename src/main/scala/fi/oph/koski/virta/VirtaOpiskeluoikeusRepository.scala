package fi.oph.koski.virta

import fi.oph.koski.cache.{CacheManager, GlobalCacheManager}
import fi.oph.koski.config.Environment.isLocalDevelopmentEnvironment
import fi.oph.koski.henkilo.{FindByOid, Hetu, MockOpintopolkuHenkilöRepository}
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.{AccessChecker, SkipAccessCheck}
import fi.oph.koski.log.Logging
import fi.oph.koski.oppilaitos.{MockOppilaitosRepository, OppilaitosRepository}
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator

case class VirtaOpiskeluoikeusRepository(virta: VirtaClient, henkilöRepository: FindByOid, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, accessChecker: AccessChecker, validator: Option[KoskiValidator] = None)(implicit cacheInvalidator: CacheManager)
  extends HetuBasedOpiskeluoikeusRepository[KorkeakoulunOpiskeluoikeus](henkilöRepository, oppilaitosRepository, koodistoViitePalvelu, accessChecker, validator) with Logging {

  private val converter = VirtaXMLConverter(oppilaitosRepository, koodistoViitePalvelu)

  override def opiskeluoikeudetByHetu(hetu: String): List[KorkeakoulunOpiskeluoikeus] = Hetu.validate(hetu, isLocalDevelopmentEnvironment) match {
    case Right(h) => virta.opintotiedot(VirtaHakuehtoHetu(h)).toList.flatMap(converter.convertToOpiskeluoikeudet)
    case Left(status) =>
      logger.warn(s"Virta haku prevented $status")
      Nil
  }
}

object MockVirtaOpiskeluoikeusRepository extends VirtaOpiskeluoikeusRepository(MockVirtaClient, MockOpintopolkuHenkilöRepository, MockOppilaitosRepository, MockKoodistoViitePalvelu, SkipAccessCheck)(GlobalCacheManager)