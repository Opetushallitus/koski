package fi.oph.koski.api

import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
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

    "voi hakea ja katsella ytr-ylioppilastutkintosuorituksia" in {
      haeOpiskeluoikeudetHetulla("250493-602S", user).filter(_.tyyppi.koodiarvo == "ylioppilastutkinto").length should equal(1)
    }

    "voi hakea ja katsella virta-ylioppilastutkintosuorituksia" in {
      haeOpiskeluoikeudetHetulla("090888-929X", user).filter(_.tyyppi.koodiarvo == "korkeakoulutus").length should be >= 1
    }
  }

  "koski-oppilaitos-palvelukäyttäjä" - {
    val user = MockUsers.omniaPalvelukäyttäjä
    "voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      putOpiskeluOikeus(opiskeluOikeusLähdejärjestelmästä, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(200)
      }
    }

    "voi muokata vain lähdejärjestelmällisiä opiskeluoikeuksia" in {
      putOpiskeluOikeus(opiskeluoikeusOmnia, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.lähdejärjestelmäIdPuuttuu("Käyttäjä on palvelukäyttäjä mutta lähdejärjestelmää ei ole määritelty")) // TODO: väärä virhekoodi!
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

    "voi hakea ja katsella ytr-ylioppilastutkintosuorituksia" - {
      "vain omassa organisaatiossaan" in {
        haeOpiskeluoikeudetHetulla("250493-602S", MockUsers.omniaPalvelukäyttäjä).filter(_.tyyppi.koodiarvo == "ylioppilastutkinto").length should equal(0)
        haeOpiskeluoikeudetHetulla("250493-602S", MockUsers.kalle).filter(_.tyyppi.koodiarvo == "ylioppilastutkinto").length should equal(1)
      }
    }

    "voi hakea ja katsella virta-ylioppilastutkintosuorituksia" - {
      "vain omassa organisaatiossaan" in {
        haeOpiskeluoikeudetHetulla("090888-929X", MockUsers.omniaPalvelukäyttäjä).filter(_.tyyppi.koodiarvo == "korkeakoulutus").length should equal(0)
        haeOpiskeluoikeudetHetulla("090888-929X", MockUsers.kalle).filter(_.tyyppi.koodiarvo == "korkeakoulutus").length should be >= 1
      }
    }


  }

  "palvelukäyttäjä, jolla useampi juuriorganisaatio" - {
    "ei voi tallentaa tietoja" in {
      putOpiskeluOikeus(opiskeluOikeusLähdejärjestelmästä, headers = authHeaders(MockUsers.kahdenOrganisaatioPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.juuriorganisaatioPuuttuu("Automaattisen tiedonsiirron palvelukäyttäjällä ei yksiselitteistä juuriorganisaatiota"))
      }
    }
  }

  "koski-oppilaitos-katselija" - {
    val user = MockUsers.omniaKatselija
    "ei voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      putOpiskeluOikeus(opiskeluoikeusOmnia, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
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

  "koski-oppilaitos-tallentaja" - {
    val user = MockUsers.omniaTallentaja
    "voi muokata opiskeluoikeuksia omassa organisaatiossa" in {
      putOpiskeluOikeus(opiskeluoikeusOmnia, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(200)
      }
    }

    "ei voi tallentaa opiskeluoikeuksia käyttäen lähdejärjestelmä-id:tä" in {
      putOpiskeluOikeus(opiskeluOikeusLähdejärjestelmästä, henkilö = OidHenkilö(MockOppijat.markkanen.oid), headers = authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.lähdejärjestelmäIdEiSallittu("Lähdejärjestelmä määritelty, mutta käyttäjä ei ole palvelukäyttäjä"))
      }
    }

    "ei voi muokata lähdejärjestelmän tallentamia opiskeluoikeuksia" - {
      val oppija = MockOppijat.tyhjä
      "ilman opiskeluoikeuden id:tä luodaan uusi opiskeluoikeus" in {
        resetFixtures
        putOpiskeluOikeus(opiskeluOikeusLähdejärjestelmästä, henkilö = OidHenkilö(oppija.oid), headers = authHeaders(MockUsers.omniaPalvelukäyttäjä) ++ jsonContent) {
          verifyResponseStatus(200)
          haeOpiskeluoikeudetHetulla(oppija.hetu, user).filter(_.tyyppi.koodiarvo == "ammatillinenkoulutus").length should equal(1)
          putOpiskeluOikeus(opiskeluoikeusOmnia, henkilö = OidHenkilö(oppija.oid), headers = authHeaders(user) ++ jsonContent) {
            verifyResponseStatus(200)
            haeOpiskeluoikeudetHetulla(oppija.hetu, user).filter(_.tyyppi.koodiarvo == "ammatillinenkoulutus").length should equal(2)
          }
        }
      }
      "opiskeluoikeus-id:tä käytettäessä muutos estetään" in {
        val id = haeOpiskeluoikeudetHetulla(oppija.hetu, user).filter(_.tyyppi.koodiarvo == "ammatillinenkoulutus").filter(_.lähdejärjestelmänId.isDefined)(0).id.get
        putOpiskeluOikeus(opiskeluoikeusOmnia.copy(id = Some(id)), henkilö = OidHenkilö(oppija.oid), headers = authHeaders(user) ++ jsonContent) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden lähdejärjestelmäId:tä ei voi muuttaa."))
        }
      }
    }
  }

  private val opiskeluoikeusOmnia: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
    oppilaitos = Oppilaitos(MockOrganisaatiot.omnia),
    suoritukset = List(tutkintoSuoritus.copy(toimipiste = Oppilaitos(MockOrganisaatiot.omnia)))
  )

  private def opiskeluOikeusLähdejärjestelmästä = opiskeluoikeusOmnia.copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId))

  private def haeOpiskeluoikeudetHetulla(hetu: String, käyttäjä: UserWithPassword) = searchForHenkilötiedot(hetu).map(_.oid).flatMap { oid =>
    opiskeluoikeudet(oid, käyttäjä)
  }

}
