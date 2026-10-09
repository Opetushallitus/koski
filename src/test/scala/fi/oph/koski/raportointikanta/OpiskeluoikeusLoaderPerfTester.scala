package fi.oph.koski.raportointikanta

import fi.oph.koski.config.KoskiApplication

object OpiskeluoikeusLoaderPerfTester extends App {

  lazy val application = KoskiApplication.apply

  def doIt: Unit = {
    val loader = new FullReloadOpiskeluoikeusLoader(
      application.opiskeluoikeusQueryRepository,
      application.suostumuksenPeruutusService,
      application.organisaatioRepository,
      application.henkilöCache,
      application.raportointiDatabase,
    )
    val loadResults = loader.loadOpiskeluoikeudet()

    loadResults.toBlocking.foreach(lr => println(s"${lr}"))
  }

  println("reseting database...")
  application.raportointiDatabase.dropAndCreateObjects()

  println("loading...")
  val start = System.currentTimeMillis()
  doIt
  val elapsed = System.currentTimeMillis() - start
  println(s"Took $elapsed")
}
