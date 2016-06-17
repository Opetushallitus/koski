package fi.oph.koski.documentation

import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoPalvelu}
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.MockOrganisaatioRepository
import fi.oph.koski.schema.Oppija
import fi.oph.koski.ytr.{YtrMock, YtrOppijaConverter}

object ExamplesYlioppilastutkinto {
  private lazy val koodistoViitePalvelu = KoodistoViitePalvelu(MockKoodistoPalvelu)
  private lazy val oppilaitokset = OppilaitosRepository(MockOrganisaatioRepository(koodistoViitePalvelu))
  val opiskeluOikeus = YtrMock.oppijaByHetu(MockOppijat.ylioppilas.hetu).flatMap(YtrOppijaConverter(oppilaitokset, koodistoViitePalvelu, MockOrganisaatioRepository(koodistoViitePalvelu)).convert(_)).get
  val oppija = Oppija(MockOppijat.ylioppilas.vainHenkilötiedot, List(opiskeluOikeus))

  val examples = List(Example("ylioppilastutkinto", "Oppija on suorittanut ylioppilastutkinnon", oppija, 501))
}
