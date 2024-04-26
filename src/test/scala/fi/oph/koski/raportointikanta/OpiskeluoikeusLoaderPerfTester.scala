package fi.oph.koski.raportointikanta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSpecificSession

object OpiskeluoikeusLoaderPerfTester extends App {

  lazy val application = KoskiApplication.apply

  def doIt: Unit = {
    implicit val systemUser = KoskiSpecificSession.systemUser
    val loader = new FullReloadOpiskeluoikeusLoader(
      application.opiskeluoikeusQueryRepository,
      application.suostumuksenPeruutusService,
      application.organisaatioRepository,
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
