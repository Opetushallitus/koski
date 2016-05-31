package fi.oph.koski.documentation

import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema.{Koodistokoodiviite, Oppija}

case class Example(name: String, description: String, data: Oppija, statusCode: Int = 200)

object Examples {
  val examples = ExamplesAmmatillinen.examples ++
    ExamplesPerusopetus.examples ++ ExamplesPerusopetuksenLisaopetus.examples ++ ExamplesLukioonValmistavaKoulutus.examples ++
    ExamplesLukio.examples ++ ExamplesYlioppilastutkinto.examples ++
    ExamplesKorkeakoulu.examples
}

object ExampleData {
  lazy val opiskeluoikeusAktiivinen = Koodistokoodiviite("aktiivinen", Some("Aktiivinen"), "opiskeluoikeudentila", Some(1))
  lazy val opiskeluoikeusPäättynyt = Koodistokoodiviite("paattynyt", Some("Päättynyt"), "opiskeluoikeudentila", Some(1))
  lazy val opiskeluoikeusKeskeyttänyt = Koodistokoodiviite("keskeyttanyt", Some("Keskeyttänyt"), "opiskeluoikeudentila", Some(1))
  lazy val suomenKieli = Some(Koodistokoodiviite("FI", Some("suomi"), "kieli", None))
  lazy val tilaKesken = Koodistokoodiviite("KESKEN", "suorituksentila")
  lazy val tilaValmis: Koodistokoodiviite = Koodistokoodiviite(koodistoUri = "suorituksentila", koodiarvo = "VALMIS")
  val helsinki = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "091", nimi = Some("Helsinki"))
  val jyväskylä = Koodistokoodiviite(koodistoUri = "kunta", koodiarvo = "179", nimi = Some("Jyväskylä"))
}
