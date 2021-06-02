package fi.oph.koski.oppivelvollisuustieto

import java.time.LocalDate
import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.OpiskeluoikeusTestMethods
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers}
import fi.oph.koski.schema.HenkilöWithOid
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class OppivelvollisuustietoServletSpec extends FreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethods with Matchers with BeforeAndAfterAll {

  override def beforeAll = resetFixtures

  "Oppivelvollisuustieto" - {
    "Rajapinnan kutsuminen vaatii käyttöoikeuden" in {
      def verifyForbidden(user: KoskiMockUser) = {
        postOids(koskeenTallennettujenOppijoidenOidit, user) {
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
        Oppivelvollisuustieto(KoskiSpecificMockOppijat.maksuttomuuttaPidennetty1.oid, LocalDate.of(2022, 1, 1), LocalDate.of(2025, 1, 12)),
        Oppivelvollisuustieto(KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2.oid, LocalDate.of(2022, 1, 1), LocalDate.of(2025, 1, 24))
      )
      val oids = List(
        KoskiSpecificMockOppijat.maksuttomuuttaPidennetty1.oid,
        KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2.oid,
        eiKuuluMaksuttomuudenPiiriinOid
      )

      postOids(oids, MockUsers.oppivelvollisuutietoRajapinta) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[Seq[Oppivelvollisuustieto]](body)
        response should contain theSameElementsAs(expectedResult)
      }
    }

    "Rajapinnasta kerralla haettavien tietojen määrä on rajoitettu" in {
      val oids = List.fill(10001)(koskeenTallennettujenOppijoidenOidit.head)

      postOids(oids, MockUsers.oppivelvollisuutietoRajapinta) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest("Rajapinnasta ei voi hakea yli 10000 oidia"))
      }
    }
  }

  private def postOids[A](oids: List[String], user: KoskiMockUser)(f: => A): A = {
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
