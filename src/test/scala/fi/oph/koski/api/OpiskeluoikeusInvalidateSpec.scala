package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.{HttpTester, KoskiErrorCategory}
import fi.oph.koski.koskiuser.MockUsers.paakayttaja
import org.scalatest.{FreeSpec, Matchers}

class OpiskeluoikeusInvalidateSpec extends FreeSpec with Matchers with KoskiHttpSpec with OpiskeluoikeusTestMethods with HttpTester {
  "Opiskeluoikeuksien mitätöiminen" in {
    val kaikkiOpiskeluoikeudet = koskeenTallennetutOpiskeluoikeudet.flatMap(_.oid)
    kaikkiOpiskeluoikeudet should not be empty
    kaikkiOpiskeluoikeudet.foreach { oid =>
      delete(s"api/opiskeluoikeus/$oid", headers = authHeaders(paakayttaja)) {
        verifyResponseStatusOk()
      }
      delete(s"api/opiskeluoikeus/$oid", headers = authHeaders(paakayttaja)) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia"))
      }
    }
  }
}
