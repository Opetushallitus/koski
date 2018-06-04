package fi.oph.koski.raportointikanta

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.koskiuser.KoskiSession

object OpiskeluoikeusLoaderPerfTester extends App {

  lazy val application = KoskiApplication.apply

  def doIt: Unit = {
    implicit val systemUser = KoskiSession.systemUser
    val loadResults = OpiskeluoikeusLoader.loadOpiskeluoikeudet(application.opiskeluoikeusQueryRepository, systemUser, application.raportointiDatabase)
    loadResults.toBlocking.foreach(lr => println(s"${lr}"))
  }

  println("reseting database...")
  application.raportointiDatabase.dropAndCreateSchema

  println("loading...")
  val start = System.currentTimeMillis()
  doIt
  val elapsed = System.currentTimeMillis() - start
  println(s"Took $elapsed")
}
