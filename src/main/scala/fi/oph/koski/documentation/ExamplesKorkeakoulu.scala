package fi.oph.koski.documentation

import java.time.LocalDate.{of => date}
import fi.oph.koski.koodisto.{KoodistoViitePalvelu, MockKoodistoPalvelu}
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.oppija.{MockOppijaRepository, MockOppijat}
import fi.oph.koski.oppilaitos.OppilaitosRepository
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema._
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.virta.{VirtaOpiskeluoikeusRepository, MockVirtaClient}

object ExamplesKorkeakoulu {
  private lazy val koodistoViitePalvelu = KoodistoViitePalvelu(MockKoodistoPalvelu)
  private def oppija = Oppija(MockOppijat.dippainssi.vainHenkilötiedot, VirtaOpiskeluoikeusRepository(MockVirtaClient, MockOppijaRepository(), OppilaitosRepository(MockOrganisaatioRepository(koodistoViitePalvelu)), koodistoViitePalvelu)
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

