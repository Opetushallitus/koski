package fi.oph.koski.documentation

import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

object DIAExampleData {
  lazy val saksalainenKoulu: Oppilaitos = Oppilaitos(MockOrganisaatiot.saksalainenKoulu, Some(Koodistokoodiviite("00085", None, "oppilaitosnumero", None)), Some("Helsingin Saksalainen koulu"))
}
