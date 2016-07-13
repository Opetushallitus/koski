package fi.oph.koski.documentation

import fi.oph.koski.koodisto.MockKoodistoViitePalvelu
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.MockOrganisaatioRepository
import fi.oph.koski.schema.Oppija
import fi.oph.koski.ytr.{YtrMock, YtrOppijaConverter}

object ExamplesYlioppilastutkinto {
  private lazy val koodistoViitePalvelu = MockKoodistoViitePalvelu
  private lazy val oppilaitokset = OppilaitosRepository(MockOrganisaatioRepository)
  val opiskeluOikeus = YtrMock.oppijaByHetu(MockOppijat.ylioppilas.hetu).flatMap(YtrOppijaConverter(oppilaitokset, koodistoViitePalvelu, MockOrganisaatioRepository).convert(_)).get
  val oppija = Oppija(MockOppijat.ylioppilas.vainHenkilötiedot, List(opiskeluOikeus))

  val examples = List(Example("ylioppilastutkinto", "Oppija on suorittanut ylioppilastutkinnon", oppija, 501))
}
