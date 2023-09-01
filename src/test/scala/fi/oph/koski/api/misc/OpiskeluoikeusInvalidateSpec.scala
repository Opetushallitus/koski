package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.paakayttaja
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OpiskeluoikeusInvalidateSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec with OpiskeluoikeusTestMethods {
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
