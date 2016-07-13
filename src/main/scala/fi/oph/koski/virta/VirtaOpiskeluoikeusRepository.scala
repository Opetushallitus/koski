package fi.oph.koski.virta

import fi.oph.koski.koodisto.{MockKoodistoViitePalvelu, KoodistoViitePalvelu}
import fi.oph.koski.koski.KoskiValidator
import fi.oph.koski.koskiuser.{SkipAccessCheck, AccessChecker}
import fi.oph.koski.log.Logging
import fi.oph.koski.oppija.{MockOpintopolkuOppijaRepository, OppijaRepository}
import fi.oph.koski.oppilaitos.{MockOppilaitosRepository, OppilaitosRepository}
import fi.oph.koski.schema._

case class VirtaOpiskeluoikeusRepository(virta: VirtaClient, oppijaRepository: OppijaRepository, oppilaitosRepository: OppilaitosRepository, koodistoViitePalvelu: KoodistoViitePalvelu, accessChecker: AccessChecker, validator: Option[KoskiValidator] = None)
  extends HetuBasedOpiskeluoikeusRepository[KorkeakoulunOpiskeluoikeus](oppijaRepository, oppilaitosRepository, koodistoViitePalvelu, accessChecker, validator) with Logging {

  private val converter = VirtaXMLConverter(oppijaRepository, oppilaitosRepository, koodistoViitePalvelu)

  override def opiskeluoikeudetByHetu(hetu: String) = virta.opintotiedot(VirtaHakuehtoHetu(hetu)).toList
    .flatMap(xmlData => converter.convertToOpiskeluoikeudet(xmlData))
}

object MockVirtaOpiskeluoikeusRepository extends VirtaOpiskeluoikeusRepository(MockVirtaClient, MockOpintopolkuOppijaRepository, MockOppilaitosRepository, MockKoodistoViitePalvelu, SkipAccessCheck)