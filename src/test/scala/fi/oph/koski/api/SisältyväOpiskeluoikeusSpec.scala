package fi.oph.koski.api

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, OidOrganisaatio, Oppilaitos, SisältäväOpiskeluoikeus}
import org.scalatest.{FreeSpec, Matchers}
import fi.oph.koski.documentation.AmmatillinenExampleData._

class SisältyväOpiskeluoikeusSpec extends FreeSpec with Matchers with OpiskeluoikeusTestMethodsAmmatillinen with SearchTestMethods with LocalJettyHttpSpecification {
  "Sisältyvä opiskeluoikeus" - {
    lazy val fixture = new {
      resetFixtures
      val original: AmmatillinenOpiskeluoikeus = createOpiskeluoikeus(defaultHenkilö, defaultOpiskeluoikeus, user = MockUsers.stadinAmmattiopistoTallentaja)

      val sisältyvä: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
        oppilaitos = Some(Oppilaitos(MockOrganisaatiot.omnia)),
        sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(original.oppilaitos.get, original.oid.get)),
        suoritukset = List(autoalanPerustutkinnonSuoritus(OidOrganisaatio(MockOrganisaatiot.omnia)))
      )
    }

    "Kun sisältävä opiskeluoikeus löytyy Koskesta" - {
      lazy val sisältyvä = createOpiskeluoikeus(defaultHenkilö, fixture.sisältyvä, user = MockUsers.omniaTallentaja)
      "Lisäys onnistuu" in {
        sisältyvä.oid.isDefined should equal(true)
      }

      "Sisältävän opiskeluoikeuden organisaatiolla on katseluoikeudet sisältyvään opiskeluoikeuteen" in {
        val oids = getOpiskeluoikeudet(MockOppijat.eero.oid, MockUsers.stadinAmmattiopistoTallentaja).flatMap(_.oid)
        oids should contain(fixture.original.oid.get)
        oids should contain(sisältyvä.oid.get)
      }

      "Sisältyvän opiskeluoikeuden organisaatiolla ei ole oikeuksia sisältävään opiskeluoikeuteen" in {
        val oids = getOpiskeluoikeudet(MockOppijat.eero.oid, MockUsers.omniaKatselija).flatMap(_.oid)
        oids should contain(sisältyvä.oid.get)
        oids should not contain(fixture.original.oid)
      }

      "Sisältävän opiskeluoikeuden organisaatiolla ei ole kirjoitusoikeuksia sisältyvään opiskeluoikeuteen" in {
        putOpiskeluoikeus(sisältyvä, headers = authHeaders(MockUsers.stadinAmmattiopistoTallentaja) ++ jsonContent) {
          verifyResponseStatus(403)
        }
        putOpiskeluoikeus(sisältyvä, headers = authHeaders(MockUsers.omniaTallentaja) ++ jsonContent) {
          verifyResponseStatus(200)
        }
      }

      "Sisältävän opiskeluoikeuden organisaatio löytää sisältyvän opiskeluoikeuden hakutoiminnolla" in {
        searchForPerustiedot(Map("toimipiste" -> MockOrganisaatiot.stadinAmmattiopisto), MockUsers.stadinAmmattiopistoTallentaja).map(_.oid) should contain(fixture.original.oid.get)
        searchForPerustiedot(Map("toimipiste" -> MockOrganisaatiot.omnia), MockUsers.stadinAmmattiopistoTallentaja).map(_.oid) should contain(sisältyvä.oid.get)
      }

      "Sisältyvän opiskeluoikeuden organisaatio ei löydä sisältävää opiskeluoikeutta hakutoiminnolla" in {
        searchForPerustiedot(Map("toimipiste" -> MockOrganisaatiot.stadinAmmattiopisto), MockUsers.omniaKatselija).map(_.oid) should equal(Nil)
        searchForPerustiedot(Map("toimipiste" -> MockOrganisaatiot.omnia), MockUsers.omniaKatselija).map(_.oid) should contain(sisältyvä.oid.get)
      }
    }

    "Kun sisältävä opiskeluoikeus ei löydy Koskesta -> HTTP 400" in {
      putOpiskeluoikeus(fixture.sisältyvä.copy( sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(fixture.original.oppilaitos.get, "66666666")))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.eiLöydy("Sisältävää opiskeluoikeutta ei löydy oid-arvolla 66666666"))
      }
    }

    "Kun sisältävän opiskeluoikeuden organisaatio ei täsmää -> HTTP 400" in {
      putOpiskeluoikeus(fixture.sisältyvä.copy( sisältyyOpiskeluoikeuteen = Some(SisältäväOpiskeluoikeus(Oppilaitos(MockOrganisaatiot.omnia), fixture.original.oid.get)))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.vääräOppilaitos())
      }
     }

    "Kun sisältävän opiskeluoikeuden henkilötieto ei täsmää -> HTTP 400" in {
      putOpiskeluoikeus(fixture.sisältyvä, henkilö = MockOppijat.eerola.vainHenkilötiedot) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.sisältäväOpiskeluoikeus.henkilöTiedot())
      }
    }
  }
}