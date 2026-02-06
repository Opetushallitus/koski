package fi.oph.koski.hsl

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.koskiuser.{KoskiSpecificSession, MockUsers}
import fi.oph.koski.luovutuspalvelu.HslResponse
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class HslOmaDataOAuth2ServiceSpec extends AnyFreeSpec with Matchers {

  private val application = KoskiApplicationForTests
  private val service = application.hslOmaDataOAuth2Service
  // Use a user with global read permissions for testing the service directly
  private implicit val session: KoskiSpecificSession = MockUsers.viranomainenGlobaaliKatselija.toKoskiSpecificSession(application.käyttöoikeusRepository)

  "HslOmaDataOAuth2Service" - {
    "findOpiskeluoikeudet" - {
      "palauttaa opiskeluoikeudet oppijalle jolla on ammatillisia opintoja" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen
        val result = service.findOpiskeluoikeudet(oppija.oid)

        result.isRight should be(true)
        result.toOption.get should not be empty
        result.toOption.get.foreach { oo =>
          HslResponse.schemassaTuetutOpiskeluoikeustyypit should contain(oo.tyyppi.koodiarvo)
        }
      }

      "suodattaa palauttamaan vain HSL-skeemassa tuetut opiskeluoikeustyypit" in {
        val oppija = KoskiSpecificMockOppijat.eero
        val result = service.findOpiskeluoikeudet(oppija.oid)

        result.isRight should be(true)

        result.toOption.get.foreach { oo =>
          HslResponse.schemassaTuetutOpiskeluoikeustyypit should contain(oo.tyyppi.koodiarvo)
        }
      }

      "palauttaa opiskeluoikeuden tila-tiedot" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen
        val result = service.findOpiskeluoikeudet(oppija.oid)

        result.isRight should be(true)
        result.toOption.get should not be empty

        val firstOo = result.toOption.get.head
        firstOo.tila.opiskeluoikeusjaksot should not be empty
      }

      "palauttaa opiskeluoikeuden oppilaitos-tiedot" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen
        val result = service.findOpiskeluoikeudet(oppija.oid)

        result.isRight should be(true)
        result.toOption.get should not be empty

        val firstOo = result.toOption.get.head
        firstOo.oppilaitos shouldBe defined
      }

      "palauttaa opiskeluoikeuden suoritukset" in {
        val oppija = KoskiSpecificMockOppijat.ammattilainen
        val result = service.findOpiskeluoikeudet(oppija.oid)

        result.isRight should be(true)
        result.toOption.get should not be empty

        val firstOo = result.toOption.get.head
        firstOo.suoritukset should not be empty
      }
    }
  }
}
