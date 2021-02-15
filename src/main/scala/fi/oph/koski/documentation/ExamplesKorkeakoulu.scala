package fi.oph.koski.documentation

import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.koskiuser._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.virta.MockVirtaOpiskeluoikeusRepository

object ExamplesKorkeakoulu {
  lazy val opiskeluoikeudet = MockVirtaOpiskeluoikeusRepository.findByOppija(KoskiSpecificMockOppijat.dippainssi)(KoskiSpecificSession.systemUser)
  lazy val oppija = Oppija(asUusiOppija(KoskiSpecificMockOppijat.dippainssi), opiskeluoikeudet)
  lazy val examples = List(
    Example("korkeakoulu - valmis diplomi-insinööri", "Diplomi-insinööriksi valmistunut opiskelija", oppija, 501)
  )
}

object KorkeakouluTestdata {
  lazy val oppija = KoskiSpecificMockOppijat.dippainssi
  lazy val helsinginYliopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.helsinginYliopisto, Some(Koodistokoodiviite("01901", None, "oppilaitosnumero", None)), Some("Helsingin yliopisto"))
  lazy val opiskeluoikeusAktiivinen = Koodistokoodiviite("1", Some("aktiivinen"), "virtaopiskeluoikeudentila", Some(1))
}
