package fi.oph.koski.documentation

import fi.oph.koski.henkilo.MockAuthenticationServiceClient
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoPalvelu}
import fi.oph.koski.koskiuser.{MockUsers, SkipAccesCheck}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.oppija.{MockOppijat, OpintopolkuOppijaRepository}
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema._
import fi.oph.koski.virta.{MockVirtaClient, VirtaOpiskeluoikeusRepository}

object ExamplesKorkeakoulu {
  val mockOppijaRepository = new OpintopolkuOppijaRepository(new MockAuthenticationServiceClient(), KoodistoViitePalvelu(MockKoodistoPalvelu))

  private lazy val koodistoViitePalvelu = KoodistoViitePalvelu(MockKoodistoPalvelu)
  private def oppija = Oppija(MockOppijat.dippainssi.vainHenkilötiedot, VirtaOpiskeluoikeusRepository(MockVirtaClient, mockOppijaRepository, OppilaitosRepository(MockOrganisaatioRepository(koodistoViitePalvelu)), koodistoViitePalvelu, SkipAccesCheck)
    .findByOppijaOid(MockOppijat.dippainssi.oid)(MockUsers.kalle.asKoskiUser)
  )
  lazy val examples = List(
    Example("korkeakoulu - valmis diplomi-insinööri", "Diplomi-insinööriksi valmistunut opiskelija", oppija, 501)
  )
}

object KorkeakouluTestdata {
  lazy val oppija = MockOppijat.dippainssi.vainHenkilötiedot
  lazy val helsinginYliopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.helsinginYliopisto, Some(Koodistokoodiviite("01901", None, "oppilaitosnumero", None)), Some("Helsingin yliopisto"))
  lazy val opiskeluoikeusAktiivinen = Koodistokoodiviite("1", Some("aktiivinen"), "virtaopiskeluoikeudentila", Some(1))
}

