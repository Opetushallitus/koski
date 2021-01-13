package fi.oph.koski.api

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.common.koskiuser.MockUsers
import fi.oph.koski.schema.AmmatillinenOpiskeluoikeus
import org.scalatest.FreeSpec

class OppijaValidationKayttooikeudetSpec extends FreeSpec with OpiskeluoikeusTestMethodsAmmatillinen with LocalJettyHttpSpecification {
  "Opiskeluoikeuksien tyyppikohtainen käyttöoikeuksien tarkistus" - {

    "Käyttäjällä ei ole oikeuksia annettuun opiskeluoikeuden tyyppiin" in {
      val opiskeluoikeus: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus
      val esiopetusTallentaja = authHeaders(MockUsers.esiopetusTallentaja)
      putOpiskeluoikeus(opiskeluoikeus, headers = esiopetusTallentaja ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.opiskeluoikeudenTyyppi("Ei oikeuksia opiskeluoikeuden tyyppiin ammatillinenkoulutus"))
      }
    }
  }
}
