package fi.oph.koski.kela

import fi.oph.koski.api.{LocalJettyHttpSpecification, OpiskeluoikeusTestMethodsAmmatillinen}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}

class KelaSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with Matchers with BeforeAndAfterAll {

  "KelaSpec" - {
    "KelaLaaja käyttöoikeuksilla voi kutsua yksittäishakua" in {
      postHetu(MockOppijat.amis.hetu.get) {
        verifyResponseStatusOk()
      }
    }
  }

  private def postHetu[A](hetu: String)(f: => A): A = {
    post(
      "api/luovutuspalvelu/kela/hetu",
      JsonSerializer.writeWithRoot(KelaRequest(hetu)),
      headers = authHeaders(MockUsers.kelaLaajatOikeudet) ++ jsonContent
    )(f)
  }
}
