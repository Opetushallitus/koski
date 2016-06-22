package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._

case class Example(name: String, description: String, data: Oppija, statusCode: Int = 200)

object Examples {
  val examples = ExamplesAmmatillinen.examples ++ ExamplesValma.examples ++ ExamplesTelma.examples ++
    ExamplesPerusopetukseenValmistavaOpetus.examples ++ ExamplesPerusopetus.examples ++ ExamplesPerusopetuksenLisaopetus.examples ++ ExamplesLukioonValmistavaKoulutus.examples ++
    ExamplesLukio.examples ++ ExamplesYlioppilastutkinto.examples ++
    ExamplesKorkeakoulu.examples
}

object ExampleData {
  lazy val suomenKieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None))
  lazy val tilaKesken = Koodistokoodiviite("KESKEN", "suorituksentila")
  lazy val tilaValmis: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "suorituksentila", koodiarvo = "VALMIS")
  val helsinki = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "091", nimi = Some("Helsinki"))
  val jyväskylä = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "179", nimi = Some("Jyväskylä"))
  val suomi = Koodistokoodiviite(koodistoUri = "maatjavaltiot2", koodiarvo = "246", nimi = Some("Suomi"))
  def vahvistus(päivä: LocalDate = date(2016, 6, 4), org: OrganisaatioWithOid = jyväskylänNormaalikoulu, kunta: Koodistokoodiviite = jyväskylä) = Some(Henkilövahvistus(päivä = päivä, kunta, myöntäjäOrganisaatio = org, myöntäjäHenkilöt = List(OrganisaatioHenkilö("Reijo Reksi", "rehtori", org))))
}
