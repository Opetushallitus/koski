package fi.oph.koski.api

import fi.oph.koski.http.{HttpTester, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers.paakayttaja
import fi.oph.koski.perustiedot.OpiskeluoikeudenPerustiedot
import org.json4s.jackson.JsonMethods
import org.scalatest.{FreeSpec, Matchers}

class OpiskeluoikeusInvalidateSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with HttpTester {
  "Opiskeluoikeuksien mitätöiminen" in {
    val oids = opiskeluoikeudet
    oids.foreach { opiskeluoikeusOid =>
      delete(s"api/opiskeluoikeus/$opiskeluoikeusOid", headers = authHeaders(paakayttaja)) {
        verifyResponseStatus(200)
        println(body)
      }
    }
    oids.foreach { opiskeluoikeusOid =>
      delete(s"api/opiskeluoikeus/$opiskeluoikeusOid", headers = authHeaders(paakayttaja)) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia"))
      }
    }
  }

  private def opiskeluoikeudet: List[String] =
    koskeenTallennetutOppijat.flatMap(oppija(_).tallennettavatOpiskeluoikeudet).flatMap(_.oid).distinct

  private def koskeenTallennetutOppijat: List[String] = authGet("api/opiskeluoikeus/perustiedot", paakayttaja) {
    verifyResponseStatus(200)
    JsonSerializer.extract[List[OpiskeluoikeudenPerustiedot]](JsonMethods.parse(body) \\ "result").map(_.henkilö.oid)
  }
}
