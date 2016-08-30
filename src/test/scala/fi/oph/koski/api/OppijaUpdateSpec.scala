package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData
import fi.oph.koski.documentation.AmmatillinenExampleData.winnovaLähdejärjestelmäId
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.koski.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.koskiuser.MockUsers.{paakayttaja, tiedonsiirtäjä}
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import org.scalatest.FreeSpec

class OppijaUpdateSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  val oppija: TäydellisetHenkilötiedot = MockOppijat.tyhjä

  "Opiskeluoikeuden lisääminen" - {
    "Palauttaa oidin ja versiot" in {
      resetFixtures
      putOppija(Oppija(oppija, List(defaultOpiskeluoikeus))) {
        verifyResponseStatus(200)
        val result = Json.read[HenkilönOpiskeluoikeusVersiot](response.body)
        result.henkilö.oid should equal(oppija.oid)
        result.opiskeluoikeudet.map(_.versionumero) should equal(List(1))
      }
    }
    "Puuttuvien tietojen täyttäminen" - {
      "Oppilaitoksen tiedot" - {
        "Ilman nimeä -> haetaan nimi" in {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, defaultOpiskeluoikeus)
          opiskeluOikeus.oppilaitos.nimi.get.get("fi") should equal("Stadin ammattiopisto")
          opiskeluOikeus.oppilaitos.oppilaitosnumero.get.koodiarvo should equal("10105")
        }
        "Väärällä nimellä -> korvataan nimi" in {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, defaultOpiskeluoikeus.copy(oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto, nimi = Some(LocalizedString.finnish("Läppäkoulu")))))
          opiskeluOikeus.oppilaitos.nimi.get.get("fi") should equal("Stadin ammattiopisto")
        }
      }
      "Koodistojen tiedot" - {
        "Ilman nimeä -> haetaan nimi" in {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, defaultOpiskeluoikeus)
          val suoritus = opiskeluOikeus.suoritukset(0).asInstanceOf[AmmatillisenTutkinnonSuoritus]
          suoritus.koulutusmoduuli.tunniste.nimi.get.get("fi") should equal("Autoalan perustutkinto")
          suoritus.koulutusmoduuli.tunniste.nimi.get.get("sv") should equal("Grundexamen inom bilbranschen")
        }
        "Väärällä nimellä -> korvataan nimi" in {
          val opiskeluOikeus = createOpiskeluOikeus(oppija, defaultOpiskeluoikeus.copy(suoritukset = List(tutkintoSuoritus.copy(koulutusmoduuli = tutkintoSuoritus.koulutusmoduuli.copy(tunniste = Koodistokoodiviite(koodiarvo = "351301", nimi=Some(LocalizedString.finnish("Läppätutkinto")), koodistoUri = "koulutus"))))))

          opiskeluOikeus.suoritukset(0).asInstanceOf[AmmatillisenTutkinnonSuoritus].koulutusmoduuli.tunniste.nimi.get.get("fi") should equal("Autoalan perustutkinto")
        }
      }

      "Koulutustoimijan tiedot" in {
        val opiskeluOikeus = createOpiskeluOikeus(oppija, defaultOpiskeluoikeus)
        opiskeluOikeus.koulutustoimija.map(_.oid) should equal(Some("1.2.246.562.10.346830761110"))
      }
    }
  }

  "Opiskeluoikeuden muokkaaminen" - {
    "Käytettäessä opiskeluoikeus-id:tä" - {
      "Muokkaa olemassaolevaa opiskeluoikeutta" in {
        resetFixtures
        val d: LocalDate = date(2020, 1, 1)
        verifyChange(change = existing => existing.copy(arvioituPäättymispäivä = Some(d))) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluOikeus(oppija.oid)
          result.arvioituPäättymispäivä should equal(Some(d))
          result.versionumero should equal(Some(2))
        }
      }

      "Estää oppilaitoksen vaihtamisen" in {
        verifyChange(change = existing => existing.copy(oppilaitos = Oppilaitos(MockOrganisaatiot.omnia))) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden oppilaitosta ei voi vaihtaa. Vanha oid 1.2.246.562.10.52251087186. Uusi oid 1.2.246.562.10.51720121923."))
        }
      }

      "Estää tyypin vaihtamisen" in {
        verifyChange(change = existing => OpiskeluoikeusTestMethodsLukio.lukionOpiskeluoikeus.copy(id = existing.id, oppilaitos = existing.oppilaitos)) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden tyyppiä ei voi vaihtaa. Vanha tyyppi ammatillinenkoulutus. Uusi tyyppi lukiokoulutus."))
        }
      }
    }

    "Käytettäessä lähdejärjestelmä-id:tä" - {
      val original: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId))

      "Muokkaa olemassaolevaa opiskeluoikeutta, kun lähdejärjestelmä-id on sama" in {
        resetFixtures
        val d: LocalDate = date(2020, 1, 1)
        verifyChange(original = original, user = tiedonsiirtäjä, change = existing => existing.copy(id = None, versionumero = None, arvioituPäättymispäivä = Some(d))) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluOikeus(oppija.oid)
          result.arvioituPäättymispäivä should equal(Some(d))
          result.versionumero should equal(Some(2))
        }
      }

      "Estää oppilaitoksen vaihtamisen" in {
        verifyChange(original = original, user = paakayttaja, change = existing => existing.copy(id = None, versionumero = None, oppilaitos = Oppilaitos(MockOrganisaatiot.omnia))) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden oppilaitosta ei voi vaihtaa. Vanha oid 1.2.246.562.10.52251087186. Uusi oid 1.2.246.562.10.51720121923."))
        }
      }

      "Estää tyypin vaihtamisen" in {
        verifyChange(original = original, user = paakayttaja, change = existing => OpiskeluoikeusTestMethodsLukio.lukionOpiskeluoikeus.copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId), oppilaitos = existing.oppilaitos)) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden tyyppiä ei voi vaihtaa. Vanha tyyppi ammatillinenkoulutus. Uusi tyyppi lukiokoulutus."))
        }
      }

      "Mahdollistaa toisen opiskeluoikeuden luonnin samalla tyypillä ja oppilaitoksella, kunhan lähdejärjestelmä-id on eri" in {
        resetFixtures
        val lähdejärjestelmänId2 = LähdejärjestelmäId(Some("123452"), AmmatillinenExampleData.lähdeWinnova)
        verifyChange(original = original, user = tiedonsiirtäjä, change = existing => existing.copy(id = None, versionumero = None, lähdejärjestelmänId = Some(lähdejärjestelmänId2))) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluOikeus(oppija.oid)
          result.lähdejärjestelmänId.map(_.id) should equal(Some(lähdejärjestelmänId2.id))
          result.versionumero should equal(Some(1))
        }
      }
    }

    "Käytettäessä vain oppilaitoksen tietoa ja opiskeluoikeuden tyyppiä" - {
      "Muokkaa olemassaolevaa opiskeluoikeutta, jos sama oppilaitos ja opiskeluoikeustyyppi (estää siis useamman luonnin)" in {
        resetFixtures
        val d: LocalDate = date(2020, 1, 1)
        verifyChange(change = existing => existing.copy(id = None, arvioituPäättymispäivä = Some(d))) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluOikeus(oppija.oid)
          result.arvioituPäättymispäivä should equal(Some(d))
          result.versionumero should equal(Some(2))
        }
      }

      "Jos olemassa olevassa opiskeluoikeudessa on lähdejärjestelmä-id, ei päivitetä" in {
        resetFixtures
        val lähdejärjestelmänId = LähdejärjestelmäId(Some("12345"), AmmatillinenExampleData.lähdeWinnova)
        verifyChange(original = defaultOpiskeluoikeus.copy(lähdejärjestelmänId = Some(lähdejärjestelmänId)), user = tiedonsiirtäjä, change = existing => existing.copy(id = None, lähdejärjestelmänId = None)) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluOikeus(oppija.oid)
          result.versionumero should equal(Some(1))
        }
      }

      "Jos oppilaitos vaihtuu, tekee uuden opiskeluoikeuden" in {
        resetFixtures
        verifyChange(change = existing => existing.copy(id = None, versionumero = None, oppilaitos = Oppilaitos(MockOrganisaatiot.omnia))) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluOikeus(oppija.oid)
          result.oppilaitos.oid should equal(MockOrganisaatiot.omnia)
          result.versionumero should equal(Some(1))
        }
      }

      "Jos tyyppi vaihtuu, tekee uuden opiskeluoikeuden" in {
        resetFixtures
        verifyChange(change = existing => OpiskeluoikeusTestMethodsLukio.lukionOpiskeluoikeus.copy(oppilaitos = existing.oppilaitos)) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluOikeus(oppija.oid)
          result.tyyppi.koodiarvo should equal(OpiskeluoikeusTestMethodsLukio.lukionOpiskeluoikeus.tyyppi.koodiarvo)
          result.versionumero should equal(Some(1))
        }
      }
    }

    def verifyChange(original: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus, user: UserWithPassword = defaultUser, change: AmmatillinenOpiskeluoikeus => KoskeenTallennettavaOpiskeluoikeus)(block: => Unit) = {
      val headers = authHeaders(user) ++ jsonContent
      putOppija(Oppija(oppija, List(original)), headers) {
        verifyResponseStatus(200)
        val existing = lastOpiskeluOikeus(oppija.oid).asInstanceOf[AmmatillinenOpiskeluoikeus]
        putOppija(Oppija(oppija, List(change(existing))), headers) {
          block
        }
      }
    }
  }
}
