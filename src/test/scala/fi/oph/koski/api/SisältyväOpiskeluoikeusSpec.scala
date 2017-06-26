package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, Oppilaitos, SisältäväOpiskeluoikeus}
import org.scalatest.{FreeSpec, Matchers}

class SisältyväOpiskeluoikeusSpec extends FreeSpec with Matchers with OpiskeluoikeusTestMethodsAmmatillinen with LocalJettyHttpSpecification {
  "Sisältyvä opiskeluoikeus" - {
    lazy val fixture = new {
      resetFixtures
      val original: AmmatillinenOpiskeluoikeus = createOpiskeluoikeus(defaultHenkilö, defaultOpiskeluoikeus)
      val sisältyvä: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
        oppilaitos = Some(Oppilaitos(MockOrganisaatiot.omnia)),
        sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(original.id.get, original.oppilaitos.get))
      )
    }

    "Kun viitattu opiskeluoikeus löytyy Koskesta" in {
      putOpiskeluoikeus(fixture.sisältyvä) {
        verifyResponseStatus(200)
      }
    }

    "Kun viitattu opiskeluoikeus ei löydy Koskesta" in {
      putOpiskeluoikeus(fixture.sisältyvä.copy( sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(66666666, fixture.original.oppilaitos.get)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.eiLöydy("Sisältävää opiskeluoikeutta ei löydy id-arvolla 66666666"))
      }
    }

    "Kun viitatun opiskeluoikeuden organisaatio ei täsmää" in {
      putOpiskeluoikeus(fixture.sisältyvä.copy( sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(fixture.original.id.get, Oppilaitos(MockOrganisaatiot.omnia))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.vääräOppilaitos())
      }
     }

    "Kun viitatun opiskeluoikeuden henkilötieto ei täsmää" in {
      putOpiskeluoikeus(fixture.sisältyvä, henkilö = MockOppijat.eerola.vainHenkilötiedot) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.henkilöTiedot())
      }
    }
  }
}