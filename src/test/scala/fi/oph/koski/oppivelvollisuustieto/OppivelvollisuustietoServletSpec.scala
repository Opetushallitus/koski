package fi.oph.koski.oppivelvollisuustieto

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}
import fi.oph.koski.schema.HenkilöWithOid
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class OppivelvollisuustietoServletSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

  override def beforeAll = resetFixtures

  "Oppivelvollisuustieto" - {
    "Rajapinnan kutsuminen vaatii käyttöoikeuden" in {
      def verifyForbidden(user: KoskiMockUser) = {
        haeOppivelvollisuustiedot(koskeenTallennettujenOppijoidenOidit, user) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden())
        }
      }

      List(
        MockUsers.eiOikkia,
        MockUsers.omniaPääkäyttäjä,
        MockUsers.kelaLaajatOikeudet,
        MockUsers.paakayttaja
      ).foreach(verifyForbidden)
    }

    "Rajapintaa voi kutsua oikeilla käyttöoikeuksilla" in {
      val eiKuuluMaksuttomuudenPiiriinOid = KoskiSpecificMockOppijat.lukiolainen.oid

      val expectedResult = List(
        Oppivelvollisuustieto(
          oid = KoskiSpecificMockOppijat.maksuttomuuttaPidennetty1.oid,
          oppivelvollisuusVoimassaAsti = LocalDate.of(2021, 12, 31),
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = LocalDate.of(2024, 12, 31),
        ),
        Oppivelvollisuustieto(
          oid = KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2.oid,
          oppivelvollisuusVoimassaAsti = LocalDate.of(2021, 12, 31),
          oikeusMaksuttomaanKoulutukseenVoimassaAsti = LocalDate.of(2024, 12, 31),
        )
      )

      val oids = List(
        KoskiSpecificMockOppijat.maksuttomuuttaPidennetty1.oid,
        KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2.oid,
        eiKuuluMaksuttomuudenPiiriinOid
      )

      haeOppivelvollisuustiedot(oids, MockUsers.oppivelvollisuutietoRajapinta) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[Seq[Oppivelvollisuustieto]](body)
        response should contain theSameElementsAs(expectedResult)
      }
    }

    "Rajapinta ei palauta vain oppijanumerorekisteristä löytyviä oppijoita" in {
      val expectedResult = List()

      val oids = List(
        KoskiSpecificMockOppijat.eiKoskessaOppivelvollinen.oid,
      )

      haeOppivelvollisuustiedot(oids, MockUsers.oppivelvollisuutietoRajapinta) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[Seq[Oppivelvollisuustieto]](body)
        response should contain theSameElementsAs(expectedResult)
      }
    }

    "Rajapinnasta kerralla haettavien tietojen määrä on rajoitettu" in {
      val oids = List.fill(10001)(koskeenTallennettujenOppijoidenOidit.head)

      haeOppivelvollisuustiedot(oids, MockUsers.oppivelvollisuutietoRajapinta) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest("Rajapinnasta ei voi hakea yli 10000 oidia"))
      }
    }
  }

  private def haeOppivelvollisuustiedot[A](oids: List[String], user: KoskiMockUser)(f: => A): A = {
    post(
      "api/oppivelvollisuustieto/oids",
      JsonSerializer.writeWithRoot(oids),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  lazy val koskeenTallennettujenOppijoidenOidit = koskeenTallennetutOppijat.map(_.henkilö).collect {
    case h: HenkilöWithOid => h.oid
  }
}
