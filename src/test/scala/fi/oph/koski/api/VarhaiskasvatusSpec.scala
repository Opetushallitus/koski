package fi.oph.koski.api

import fi.oph.koski.documentation.ExamplesEsiopetus.{peruskoulunEsiopetuksenTunniste, päiväkodinEsiopetuksenTunniste, suoritus}
import fi.oph.koski.documentation.ReforminMukainenErikoisammattitutkintoExample.{opiskeluoikeus => ammatillinenOpiskeluoikeus}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.päiväkotiTouhula
import fi.oph.koski.henkilo.MockOppijat.{asUusiOppija, ysiluokkalainen}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.MockOrganisaatiot.{helsinginKaupunki, jyväskylänYliopisto, tornionKaupunki}
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus
import org.scalatest.FreeSpec

class VarhaiskasvatusSpec extends FreeSpec with EsiopetusSpecification {
  "Varhaiskasvatuksen järjestäjä koulutustoimija" - {
    "voi luoda, lukea, päivittää ja mitätöidä päiväkodissa järjestettävän esiopetuksen opiskeluoikeuden organisaatiohierarkiansa ulkopuolelta" in {
      val resp = putOpiskeluoikeus(päiväkotiEsiopetus.copy(koulutustoimija = hki), headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
        readPutOppijaResponse
      }

      val oo = resp.opiskeluoikeudet.head
      oo.versionumero should equal(1)

      authGet(s"api/opiskeluoikeus/${oo.oid}", user = MockUsers.helsinkiTallentaja) {
        verifyResponseStatusOk()
      }

      authGet(s"api/oppija/${resp.henkilö.oid}", user = MockUsers.helsinkiTallentaja) {
        verifyResponseStatusOk()
      }

      putOpiskeluoikeus(päiväkotiEsiopetus.copy(oid = Some(oo.oid), koulutustoimija = hki, lisätiedot = None), headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
        readPutOppijaResponse.opiskeluoikeudet.head.versionumero should equal(2)
      }

      delete(s"api/opiskeluoikeus/${resp.opiskeluoikeudet.head.oid}", headers = authHeaders(MockUsers.helsinkiTallentaja)) {
        verifyResponseStatusOk()
      }
    }

    "ei voi luoda perusopetuksessa järjestettävien esiopetuksen opiskeluoikeuksia organisaatiohierarkiansa ulkopuolelle" in {
      putOpiskeluoikeus(peruskouluEsiopetus.copy(koulutustoimija = hki), headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.vääräKoulutustoimija("Annettu koulutustoimija 1.2.246.562.10.346830761110 ei vastaa organisaatiopalvelusta löytyvää koulutustoimijaa 1.2.246.562.10.69417312936"))
      }
    }

