package fi.oph.koski.documentation

import fi.oph.koski.cache.GlobalCacheManager
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.localization.MockLocalizationRepository
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.MockOrganisaatioRepository
import fi.oph.koski.schema.Oppija
import fi.oph.koski.ytr.{MockYrtClient, YtrOppijaConverter}
import GlobalCacheManager._

object ExamplesYlioppilastutkinto {
  private lazy val koodistoViitePalvelu = MockKoodistoViitePalvelu
  private lazy val oppilaitokset = OppilaitosRepository(MockOrganisaatioRepository)
  val opiskeluoikeus = MockYrtClient.oppijaByHetu(MockOppijat.ylioppilas.hetu.get).flatMap(YtrOppijaConverter(oppilaitokset, koodistoViitePalvelu, MockOrganisaatioRepository, MockLocalizationRepository()).convert(_)).get
  val oppija = Oppija(MockOppijat.ylioppilas.henkilö, List(opiskeluoikeus))

  val examples = List(Example("ylioppilastutkinto", "Oppija on suorittanut ylioppilastutkinnon", oppija, 501))
}
