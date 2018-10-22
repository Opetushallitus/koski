package fi.oph.koski.virta

import fi.oph.koski.cache.{CacheManager, GlobalCacheManager}
import fi.oph.koski.henkilo.HenkilönTunnisteet
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.{AccessChecker, AccessType, KoskiSession, SkipAccessCheck}
import fi.oph.koski.log.NotLoggable
import fi.oph.koski.opiskeluoikeus.AuxiliaryOpiskeluoikeusRepositoryImpl
import fi.oph.koski.oppilaitos.{MockOppilaitosRepository, OppilaitosRepository}
import fi.oph.koski.schema.{KorkeakoulunOpiskeluoikeus, Oppija, UusiHenkilö}
import fi.oph.koski.validation.KoskiValidator

case class VirtaOpiskeluoikeusRepository(
  virta: VirtaClient,
  oppilaitosRepository: OppilaitosRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu,
  accessChecker: AccessChecker,
  validator: Option[KoskiValidator] = None
)(implicit cacheInvalidator: CacheManager) extends AuxiliaryOpiskeluoikeusRepositoryImpl[KorkeakoulunOpiskeluoikeus, VirtaCacheKey](accessChecker) {
  private val converter = VirtaXMLConverter(oppilaitosRepository, koodistoViitePalvelu)

  override protected def uncachedOpiskeluoikeudet(cacheKey: VirtaCacheKey): List[KorkeakoulunOpiskeluoikeus] = {
    val opiskeluoikeudet = cacheKey.hetut.headOption match {
      case Some(hetu) => virta.opintotiedot(VirtaHakuehtoHetu(hetu)).toList.flatMap(converter.convertToOpiskeluoikeudet)
      case None => Nil
    }
    opiskeluoikeudet.foreach(validate)
    opiskeluoikeudet
  }

  override protected def buildCacheKey(tunnisteet: HenkilönTunnisteet): VirtaCacheKey =
    VirtaCacheKey((tunnisteet.hetu.toList ++ tunnisteet.vanhatHetut).sorted, (tunnisteet.oid +: tunnisteet.linkitetytOidit).sorted)

  private def validate(opiskeluoikeus: KorkeakoulunOpiskeluoikeus): Unit = {
    val oppija = Oppija(UusiHenkilö("010101-123N", "tuntematon", Some("tuntematon"), "tuntematon"), List(opiskeluoikeus))
    validator.foreach(_.validateAsJson(oppija)(KoskiSession.systemUser, AccessType.read).left.foreach { status: HttpStatus =>
      logger.warn("Ulkoisesta järjestelmästä saatu opiskeluoikeus sisältää validointivirheitä " + status)
    })
  }
}

private[virta] case class VirtaCacheKey(hetut: List[String], oidit: List[String]) extends NotLoggable

object MockVirtaOpiskeluoikeusRepository extends VirtaOpiskeluoikeusRepository(MockVirtaClient, MockOppilaitosRepository, MockKoodistoViitePalvelu, SkipAccessCheck)(GlobalCacheManager)
