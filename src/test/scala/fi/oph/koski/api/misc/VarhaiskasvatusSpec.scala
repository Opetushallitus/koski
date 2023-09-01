package fi.oph.koski.api.misc

import fi.oph.koski.documentation.ExamplesEsiopetus.{ostopalvelu, päiväkodinEsiopetuksenTunniste}
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData._
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat.ysiluokkalainen
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.päiväkotiTarina
import fi.oph.koski.schema.EsiopetuksenOpiskeluoikeus
import org.scalatest.freespec.AnyFreeSpec

class VarhaiskasvatusSpec extends AnyFreeSpec with EsiopetusSpecification {
  "Varhaiskasvatuksen järjestäjä koulutustoimija" - {
    "kun järjestämismuoto syötetty" - {
      "voi luoda, lukea, päivittää ja mitätöidä päiväkodissa järjestettävän esiopetuksen opiskeluoikeuden organisaatiohierarkiansa ulkopuolelta" in {
        val opiskeluoikeus = päiväkotiEsiopetus(päiväkotiTouhula, ostopalvelu)
        val resp = putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
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

        putOpiskeluoikeus(opiskeluoikeus.copy(oid = Some(oo.oid), lisätiedot = None), headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
          readPutOppijaResponse.opiskeluoikeudet.head.versionumero should equal(2)
        }

        delete(s"api/opiskeluoikeus/${resp.opiskeluoikeudet.head.oid}", headers = authHeaders(MockUsers.helsinkiTallentaja)) {
          verifyResponseStatusOk()
        }
      }

      "ei voi tallentaa opiskeluoikeutta jonka oppilaitos on päiväkoti joka on omassa organisaatiohierarkiassa" in {
        val opiskeluoikeus = päiväkotiEsiopetus(päiväkotiVironniemi, ostopalvelu)
        putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.järjestämismuoto())
        }
      }

