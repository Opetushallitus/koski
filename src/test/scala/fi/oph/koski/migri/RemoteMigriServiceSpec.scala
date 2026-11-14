package fi.oph.koski.migri

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.config.KoskiApplication
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._
import scala.util.Try

class RemoteMigriServiceSpec extends AnyFreeSpec with Matchers {
  // Jokainen blaze-client käynnistää oman tick wheel -säikeen.
  private def blazeClientThreads: Int =
    Thread.getAllStackTraces.keySet.asScala.count(_.getName == "blaze-tick-wheel-executor")

  "Kutsut käyttävät samaa http-clientia eivätkä vuoda säikeitä" in {
    implicit val application: KoskiApplication = new KoskiApplication(
      KoskiApplication.defaultConfig.withValue("opintopolku.virkailija.url", fromAnyRef("http://localhost:1"))
    )
    val service = new RemoteMigriService
    val threadsBefore = blazeClientThreads

    (1 to 10).foreach(_ => Try(service.getByOids(List("1.2.246.562.24.00000000001"), MigriCredentials("käyttäjä", "salasana"))))

    (blazeClientThreads - threadsBefore) should be < 2
  }
}
