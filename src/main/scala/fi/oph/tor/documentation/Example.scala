package fi.oph.tor.documentation

import fi.oph.tor.schema.{KoodistoKoodiViite, Henkilö, TorOppija}

case class Example(name: String, description: String, data: TorOppija)

object Examples {
  val examples = ExamplesAmmatillinen.examples ++ ExamplesPeruskoulutus.examples
}

object ExampleData {
  val exampleHenkilö = Henkilö("010101-123N", "matti pekka", "matti", "virtanen")
  lazy val opiskeluoikeusAktiivinen = KoodistoKoodiViite("aktiivinen", Some("Aktiivinen"), "opiskeluoikeudentila", Some(1))
  lazy val opiskeluoikeusPäättynyt = KoodistoKoodiViite("paattynyt", Some("Päättynyt"), "opiskeluoikeudentila", Some(1))
  lazy val opiskeluoikeusKeskeyttänyt = KoodistoKoodiViite("keskeyttanyt", Some("Keskeyttänyt"), "opiskeluoikeudentila", Some(1))
}
