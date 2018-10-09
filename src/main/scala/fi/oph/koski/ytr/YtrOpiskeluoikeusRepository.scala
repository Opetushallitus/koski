package fi.oph.koski.ytr

import fi.oph.koski.cache.CacheManager
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koskiuser.AccessChecker
import fi.oph.koski.localization.LocalizationRepository
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.OrganisaatioRepository
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.virta.HetuBasedOpiskeluoikeusRepository

case class YtrOpiskeluoikeusRepository(ytr: YtrClient, organisaatioRepository: OrganisaatioRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, accessChecker: AccessChecker, validator: Option[KoskiValidator] = None, localizations: LocalizationRepository)(implicit cacheInvalidator: CacheManager)
    extends HetuBasedOpiskeluoikeusRepository[YlioppilastutkinnonOpiskeluoikeus](oppilaitosRepository, koodistoViitePalvelu, accessChecker, validator)
{
  private val converter = YtrOppijaConverter(oppilaitosRepository, koodistoViitePalvelu, organisaatioRepository, localizations)

  override protected def opiskeluoikeudetByHetu(hetu: String) = ytr.oppijaByHetu(hetu).flatMap(converter.convert(_)).toList
}

