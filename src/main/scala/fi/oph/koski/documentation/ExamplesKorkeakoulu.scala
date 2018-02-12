package fi.oph.koski.documentation

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.koskiuser._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import fi.oph.koski.virta.MockVirtaOpiskeluoikeusRepository

object ExamplesKorkeakoulu {
  lazy val opiskeluoikeudet = MockVirtaOpiskeluoikeusRepository.findByOppijaOid(MockOppijat.dippainssi.oid)(KoskiSession.systemUser)
  lazy val oppija = Oppija(MockOppijat.dippainssi.henkilö, opiskeluoikeudet)
  lazy val examples = List(
    Example("korkeakoulu - valmis diplomi-insinööri", "Diplomi-insinööriksi valmistunut opiskelija", oppija, 501)
  )
}

object KorkeakouluTestdata {
  lazy val oppija = MockOppijat.dippainssi.henkilö
  lazy val helsinginYliopisto: Oppilaitos = Oppilaitos(MockOrganisaatiot.helsinginYliopisto, Some(Koodistokoodiviite("01901", None, "oppilaitosnumero", None)), Some("Helsingin yliopisto"))
  lazy val opiskeluoikeusAktiivinen = Koodistokoodiviite("1", Some("aktiivinen"), "virtaopiskeluoikeudentila", Some(1))
}
