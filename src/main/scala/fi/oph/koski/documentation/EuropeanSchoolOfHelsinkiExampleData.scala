package fi.oph.koski.documentation

import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

import java.time.LocalDate

object EuropeanSchoolOfHelsinkiExampleData {
  lazy val europeanSchoolOfHelsinki: Oppilaitos = Oppilaitos(MockOrganisaatiot.europeanSchoolOfHelsinki, Some(Koodistokoodiviite("03782", None, "oppilaitosnumero", None)), Some("European School of Helsinki"))
  lazy val europeanSchoolOfHelsinkiToimipiste: Toimipiste = Toimipiste("1.2.246.562.10.12798841685")
  def vahvistus(päivä: LocalDate) = ExampleData.vahvistusPaikkakunnalla(päivä, europeanSchoolOfHelsinki, ExampleData.helsinki)
}
