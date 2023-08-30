package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class ClassReferenceSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec with OpiskeluoikeusTestMethods {

  "GET /api/oppija/:oid" - {
    "Should show $class metadata with class_refs=true" in {
      get("api/oppija/" + KoskiSpecificMockOppijat.eero.oid, params = List(("class_refs", "true")), headers = authHeaders()) {
        verifyResponseStatusOk()
        body should include("\"$class\":\"fi.oph.koski.schema.Koodistokoodiviite\"")
        body should include("\"$class\":\"fi.oph.koski.schema.AmmatillinenOpiskeluoikeus\"")
      }
    }
    "Should not show $class metadata with class_refs=false" in {
      get("api/oppija/" + KoskiSpecificMockOppijat.eero.oid, params = List(("class_refs", "false")), headers = authHeaders()) {
        verifyResponseStatusOk()
        body should not include("\"$class\":\"fi.oph.koski.schema.Koodistokoodiviite\"")
        body should not include("\"$class\":\"fi.oph.koski.schema.AmmatillinenOpiskeluoikeus\"")
      }
    }
  }
}

