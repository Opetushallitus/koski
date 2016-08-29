package fi.oph.koski.documentation

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._
import fi.oph.koski.tiedonsiirto.ExamplesTiedonsiirto

case class Example(name: String, description: String, data: Oppija, statusCode: Int = 200)

object Examples {
  val examples = ExamplesAmmatillinen.examples ++ ExamplesValma.examples ++ ExamplesTelma.examples ++
    ExamplesPerusopetukseenValmistavaOpetus.examples ++ ExamplesPerusopetus.examples ++ ExamplesPerusopetuksenLisaopetus.examples ++ ExamplesLukioonValmistavaKoulutus.examples ++
    ExamplesLukio.examples ++ ExamplesYlioppilastutkinto.examples ++
    ExamplesKorkeakoulu.examples

  val hiddenExamples = ExamplesTiedonsiirto.examples

  val allExamples = examples ++ hiddenExamples
}

object ExampleData {
  lazy val longTimeAgo = date(2000, 1, 1)
  lazy val suomenKieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None))
  val opiskeluoikeusLäsnä = Koodistokoodiviite("lasna", Some("Läsnä"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusValmistunut = Koodistokoodiviite("valmistunut", Some("Valmistunut"), "koskiopiskeluoikeudentila", Some(1))
  val opiskeluoikeusEronnut = Koodistokoodiviite("eronnut", Some("Eronnut"), "koskiopiskeluoikeudentila", Some(1))
  lazy val tilaKesken = Koodistokoodiviite("KESKEN", "suorituksentila")
  lazy val tilaValmis: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "suorituksentila", koodiarvo = "VALMIS")
  val helsinki = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "091", nimi = Some("Helsinki"))
  val jyväskylä = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "179", nimi = Some("Jyväskylä"))
  val suomi = Koodistokoodiviite(koodistoUri = "maatjavaltiot2", koodiarvo = "246", nimi = Some("Suomi"))
  def vahvistus(päivä: LocalDate = date(2016, 6, 4), org: OrganisaatioWithOid = jyväskylänNormaalikoulu, kunta: Koodistokoodiviite = jyväskylä) = Some(Henkilövahvistus(päivä = päivä, kunta, myöntäjäOrganisaatio = org, myöntäjäHenkilöt = List(Organisaatiohenkilö("Reijo Reksi", "rehtori", org))))
}
