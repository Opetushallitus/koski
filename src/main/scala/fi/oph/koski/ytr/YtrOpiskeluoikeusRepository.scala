package fi.oph.koski.ytr

import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.koski.KoskiValidator
import fi.oph.koski.oppija.OppijaRepository
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.schema._
import fi.oph.koski.virta.HetuBasedOpiskeluoikeusRepository

case class YtrOpiskeluoikeusRepository(ytr: YlioppilasTutkintoRekisteri, oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, validator: Option[KoskiValidator] = None)
    extends HetuBasedOpiskeluoikeusRepository[YlioppilastutkinnonOpiskeluoikeus](oppijaRepository, oppilaitosRepository, koodistoViitePalvelu, validator)
{
  private val converter = YtrOppijaConverter(oppilaitosRepository, koodistoViitePalvelu)

  override def opiskeluoikeudetByHetu(hetu: String) = ytr.oppijaByHetu(hetu).flatMap(converter.convert(_)).toList
}

