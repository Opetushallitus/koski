package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.permission.{PermissionCheckRequest, PermissionCheckResponse}
import fi.oph.koski.schema.{Henkilö, Organisaatio}
import fi.oph.scalaschema.SchemaValidatingExtractor
import org.scalatest.{FreeSpec, Matchers}

class PermissionCheckSpec extends FreeSpec with LocalJettyHttpSpecification with Matchers {
  "Käyttöoikeustarkistus henkilo-ui:sta / kayttooikeus-service:ltä" - {
    "Käyttäjä samassa organisaatiossa kuin opiskeluoikeus" - {
      "pääsy sallittu" in { permissionCheck(List(MockOppijat.lukioKesken.oid), List(MockOrganisaatiot.jyväskylänNormaalikoulu)) should equal(true) }
    }
    "Käyttäjä eri organisaatiossa kuin opiskeluoikeus" - {
      "pääsy estetty" in { permissionCheck(List(MockOppijat.lukioKesken.oid), List(MockOrganisaatiot.winnova)) should equal(false) }
    }
    "Käyttäjä samassa organisaatiossa kuin opiskeluoikeus, mutta opiskeluoikeus päättynyt" - {
      "pääsy estetty" in { permissionCheck(List(MockOppijat.lukiolainen.oid), List(MockOrganisaatiot.jyväskylänNormaalikoulu)) should equal(false) }
    }
    "Käyttäjällä myös ylimääräisiä organisaatioita" - {
      "pääsy sallittu" in { permissionCheck(List(MockOppijat.lukioKesken.oid), MockOrganisaatiot.oppilaitokset) should equal(true) }
    }
    "Korkeakoulun opiskeluoikeus, sama organisaatio" - {
      "pääsy sallittu" in { permissionCheck(List(MockOppijat.amkKesken.oid), List(MockOrganisaatiot.yrkehögskolanArcada)) should equal(true) }
    }
    "Korkeakoulun opiskeluoikeus, eri organisaatio" - {
      "pääsy sallittu" in { permissionCheck(List(MockOppijat.amkKesken.oid), List(MockOrganisaatiot.omnia)) should equal(false) }
    }
    "Korkeakoulun opiskeluoikeus, valmistunut" - {
      "pääsy estetty" in { permissionCheck(List(MockOppijat.amkValmistunut.oid), List(MockOrganisaatiot.aaltoYliopisto)) should equal(false) }
    }
    "Käyttäjältä puuttuu tarvittavat roolit" - {
      "pääsy estetty" in { permissionCheck(List(MockOppijat.lukioKesken.oid), List(MockOrganisaatiot.jyväskylänNormaalikoulu), List("ROLE_APP_FOOBAR")) should equal(false) }
    }
  }

  import fi.oph.koski.schema.KoskiSchema.deserializationContext

  def permissionCheck(personOidsForSamePerson: List[Henkilö.Oid], organisationOids: List[Organisaatio.Oid], loggedInUserRoles: List[String] = List("ROLE_APP_KOSKI", "ROLE_APP_HENKILONHALLINTA_CRUD")): Boolean = {
    post(
      "api/permission/checkpermission",
      JsonSerializer.writeWithRoot(PermissionCheckRequest(
        personOidsForSamePerson = personOidsForSamePerson,
        organisationOids = organisationOids,
        loggedInUserRoles = loggedInUserRoles)),
      headers = jsonContent
    ) {
      verifyResponseStatusOk()
      SchemaValidatingExtractor.extract[PermissionCheckResponse](body).right.get.accessAllowed
    }
  }
}
