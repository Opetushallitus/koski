package fi.oph.koski.documentation

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.localization.{KoskiLocalizationConfig, MockLocalizationRepository}
import fi.oph.koski.oppilaitos.MockOppilaitosRepository
import fi.oph.koski.organisaatio.MockOrganisaatioRepository
import fi.oph.koski.schema.Oppija
import fi.oph.koski.ytr.{MockYrtClient, YtrOppijaConverter, YtrSsnWithPreviousSsns}
import fi.oph.koski.cache.GlobalCacheManager._
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija

object ExamplesYlioppilastutkinto {
  private lazy val koodistoViitePalvelu = MockKoodistoViitePalvelu
  private lazy val oppilaitokset = new MockOppilaitosRepository
  val opiskeluoikeus = MockYrtClient.oppijaByHetu(YtrSsnWithPreviousSsns(KoskiSpecificMockOppijat.ylioppilas.hetu.get))
    .flatMap(YtrOppijaConverter(oppilaitokset, koodistoViitePalvelu, MockOrganisaatioRepository, MockLocalizationRepository(new KoskiLocalizationConfig)).convert(_)).get
  val oppija = Oppija(asUusiOppija(KoskiSpecificMockOppijat.ylioppilas), List(opiskeluoikeus))

  val examples = List(Example("ylioppilastutkinto", "Oppija on suorittanut ylioppilastutkinnon", oppija, 501))
}
