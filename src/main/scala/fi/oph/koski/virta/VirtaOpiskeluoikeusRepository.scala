package fi.oph.koski.virta

import fi.oph.koski.cache.{CacheManager, GlobalCacheManager}
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.HenkilönTunnisteet
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoViitePalvelu}
import fi.oph.koski.koskiuser.{AccessChecker, AccessType, KoskiSpecificSession, SkipAccessCheck}
import fi.oph.koski.log.NotLoggable
import fi.oph.koski.opiskeluoikeus.AuxiliaryOpiskeluoikeusRepositoryImpl
import fi.oph.koski.oppilaitos.{MockOppilaitosRepository, OppilaitosRepository}
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, OrganisaatioRepository}
import fi.oph.koski.schema.{KorkeakoulunOpiskeluoikeus, Oppija, UusiHenkilö}
import fi.oph.koski.validation.KoskiValidator

case class VirtaOpiskeluoikeusRepository(
  virta: VirtaClient,
  oppilaitosRepository: OppilaitosRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu,
  organisaatioRepository: OrganisaatioRepository,
  accessChecker: AccessChecker,
  validator: Option[KoskiValidator] = None
)(implicit cacheInvalidator: CacheManager) extends AuxiliaryOpiskeluoikeusRepositoryImpl[KorkeakoulunOpiskeluoikeus, VirtaCacheKey](accessChecker) {
  private val converter = VirtaXMLConverter(oppilaitosRepository, koodistoViitePalvelu, organisaatioRepository)

  override protected def uncachedOpiskeluoikeudet(cacheKey: VirtaCacheKey): List[KorkeakoulunOpiskeluoikeus] = {
    val opiskeluoikeudet = virtaHaku(cacheKey)
    opiskeluoikeudet.foreach(validate)
    opiskeluoikeudet
  }

  override protected def buildCacheKey(tunnisteet: HenkilönTunnisteet): VirtaCacheKey =
    VirtaCacheKey((tunnisteet.hetu.toList ++ tunnisteet.vanhatHetut).sorted, (tunnisteet.oid +: tunnisteet.linkitetytOidit).sorted)

  private def validate(opiskeluoikeus: KorkeakoulunOpiskeluoikeus): Unit = {
    val oppija = Oppija(UusiHenkilö("010101-123N", "tuntematon", Some("tuntematon"), "tuntematon"), List(opiskeluoikeus))
    validator.foreach(_.updateFieldsAndValidateAsJson(oppija)(KoskiSpecificSession.systemUser, AccessType.read).left.foreach { status: HttpStatus =>
      logger.warn("Ulkoisesta järjestelmästä saatu opiskeluoikeus sisältää validointivirheitä " + status)
    })
  }

  private def virtaHaku(cacheKey: VirtaCacheKey): List[KorkeakoulunOpiskeluoikeus] = {
    def massaHaku(hakuehdot: List[VirtaHakuehto]) = if (hakuehdot.isEmpty) {
      Nil
    } else {
      virta.opintotiedotMassahaku(hakuehdot)
        .toList
        .flatMap(converter.convertToOpiskeluoikeudet)
    }

    (massaHaku(cacheKey.hetut.map(VirtaHakuehtoHetu)) ++ massaHaku(cacheKey.oidit.map(VirtaHakuehtoKansallinenOppijanumero))).distinct
  }
}

private[virta] case class VirtaCacheKey(hetut: List[String], oidit: List[String]) extends NotLoggable

object MockVirtaOpiskeluoikeusRepository extends VirtaOpiskeluoikeusRepository(
  MockVirtaClient(KoskiApplication.defaultConfig),
  new MockOppilaitosRepository,
  MockKoodistoViitePalvelu,
  MockOrganisaatioRepository,
  SkipAccessCheck
)(GlobalCacheManager)