    "ei voi luoda muun tyyppisiä opiskeluoikeuksia organisaatiohierarkiansa ulkopuolelle" in {
      putOpiskeluoikeus(ammatillinenOpiskeluoikeus.copy(koulutustoimija = tornio), headers = authHeaders(MockUsers.tornioTallentaja) ++ jsonContent) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.vääräKoulutustoimija("Annettu koulutustoimija 1.2.246.562.10.25412665926 ei vastaa organisaatiopalvelusta löytyvää koulutustoimijaa 1.2.246.562.10.346830761110"))
      }
    }

    "ei voi lukea, päivittää tai poistaa muiden luomia opiskeluoikeuksia organisaatiohierarkiansa ulkopuolelta" in {
      val esiopetuksenOpiskeluoikeus: EsiopetuksenOpiskeluoikeus = päiväkotiEsiopetus.copy(koulutustoimija = hki)
      val resp = putOpiskeluoikeus(esiopetuksenOpiskeluoikeus, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
        readPutOppijaResponse
      }
      val oo = resp.opiskeluoikeudet.head

      authGet(s"api/opiskeluoikeus/${oo.oid}", user = MockUsers.tornioTallentaja) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }

      authGet(s"api/oppija/${resp.henkilö.oid}", user = MockUsers.tornioTallentaja) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa 1.2.246.562.24.00000000001 ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }

      putOpiskeluoikeus(päiväkotiEsiopetus.copy(oid = Some(oo.oid), koulutustoimija = hki, lisätiedot = None), headers = authHeaders(MockUsers.tornioTallentaja) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio(s"Ei oikeuksia organisatioon ${MockOrganisaatiot.päiväkotiTouhula}"))
      }

      delete(s"api/opiskeluoikeus/${resp.opiskeluoikeudet.head.oid}", headers = authHeaders(MockUsers.tornioTallentaja)) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
      }
    }

    "ei voi siirtää muiden luomia opiskeluoikeuksia organisaatiohierarkiansa ulkopuolelta oman organisaationsa alle" in {
      val esiopetuksenOpiskeluoikeus: EsiopetuksenOpiskeluoikeus = päiväkotiEsiopetus.copy(koulutustoimija = hki)
      val opiskeluoikeusOid = putOpiskeluoikeus(esiopetuksenOpiskeluoikeus, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
        readPutOppijaResponse.opiskeluoikeudet.head.oid
      }

      putOpiskeluoikeus(esiopetuksenOpiskeluoikeus.copy(oid = Some(opiskeluoikeusOid), koulutustoimija = tornio), headers = authHeaders(MockUsers.tornioTallentaja) ++ jsonContent) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Opiskeluoikeutta $opiskeluoikeusOid ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
      }
    }
  }

  "Koulutustoimija joka ei ole varhaiskasvatuksen järjestäjä" - {
    "ei voi luoda päiväkodissa järjestettävän esiopetuksen opiskeluoikeuden organisaatiohierarkiansa ulkopuolella" in {
      putOpiskeluoikeus(päiväkotiEsiopetus.copy(koulutustoimija = jyväskylä), headers = authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio(s"Ei oikeuksia organisatioon ${MockOrganisaatiot.päiväkotiTouhula}"))
      }
    }
  }

  "Päiväkodin virkailija" - {
    "näkee kaikki omaan organisaatioon luodut opiskeluoikeudet" in {
      val resp1 = putOpiskeluoikeus(päiväkotiEsiopetus.copy(koulutustoimija = hki), headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
        readPutOppijaResponse
      }
      val resp2 = putOpiskeluoikeus(päiväkotiEsiopetus.copy(koulutustoimija = tornio), henkilö = asUusiOppija(ysiluokkalainen), headers = authHeaders(MockUsers.tornioTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
        readPutOppijaResponse
      }

      authGet(s"api/opiskeluoikeus/${resp1.opiskeluoikeudet.head.oid}", user = MockUsers.touholaKatselija) {
        verifyResponseStatusOk()
      }
      authGet(s"api/opiskeluoikeus/${resp2.opiskeluoikeudet.head.oid}", user = MockUsers.touholaKatselija) {
        verifyResponseStatusOk()
      }

      oppija(resp1.henkilö.oid, MockUsers.touholaKatselija).opiskeluoikeudet.flatMap(_.oid) should equal(resp1.opiskeluoikeudet.map(_.oid))
      oppija(resp2.henkilö.oid, MockUsers.touholaKatselija).opiskeluoikeudet.flatMap(_.oid) should equal(resp2.opiskeluoikeudet.map(_.oid))
    }
  }

  private lazy val hki = MockOrganisaatioRepository.getOrganisaatioHierarkia(helsinginKaupunki).flatMap(_.toKoulutustoimija)
  private lazy val tornio = MockOrganisaatioRepository.getOrganisaatioHierarkia(tornionKaupunki).flatMap(_.toKoulutustoimija)
  private lazy val jyväskylä = MockOrganisaatioRepository.getOrganisaatioHierarkia(jyväskylänYliopisto).flatMap(_.toKoulutustoimija)
  private lazy val päiväkotiEsiopetus: EsiopetuksenOpiskeluoikeus = defaultOpiskeluoikeus.copy(oppilaitos = None, suoritukset = List(suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = päiväkodinEsiopetuksenTunniste, päiväkotiTouhula)))
  private lazy val peruskouluEsiopetus: EsiopetuksenOpiskeluoikeus = defaultOpiskeluoikeus.copy(oppilaitos = None, suoritukset = List(suoritus(perusteenDiaarinumero = "102/011/2014", tunniste = peruskoulunEsiopetuksenTunniste, päiväkotiTouhula)))
}