      "voi tallentaa opiskeluoikeuden jonka oppilaitoksena on yksityinen päiväkoti joka ei ole koulutustoimijan alla organisaatiohierarkiassa" in {
        val opiskeluoikeus = päiväkotiEsiopetus(oidOrganisaatio(päiväkotiTarina), ostopalvelu).copy(koulutustoimija = hki)
        putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "voi siirtää koulutustoimijatiedon" in {
        val opiskeluoikeus = päiväkotiEsiopetus(päiväkotiTouhula, ostopalvelu).copy(koulutustoimija = hki)
        putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "koulutustoimija voi siirtää väärän koulutustoimijatiedon ostopalvelun opiskeluoikeudessa ja väärä koulutustoimija ylikirjoitetaan käyttäjätietojen koulutustoimijalla" in {
        val opiskeluoikeus = päiväkotiEsiopetus(jyväskylänNormaalikoulu, ostopalvelu).copy(koulutustoimija = tornio)
        val resp = putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
          readPutOppijaResponse
        }
        resp.opiskeluoikeudet.size shouldBe 1
        val oo = oppija(resp.henkilö.oid).opiskeluoikeudet.find(_.oid.contains(resp.opiskeluoikeudet.head.oid))
        oo.head.koulutustoimija.head.oid == MockOrganisaatiot.helsinginKaupunki
      }

      "voi luoda perusopetuksessa järjestettävän esiopetuksen opiskeluoikeuden organisaatiohierarkian ulkopuoliselle peruskoululle" in {
        putOpiskeluoikeus(peruskouluEsiopetus(kulosaarenAlaAste, ostopalvelu), headers = authHeaders(MockUsers.pyhtäänTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "ei voi luoda perusopetuksessa järjestettävien esiopetuksen opiskeluoikeuksia organisaatiohierarkiansa ulkopuolelle varhaiskasvatuksen toimipisteeseen" in {
        putOpiskeluoikeus(peruskouluEsiopetus(päiväkotiTouhula, ostopalvelu), headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.koodisto.vääräKoulutuksenTunniste(s"Varhaiskasvatustoimipisteeseen voi tallentaa vain päiväkodin esiopetusta (koulutus $päiväkodinEsiopetuksenTunniste)"))
        }
      }

      "ei voi lukea, päivittää tai poistaa muiden luomia opiskeluoikeuksia organisaatiohierarkiansa ulkopuolelta" in {
        val esiopetuksenOpiskeluoikeus: EsiopetuksenOpiskeluoikeus = päiväkotiEsiopetus(päiväkotiTouhula, ostopalvelu)
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

        putOpiskeluoikeus(esiopetuksenOpiskeluoikeus.copy(oid = Some(oo.oid), lisätiedot = None), headers = authHeaders(MockUsers.tornioTallentaja) ++ jsonContent) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Opiskeluoikeutta ${oo.oid} ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
        }

        delete(s"api/opiskeluoikeus/${resp.opiskeluoikeudet.head.oid}", headers = authHeaders(MockUsers.tornioTallentaja)) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())
        }
      }
    }

    "kun järjestämismuotoa ei syötetty" - {
      "ei voi luoda opiskeluoikeuksia organisaatiohierarkian ulkopuolelle" in {
        val opiskeluoikeus = päiväkotiEsiopetus(päiväkotiTouhula).copy(koulutustoimija = hki)
        putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.vääräKoulutustoimija(s"Annettu koulutustoimija ${MockOrganisaatiot.helsinginKaupunki} ei vastaa organisaatiopalvelusta löytyvää koulutustoimijaa ${MockOrganisaatiot.pyhtäänKunta}"))
        }
      }

      "ei voi siirtää muiden luomia opiskeluoikeuksia organisaatiohierarkiansa ulkopuolelta oman organisaationsa alle" in {
        val esiopetuksenOpiskeluoikeus: EsiopetuksenOpiskeluoikeus = päiväkotiEsiopetus(päiväkotiTouhula, ostopalvelu)
        val opiskeluoikeusOid = putOpiskeluoikeus(esiopetuksenOpiskeluoikeus, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
          verifyResponseStatusOk()
          readPutOppijaResponse.opiskeluoikeudet.head.oid
        }

        putOpiskeluoikeus(esiopetuksenOpiskeluoikeus.copy(oid = Some(opiskeluoikeusOid), koulutustoimija = tornio), headers = authHeaders(MockUsers.tornioTallentaja) ++ jsonContent) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia(s"Opiskeluoikeutta $opiskeluoikeusOid ei löydy tai käyttäjällä ei ole oikeutta sen katseluun"))
        }
      }

      "ei voi tallentaa opiskeluoikeutta jonka oppilaitoksena on yksityinen päiväkoti joka ei ole koulutustoimijan alla organisaatiohierarkiassa" in {
        val opiskeluoikeus = päiväkotiEsiopetus(oidOrganisaatio(päiväkotiTarina))
        putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.organisaatio("Ei oikeuksia organisatioon " + päiväkotiTarina))
        }
      }
    }
  }

  "Pääkäyttäjä" - {
    "kun koulutustoimija ja järjestämismuoto on syötetty" - {
      "ei voi tallentaa opiskeluoikeutta jonka oppilaitoksena on päiväkoti joka on koulutustoimijan alla organisaatiohierarkiassa" in {
        val opiskeluoikeus = päiväkotiEsiopetus(päiväkotiVironniemi, ostopalvelu).copy(koulutustoimija = hki)
        putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.järjestämismuoto())
        }
      }

      "voi tallentaa opiskeluoikeuden jonka oppilaitoksena on päiväkoti joka ei ole koulutustoimijan alla organisaatiohierarkiassa" in {
        val opiskeluoikeus = päiväkotiEsiopetus(päiväkotiTouhula, ostopalvelu).copy(koulutustoimija = hki)
        putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }

      "voi tallentaa opiskeluoikeuden jonka oppilaitoksena on yksityinen päiväkoti joka ei ole koulutustoimijan alla organisaatiohierarkiassa" in {
        val opiskeluoikeus = päiväkotiEsiopetus(oidOrganisaatio(päiväkotiTarina), ostopalvelu).copy(koulutustoimija = hki)
        putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.paakayttaja) ++ jsonContent) {
          verifyResponseStatusOk()
        }
      }
    }
  }

  "Varhaiskasvatuksen järjestäjä kahden koulutustoimijan käyttäjä, järjestämismuoto syötetty" - {
    "voi luoda ja muokata esiopetuksen opiskeluoikeuden organisaatiohierarkiansa ulkopuolelle jos koulutustoimija on syötetty" in {
      val opiskeluoikeus = päiväkotiEsiopetus(päiväkotiTouhula, ostopalvelu).copy(koulutustoimija = hki)
      val resp = putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.helsinkiSekäTornioTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
        readPutOppijaResponse
      }

      putOpiskeluoikeus(opiskeluoikeus.copy(oid = Some(resp.opiskeluoikeudet.head.oid), koulutustoimija = tornio), headers = authHeaders(MockUsers.helsinkiSekäTornioTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
      }
    }

    "ei voi luoda ilman koulutustoimijatietoa" in {
      val opiskeluoikeus = päiväkotiEsiopetus(päiväkotiTouhula, ostopalvelu)
      putOpiskeluoikeus(opiskeluoikeus, headers = authHeaders(MockUsers.helsinkiSekäTornioTallentaja) ++ jsonContent) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.koulutustoimijaPakollinen(s"Koulutustoimijaa ei voi yksiselitteisesti päätellä käyttäjätunnuksesta. Koulutustoimija on pakollinen."))
      }
    }
  }

  "Koulutustoimija joka ei ole varhaiskasvatuksen järjestäjä" - {
    "ei voi luoda päiväkodissa järjestettävän esiopetuksen opiskeluoikeuden organisaatiohierarkiansa ulkopuolella" in {
      putOpiskeluoikeus(päiväkotiEsiopetus(päiväkotiTouhula, ostopalvelu), headers = authHeaders(MockUsers.jyväskyläTallentaja) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainVarhaiskasvatuksenJärjestäjä("Operaatio on sallittu vain käyttäjälle joka on luotu varhaiskasvatusta järjestävälle koulutustoimijalle"))
      }
    }
  }

  "Päiväkodin virkailija" - {
    lazy val eeronOpiskeluoikeus = päiväkotiEsiopetus(päiväkotiTouhula, ostopalvelu)
    lazy val eeroResp = putOpiskeluoikeus(eeronOpiskeluoikeus, henkilö = defaultHenkilö, headers = authHeaders(MockUsers.helsinkiTallentaja) ++ jsonContent) {
      verifyResponseStatusOk()
      readPutOppijaResponse
    }

    "näkee kaikki omaan organisaatioon luodut opiskeluoikeudet" in {
      val ysiluokkalainenResp = putOpiskeluoikeus(päiväkotiEsiopetus(päiväkotiTouhula, ostopalvelu), henkilö = asUusiOppija(ysiluokkalainen), headers = authHeaders(MockUsers.tornioTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
        readPutOppijaResponse
      }

      authGet(s"api/opiskeluoikeus/${eeroResp.opiskeluoikeudet.head.oid}", user = MockUsers.touholaTallentaja) {
        verifyResponseStatusOk()
      }
      authGet(s"api/opiskeluoikeus/${ysiluokkalainenResp.opiskeluoikeudet.head.oid}", user = MockUsers.touholaTallentaja) {
        verifyResponseStatusOk()
      }

      val eeronKoskeenTallennetutOpiskeluoikeudet = oppija(eeroResp.henkilö.oid, MockUsers.touholaTallentaja).opiskeluoikeudet.flatMap(_.oid)
      eeronKoskeenTallennetutOpiskeluoikeudet.intersect(eeroResp.opiskeluoikeudet.map(_.oid)) should not be empty
      val ysiluokkalaisenKoskeenTallennetutOpiskeluoikeudet = oppija(ysiluokkalainenResp.henkilö.oid, MockUsers.touholaTallentaja).opiskeluoikeudet.flatMap(_.oid)
      ysiluokkalaisenKoskeenTallennetutOpiskeluoikeudet.intersect(ysiluokkalainenResp.opiskeluoikeudet.map(_.oid)) should not be empty
    }

    "Ei voi muokata toisen koulutustoimijan luomia opiskeluoikeuksia" in {
      putOpiskeluoikeus(eeronOpiskeluoikeus.copy(oid = Some(eeroResp.opiskeluoikeudet.head.oid), koulutustoimija = hki, lisätiedot = None), headers = authHeaders(MockUsers.touholaTallentaja) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.vainVarhaiskasvatuksenJärjestäjä("Operaatio on sallittu vain käyttäjälle joka on luotu varhaiskasvatusta järjestävälle koulutustoimijalle"))
      }
    }

    "Ei ylikirjoita koulutustoimijan luomia opiskeluoikeuksia" in {
      val resp= putOpiskeluoikeus(päiväkotiEsiopetus(päiväkotiTouhula), henkilö = defaultHenkilö, headers = authHeaders(MockUsers.pyhtäänTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
        readPutOppijaResponse
      }
      resp.opiskeluoikeudet.length should equal(1)
      resp.opiskeluoikeudet.head.versionumero should equal(1)
    }
  }

  "Varhaiskasvatustoimipisteeseen" - {
    "ei voi tallentaa muita kuin päiväkodin esiopetuksen opiskeluoikeuksia" in {
      putOpiskeluoikeus(päiväkotiEsiopetus(päiväkotiTouhula), headers = authHeaders(MockUsers.pyhtäänTallentaja) ++ jsonContent) {
        verifyResponseStatusOk()
      }
      putOpiskeluoikeus(peruskouluEsiopetus(päiväkotiTouhula), headers = authHeaders(MockUsers.pyhtäänTallentaja) ++ jsonContent) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.koodisto.vääräKoulutuksenTunniste("Varhaiskasvatustoimipisteeseen voi tallentaa vain päiväkodin esiopetusta (koulutus 001102)"))
      }
    }
  }
}
