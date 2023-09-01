package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.schema.AmmatillinenOpiskeluoikeus
import fi.oph.koski.{DirtiesFixtures, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec

class OppijaValidationKayttooikeudetSpec
  extends AnyFreeSpec
    with OpiskeluoikeusTestMethodsAmmatillinen
    with KoskiHttpSpec
    with DirtiesFixtures {

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
