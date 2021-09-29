package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{Koodistokoodiviite, OpiskeluoikeudenTyyppi}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class OppilaitosServletSpec extends AnyFreeSpec with Matchers with KoskiHttpSpec {
  "Mahdolliset opiskeluoikeuden tyypit organisaatiolle" - {
    "Palautetaan vain ne opiskeluoikeuden tyypit joihin annetulla käyttäjällä on oikeus" in {
      authGet(s"api/oppilaitos/opiskeluoikeustyypit/${MockOrganisaatiot.kulosaarenAlaAste}", headers = authHeaders(MockUsers.esiopetusTallentaja)) {
        verifyResponseStatusOk()
        JsonSerializer.parse[List[Koodistokoodiviite]](body).map(_.koodiarvo) should equal(List(OpiskeluoikeudenTyyppi.esiopetus.koodiarvo))
      }
    }
  }
}
