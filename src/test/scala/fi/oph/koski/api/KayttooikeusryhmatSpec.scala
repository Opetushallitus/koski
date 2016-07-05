package fi.oph.koski.api

import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import org.scalatest.{FreeSpec, Matchers}

class KäyttöoikeusryhmätSpec extends FreeSpec with Matchers with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen with SearchTestMethods with QueryTestMethods {
  "koski-oph-pääkäyttäjä" - {
    val user = MockUsers.paakayttaja
    "voi muokata kaikkia opiskeluoikeuksia" in {
      putOpiskeluOikeus(defaultOpiskeluoikeus, headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(200)
      }
    }

    "voi hakea kaikkia opiskeluoikeuksia" in {
      searchForNames("eero", user) should equal(List("Jouni Eerola", "Eero Esimerkki", "Eero Markkanen"))
    }

    "voi hakea ja katsella kaikkia opiskeluoikeuksia" in {
      queryOppijat(user = user).length should be >= 10
      authGet("api/oppija/" + MockOppijat.ammattilainen.oid, user) {
        verifyResponseStatus(200)
      }
    }
  }

  "koski-viranomainen-katselija" - {
    val user = MockUsers.viranomainen

    "ei voi muokata opiskeluoikeuksia" in {
      putOpiskeluOikeus(defaultOpiskeluoikeus, headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403)
      }
    }

    "voi hakea ja katsella kaikkia opiskeluoikeuksia" in {
      queryOppijat(user = user).length should be >= 10
      authGet("api/oppija/" + MockOppijat.ammattilainen.oid, user) {
        verifyResponseStatus(200)
      }
    }
  }

  "koski-oppilaitos-palvelukäyttäjä" - {
    val user = MockUsers.hiiri
    "voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      putOpiskeluOikeus(defaultOpiskeluoikeus.copy(oppilaitos = Oppilaitos(MockOrganisaatiot.omnomnia)), henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(200)
      }
    }

    "voi hakea ja katsella opiskeluoikeuksia vain omassa organisaatiossa" in {
      searchForNames("eero", user) should equal(List("Eero Markkanen"))
      authGet("api/oppija/" + MockOppijat.markkanen.oid, user) {
        verifyResponseStatus(200)
      }
    }

    "voi hakea opiskeluoikeuksia kyselyrajapinnasta" in {
      queryOppijat(user = user).map(_.henkilö.asInstanceOf[TäydellisetHenkilötiedot].sukunimi) should equal(List("Markkanen"))
    }

    "ei voi muokata opiskeluoikeuksia muussa organisaatiossa" in {
      putOpiskeluOikeus(defaultOpiskeluoikeus, headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403)
      }
    }

    "ei voi katsella opiskeluoikeuksia muussa organisaatiossa" in {
      authGet("api/oppija/" + MockOppijat.eero.oid, user) {
        verifyResponseStatus(404)
      }
    }
  }

  "koski-oppilaitos-katselija" - {
    val user = MockUsers.hiiriKatselija
    "ei voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      putOpiskeluOikeus(defaultOpiskeluoikeus.copy(oppilaitos = Oppilaitos(MockOrganisaatiot.omnomnia)), henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403)
      }
    }

    "voi hakea ja katsella opiskeluoikeuksia omassa organisaatiossa" in {
      searchForNames("eero", user) should equal(List("Eero Markkanen"))
      authGet("api/oppija/" + MockOppijat.markkanen.oid, user) {
        verifyResponseStatus(200)
      }
    }
  }
}
