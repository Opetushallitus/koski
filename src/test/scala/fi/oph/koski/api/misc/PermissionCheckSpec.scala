package fi.oph.koski.api.misc

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.permission.PermissionCheckResponse
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema.{Henkilö, Organisaatio}
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PermissionCheckSpec extends AnyFreeSpec with KoskiHttpSpec with Matchers {

  "Käyttöoikeustarkistus henkilo-ui:sta / kayttooikeus-service:ltä" - {
    "Käyttäjä samassa organisaatiossa kuin opiskeluoikeus" - {
      "pääsy sallittu" in { permissionCheck(List(KoskiSpecificMockOppijat.lukioKesken.oid), List(MockOrganisaatiot.jyväskylänNormaalikoulu)) should equal(true) }
    }
    "Käyttäjä eri organisaatiossa kuin opiskeluoikeus" - {
      "pääsy estetty" in { permissionCheck(List(KoskiSpecificMockOppijat.lukioKesken.oid), List(MockOrganisaatiot.winnova)) should equal(false) }
    }
    "Käyttäjä samassa organisaatiossa kuin opiskeluoikeus, mutta opiskeluoikeus päättynyt" - {
      "pääsy estetty" in { permissionCheck(List(KoskiSpecificMockOppijat.lukiolainen.oid), List(MockOrganisaatiot.jyväskylänNormaalikoulu)) should equal(false) }
    }
    "Käyttäjällä myös ylimääräisiä organisaatioita" - {
      "pääsy sallittu" in { permissionCheck(List(KoskiSpecificMockOppijat.lukioKesken.oid), MockOrganisaatiot.oppilaitokset) should equal(true) }
    }
    "Korkeakoulun opiskeluoikeus, sama organisaatio" - {
      "pääsy sallittu" in { permissionCheck(List(KoskiSpecificMockOppijat.amkKesken.oid), List(MockOrganisaatiot.yrkehögskolanArcada)) should equal(true) }
    }
    "Korkeakoulun opiskeluoikeus, eri organisaatio" - {
      "pääsy sallittu" in { permissionCheck(List(KoskiSpecificMockOppijat.amkKesken.oid), List(MockOrganisaatiot.omnia)) should equal(false) }
    }
    "Korkeakoulun opiskeluoikeus, valmistunut" - {
      "pääsy estetty" in { permissionCheck(List(KoskiSpecificMockOppijat.amkValmistunut.oid), List(MockOrganisaatiot.aaltoYliopisto)) should equal(false) }
    }
    "Käyttäjältä puuttuu tarvittavat roolit" - {
      "pääsy estetty" in { permissionCheck(List(KoskiSpecificMockOppijat.lukioKesken.oid), List(MockOrganisaatiot.jyväskylänNormaalikoulu), List("ROLE_APP_FOOBAR")) should equal(false) }
    }
  }

  def permissionCheck(personOidsForSamePerson: List[Henkilö.Oid], organisationOids: List[Organisaatio.Oid], loggedInUserRoles: List[String] = List("ROLE_APP_KOSKI", "ROLE_APP_OPPIJANUMEROREKISTERI_HENKILON_RU")): Boolean = {
    post(
      "api/permission/checkpermission",
      JsonSerializer.writeWithRoot(TestPermissionCheckRequest(
        personOidsForSamePerson = personOidsForSamePerson,
        loggedInUserOid = personOidsForSamePerson,
        organisationOids = organisationOids,
        loggedInUserRoles = loggedInUserRoles)),
      headers = jsonContent
    ) {
      verifyResponseStatusOk()
      implicit val context: ExtractionContext = strictDeserialization
      SchemaValidatingExtractor.extract[PermissionCheckResponse](body).right.get.accessAllowed
    }
  }
}

case class TestPermissionCheckRequest(personOidsForSamePerson: List[Henkilö.Oid], loggedInUserOid: List[Henkilö.Oid], organisationOids: List[Organisaatio.Oid], loggedInUserRoles: List[String])
