package fi.oph.koski.virta

import fi.oph.koski.cache.{CacheManager, GlobalCacheManager}
import fi.oph.koski.henkilo.Hetu
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.{AccessChecker, SkipAccessCheck}
import fi.oph.koski.log.Logging
import fi.oph.koski.oppilaitos.{MockOppilaitosRepository, OppilaitosRepository}
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator

case class VirtaOpiskeluoikeusRepository(
  virta: VirtaClient,
  oppilaitosRepository: OppilaitosRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu,
  accessChecker: AccessChecker,
  hetuValidator: Hetu,
  validator: Option[KoskiValidator] = None
)(implicit cacheInvalidator: CacheManager) extends HetuBasedOpiskeluoikeusRepository[KorkeakoulunOpiskeluoikeus](oppilaitosRepository, koodistoViitePalvelu, accessChecker, validator) with Logging {

  private val converter = VirtaXMLConverter(oppilaitosRepository, koodistoViitePalvelu)

  override protected def opiskeluoikeudetByHetu(hetu: String): List[KorkeakoulunOpiskeluoikeus] = hetuValidator.validate(hetu) match {
    case Right(h) => virta.opintotiedot(VirtaHakuehtoHetu(h)).toList.flatMap(converter.convertToOpiskeluoikeudet)
    case Left(status) =>
      logger.warn(s"Virta haku prevented $status")
      Nil
  }
}

object MockVirtaOpiskeluoikeusRepository extends VirtaOpiskeluoikeusRepository(MockVirtaClient, MockOppilaitosRepository, MockKoodistoViitePalvelu, SkipAccessCheck, new Hetu(true))(GlobalCacheManager)
