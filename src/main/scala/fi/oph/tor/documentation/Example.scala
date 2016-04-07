package fi.oph.tor.documentation

import fi.oph.tor.schema.{Koodistokoodiviite, Henkilö, TorOppija}

case class Example(name: String, description: String, data: TorOppija)

object Examples {
  val examples = ExamplesAmmatillinen.examples ++ ExamplesPeruskoulutus.examples
}

object ExampleData {
  val exampleHenkilö = Henkilö("010101-123N", "matti pekka", "matti", "virtanen")
  lazy val opiskeluoikeusAktiivinen = Koodistokoodiviite("aktiivinen", Some("Aktiivinen"), "opiskeluoikeudentila", Some(1))
  lazy val opiskeluoikeusPäättynyt = Koodistokoodiviite("paattynyt", Some("Päättynyt"), "opiskeluoikeudentila", Some(1))
  lazy val opiskeluoikeusKeskeyttänyt = Koodistokoodiviite("keskeyttanyt", Some("Keskeyttänyt"), "opiskeluoikeudentila", Some(1))
}
