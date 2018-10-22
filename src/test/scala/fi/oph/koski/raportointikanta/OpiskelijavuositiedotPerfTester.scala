package fi.oph.koski.raportointikanta

import java.time.LocalDate

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi

object OpiskelijavuositiedotPerfTester extends App {

  lazy val application = KoskiApplication.apply

  def doIt: Unit = {
    val statuses = application.raportointiDatabase.statuses
    println(s"statuses=$statuses")

    val oppilaitos = "1.2.246.562.10.2013110715495487451932"

    val start = System.currentTimeMillis()
    val rows = application.raportointiDatabase.opiskeluoikeusAikajaksot(oppilaitos, OpiskeluoikeudenTyyppi.ammatillinenkoulutus.koodiarvo, LocalDate.of(2016, 1, 1), LocalDate.of(2019, 1, 1))
    val elapsed = System.currentTimeMillis() - start
    println(s"got ${rows.size} rows, elapsed = $elapsed")
  }

  println("starting..")
  doIt
  println(s"done")
}
