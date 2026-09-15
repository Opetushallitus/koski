package fi.oph.koski.kela

import com.typesafe.config.ConfigValueFactory.fromIterable
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.TestEnvironment
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.KoskiSpecificSession
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._

/**
 * Estolistalla (kela.eiPalautettavatOpiskeluoikeustyypit) rajataan opiskeluoikeustyyppejä pois
 * Kelan luovutusrajapinnasta ilman koodimuutosta. Arvot ovat vapaita merkkijonoja: väärin
 * kirjoitettu tyyppi ei täsmää mihinkään eikä estä mitään, jolloin data päätyisi Kelalle
 * ilman virheilmoitusta.
 *
 * Testataan KelaServiceä suoraan eikä HTTP:n yli, koska LocalJettyHttpSpecin jaettu Jetty
 * käynnistetään lazynä: vain ensimmäisenä ajettu spec saa oman konfiguraationsa voimaan.
 */
class KelaEstolistaSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  private implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser

  private def kelaService(estetytTyypit: List[String]): KelaService =
    new KelaService(KoskiApplication(
      KoskiApplicationForTests.config.withValue(
        "kela.eiPalautettavatOpiskeluoikeustyypit",
        fromIterable(estetytTyypit.asJava)
      )
    ))

  private def opiskeluoikeustyypit(service: KelaService, hetu: String): List[String] =
    service.findKelaOppijaByHetu(hetu)
      .map(_.opiskeluoikeudet.map(_.tyyppi.koodiarvo).toList)
      .getOrElse(Nil)

  "Estolista" - {
    "rajaa määritellyn opiskeluoikeustyypin pois" in {
      opiskeluoikeustyypit(
        kelaService(List("korkeakoulutus")),
        KoskiSpecificMockOppijat.dippainssi.hetu.get
      ) should not contain "korkeakoulutus"
    }

    "ei rajaa mitään kun sitä ei ole asetettu" in {
      opiskeluoikeustyypit(
        kelaService(Nil),
        KoskiSpecificMockOppijat.dippainssi.hetu.get
      ) should contain("korkeakoulutus")
    }

    "ei vaikuta muihin tyyppeihin" in {
      opiskeluoikeustyypit(
        kelaService(List("korkeakoulutus")),
        KoskiSpecificMockOppijat.amis.hetu.get
      ) should not be empty
    }
  }
}
