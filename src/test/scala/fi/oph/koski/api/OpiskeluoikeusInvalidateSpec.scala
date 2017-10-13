package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.{HttpTester, KoskiErrorCategory}
import fi.oph.koski.koskiuser.MockUsers.paakayttaja
import org.scalatest.{FreeSpec, Matchers}

class OpiskeluoikeusInvalidateSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethods with HttpTester {
  "Opiskeluoikeuksien mitätöiminen" in {
    val opiskeluoikeusOid = oppija(MockOppijat.eero.oid).tallennettavatOpiskeluoikeudet.flatMap(_.oid).head
    delete(s"api/opiskeluoikeus/$opiskeluoikeusOid", headers = authHeaders(paakayttaja)) {
      verifyResponseStatus(200)
    }
    delete(s"api/opiskeluoikeus/$opiskeluoikeusOid", headers = authHeaders(paakayttaja)) {
      verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia"))
    }
  }
}
