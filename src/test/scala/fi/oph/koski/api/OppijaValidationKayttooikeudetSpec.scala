package fi.oph.koski.api

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.schema.AmmatillinenOpiskeluoikeus
import org.scalatest.{BeforeAndAfterAll, FreeSpec}

class OppijaValidationKayttooikeudetSpec
  extends FreeSpec
    with BeforeAndAfterAll
    with OpiskeluoikeusTestMethodsAmmatillinen
    with LocalJettyHttpSpecification {

  override def afterAll: Unit = resetFixtures

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
