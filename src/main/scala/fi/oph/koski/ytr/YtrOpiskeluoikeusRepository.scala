package fi.oph.koski.ytr

import fi.oph.koski.cache.CacheManager
import fi.oph.koski.henkilo.HenkilönTunnisteet
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.{AccessChecker, AccessType, KoskiSpecificSession}
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.log.NotLoggable
import fi.oph.koski.opiskeluoikeus.AuxiliaryOpiskeluoikeusRepositoryImpl
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema.{Oppija, UusiHenkilö, YlioppilastutkinnonOpiskeluoikeus}
import fi.oph.koski.validation.KoskiValidator

case class YtrOpiskeluoikeusRepository(
  ytr: YtrRepository,
  organisaatioRepository: OrganisaatioRepository,
  oppilaitosRepository: OppilaitosRepository,
  koodistoViitePalvelu: KoodistoViitePalvelu,
  accessChecker: AccessChecker,
  validator: Option[KoskiValidator] = None,
  localizations: LocalizationRepository
)(implicit cacheInvalidator: CacheManager) extends AuxiliaryOpiskeluoikeusRepositoryImpl[YlioppilastutkinnonOpiskeluoikeus, YtrCacheKey](accessChecker) {
  private val converter = YtrOppijaConverter(oppilaitosRepository, koodistoViitePalvelu, organisaatioRepository, localizations)

  override protected def uncachedOpiskeluoikeudet(cacheKey: YtrCacheKey): List[YlioppilastutkinnonOpiskeluoikeus] = {
    val opiskeluoikeudet = ytr.findByCacheKey(cacheKey).flatMap(converter.convert).toList
    opiskeluoikeudet.foreach(validate)
    opiskeluoikeudet
  }

  override protected def buildCacheKey(tunnisteet: HenkilönTunnisteet): YtrCacheKey =
    YtrCacheKey(tunnisteet.hetu.toList ++ tunnisteet.vanhatHetut)

  private def validate(opiskeluoikeus: YlioppilastutkinnonOpiskeluoikeus): Unit = {
    val oppija = Oppija(UusiHenkilö("010101-123N", "tuntematon", Some("tuntematon"), "tuntematon"), List(opiskeluoikeus))
    validator.foreach(_.updateFieldsAndValidateAsJson(oppija)(KoskiSpecificSession.systemUser, AccessType.read).left.foreach { status: HttpStatus =>
      logger.warn("Ulkoisesta järjestelmästä saatu opiskeluoikeus sisältää validointivirheitä " + status)
    })
  }
}

case class YtrCacheKey(hetut: List[String]) extends NotLoggable
