package fi.oph.koski.documentation

import fi.oph.koski.henkilo.MockAuthenticationServiceClient
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoPalvelu}
import fi.oph.koski.koskiuser.{KoskiUser, MockUsers, SkipAccessCheck, KäyttöoikeusRepository}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.oppija.{MockOppijat, OpintopolkuOppijaRepository}
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema._
import fi.oph.koski.virta.{MockVirtaClient, VirtaOpiskeluoikeusRepository}

object ExamplesKorkeakoulu {
  private def oppija = {
    // TODO: liikaa tunkkausta tässä
    val koodistoViitePalvelu = KoodistoViitePalvelu(MockKoodistoPalvelu)
    val authenticationServiceClient: MockAuthenticationServiceClient = new MockAuthenticationServiceClient()
    val oppijaRepository = new OpintopolkuOppijaRepository(authenticationServiceClient, KoodistoViitePalvelu(MockKoodistoPalvelu))
    val organisaatioRepository: MockOrganisaatioRepository = MockOrganisaatioRepository(koodistoViitePalvelu)
    val käyttöoikeudet = new KäyttöoikeusRepository(authenticationServiceClient, organisaatioRepository)
    val virtaOpiskeluoikeusRepository: VirtaOpiskeluoikeusRepository = VirtaOpiskeluoikeusRepository(MockVirtaClient, oppijaRepository, OppilaitosRepository(organisaatioRepository), koodistoViitePalvelu, SkipAccessCheck)
    val user: KoskiUser = MockUsers.paakayttaja.toKoskiUser(käyttöoikeudet)
    val opiskeluoikeudet = virtaOpiskeluoikeusRepository.findByOppijaOid(MockOppijat.dippainssi.oid)(user)
    Oppija(MockOppijat.dippainssi.vainHenkilötiedot, opiskeluoikeudet)
  }
  lazy val examples = List(
    Example("korkeakoulu - valmis diplomi-insinööri", "Diplomi-insinööriksi valmistunut opiskelija", oppija, 501)
  )
}

object KorkeakouluTestdata {
  lazy val oppija = MockOppijat.dippainssi.vainHenkilötiedot
  lazy val helsinginYliopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.helsinginYliopisto, Some(Koodistokoodiviite("01901", None, "oppilaitosnumero", None)), Some("Helsingin yliopisto"))
  lazy val opiskeluoikeusAktiivinen = Koodistokoodiviite("1", Some("aktiivinen"), "virtaopiskeluoikeudentila", Some(1))
}

