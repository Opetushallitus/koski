package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.paakayttaja
import org.scalatest.{FreeSpec, Matchers}

class OpiskeluoikeusInvalidateSpec extends FreeSpec with Matchers with KoskiHttpSpec with OpiskeluoikeusTestMethods {
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
