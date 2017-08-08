package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.{AmmatillinenExampleData, ExampleData}
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.Json
import fi.oph.koski.koskiuser.MockUsers.{kalle, paakayttaja, helsinginKaupunkiPalvelukäyttäjä}
import fi.oph.koski.koskiuser.UserWithPassword
import fi.oph.koski.localization.LocalizedString
import fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._
import org.scalatest.FreeSpec

class OppijaUpdateSpec extends FreeSpec with LocalJettyHttpSpecification with OpiskeluoikeusTestMethodsAmmatillinen {
  val oppija = MockOppijat.tyhjä

  "Opiskeluoikeuden lisääminen" - {
    "Palauttaa oidin ja versiot" in {
      resetFixtures
      putOppija(Oppija(oppija, List(defaultOpiskeluoikeus))) {
        verifyResponseStatus(200)
        val result = Json.read[HenkilönOpiskeluoikeusVersiot](response.body)
        result.henkilö.oid should startWith("1.2.246.562.24.000000000")
        result.opiskeluoikeudet.map(_.versionumero) should equal(List(1))
      }
    }
    "Puuttuvien tietojen täyttäminen" - {
      "Oppilaitoksen tiedot" - {
        "Ilman nimeä -> haetaan nimi" in {
          val opiskeluoikeus = createOpiskeluoikeus(oppija, defaultOpiskeluoikeus)
          opiskeluoikeus.getOppilaitos.nimi.get.get("fi") should equal("Stadin ammattiopisto")
          opiskeluoikeus.getOppilaitos.oppilaitosnumero.get.koodiarvo should equal("10105")
        }
        "Väärällä nimellä -> korvataan nimi" in {
          val opiskeluoikeus = createOpiskeluoikeus(oppija, defaultOpiskeluoikeus.copy(oppilaitos = Some(Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto, nimi = Some(LocalizedString.finnish("Läppäkoulu"))))))
          opiskeluoikeus.getOppilaitos.nimi.get.get("fi") should equal("Stadin ammattiopisto")
        }

        "Oppilaitos puuttuu" - {
          "Suoritukselta löytyy toimipiste -> Täytetään oppilaitos" in {
            val opiskeluoikeus = createOpiskeluoikeus(oppija, defaultOpiskeluoikeus.copy(oppilaitos = None))
            opiskeluoikeus.getOppilaitos.oid should equal(MockOrganisaatiot.stadinAmmattiopisto)
          }
          "Suorituksilta löytyy toimipisteitä, joilla sama oppilaitos -> Täytetään oppilaitos" in {
            val opiskeluoikeus = createOpiskeluoikeus(oppija, defaultOpiskeluoikeus.copy(
              oppilaitos = None,
              suoritukset = List(autoalanPerustutkinnonSuoritus(stadinToimipiste), autoalanPerustutkinnonSuoritus(stadinAmmattiopisto))
            ))
            opiskeluoikeus.getOppilaitos.oid should equal(MockOrganisaatiot.stadinAmmattiopisto)
          }
          "Suorituksilta löytyy toimipisteet, joilla eri oppilaitos -> FAIL" in {
            putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
              oppilaitos = None,
              suoritukset = List(autoalanPerustutkinnonSuoritus(stadinToimipiste), autoalanPerustutkinnonSuoritus(OidOrganisaatio(MockOrganisaatiot.omnia)))
            )) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.organisaatio.oppilaitosPuuttuu("Opiskeluoikeudesta puuttuu oppilaitos, eikä sitä voi yksiselitteisesti päätellä annetuista toimipisteistä."))
            }
          }
        }
      }
      "Koodistojen tiedot" - {
        "Ilman nimeä -> haetaan nimi" in {
          val opiskeluoikeus = createOpiskeluoikeus(oppija, defaultOpiskeluoikeus)
          val suoritus = opiskeluoikeus.suoritukset(0).asInstanceOf[AmmatillisenTutkinnonSuoritus]
          suoritus.koulutusmoduuli.tunniste.nimi.get.get("fi") should equal("Autoalan perustutkinto")
          suoritus.koulutusmoduuli.tunniste.nimi.get.get("sv") should equal("Grundexamen inom bilbranschen")
        }
        "Väärällä nimellä -> korvataan nimi" in {
          val opiskeluoikeus = createOpiskeluoikeus(oppija, defaultOpiskeluoikeus.copy(suoritukset = List(autoalanPerustutkinnonSuoritus().copy(koulutusmoduuli = autoalanPerustutkinnonSuoritus().koulutusmoduuli.copy(tunniste = Koodistokoodiviite(koodiarvo = "351301", nimi=Some(LocalizedString.finnish("Läppätutkinto")), koodistoUri = "koulutus"))))))

          opiskeluoikeus.suoritukset(0).asInstanceOf[AmmatillisenTutkinnonSuoritus].koulutusmoduuli.tunniste.nimi.get.get("fi") should equal("Autoalan perustutkinto")
        }
      }

      "Koulutustoimijan tiedot" in {
        val opiskeluoikeus = createOpiskeluoikeus(oppija, defaultOpiskeluoikeus)
        opiskeluoikeus.koulutustoimija.map(_.oid) should equal(Some("1.2.246.562.10.346830761110"))
      }
    }
  }

  "Opiskeluoikeuden muokkaaminen" - {
    "Käytettäessä opiskeluoikeus-oid:ia" - {
      "Muokkaa olemassaolevaa opiskeluoikeutta" in {
        resetFixtures
        val d: LocalDate = date(2020, 1, 1)
        verifyChange(change = {existing: AmmatillinenOpiskeluoikeus => existing.copy(arvioituPäättymispäivä = Some(d))}) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
          result.arvioituPäättymispäivä should equal(Some(d))
          result.versionumero should equal(Some(2))
        }
      }

      "Estää oppilaitoksen vaihtamisen" in {
        verifyChange(change = {existing: AmmatillinenOpiskeluoikeus => existing.copy(koulutustoimija = None, oppilaitos = Some(Oppilaitos(MockOrganisaatiot.omnia)))}) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden oppilaitosta ei voi vaihtaa. Vanha oid 1.2.246.562.10.52251087186. Uusi oid 1.2.246.562.10.51720121923."))
        }
      }

      "Estää tyypin vaihtamisen" in {
        verifyChange(change = {existing: AmmatillinenOpiskeluoikeus => OpiskeluoikeusTestMethodsLukio.lukionOpiskeluoikeus.copy(oid = existing.oid, oppilaitos = existing.oppilaitos)}) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden tyyppiä ei voi vaihtaa. Vanha tyyppi ammatillinenkoulutus. Uusi tyyppi lukiokoulutus."))
        }
      }
    }

    "Käytettäessä lähdejärjestelmä-id:tä" - {
      val original: AmmatillinenOpiskeluoikeus = defaultOpiskeluoikeus.copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId))

      "Muokkaa olemassaolevaa opiskeluoikeutta, kun lähdejärjestelmä-id on sama" in {
        resetFixtures
        val d: LocalDate = date(2020, 1, 1)
        verifyChange(original = original, user = helsinginKaupunkiPalvelukäyttäjä, change = { existing: AmmatillinenOpiskeluoikeus => existing.copy(oid = None, versionumero = None, arvioituPäättymispäivä = Some(d))}) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
          result.arvioituPäättymispäivä should equal(Some(d))
          result.versionumero should equal(Some(2))
        }
      }

      "Estää oppilaitoksen vaihtamisen" in {
        verifyChange(original = original, user = paakayttaja, change = {existing: AmmatillinenOpiskeluoikeus => existing.copy(oid = None, versionumero = None, koulutustoimija = None, oppilaitos = Some(Oppilaitos(MockOrganisaatiot.omnia)))}) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden oppilaitosta ei voi vaihtaa. Vanha oid 1.2.246.562.10.52251087186. Uusi oid 1.2.246.562.10.51720121923."))
        }
      }

      "Estää tyypin vaihtamisen" in {
        verifyChange(original = original, user = paakayttaja, change = {existing: AmmatillinenOpiskeluoikeus => OpiskeluoikeusTestMethodsLukio.lukionOpiskeluoikeus.copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId), oppilaitos = existing.oppilaitos)}) {
          verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyMuutos("Opiskeluoikeuden tyyppiä ei voi vaihtaa. Vanha tyyppi ammatillinenkoulutus. Uusi tyyppi lukiokoulutus."))
        }
      }

      "Mahdollistaa toisen opiskeluoikeuden luonnin samalla tyypillä ja oppilaitoksella, kunhan lähdejärjestelmä-id on eri" in {
        resetFixtures
        val lähdejärjestelmänId2 = LähdejärjestelmäId(Some("123452"), AmmatillinenExampleData.lähdeWinnova)
        verifyChange(original = original, user = helsinginKaupunkiPalvelukäyttäjä, change = { existing: AmmatillinenOpiskeluoikeus => existing.copy(oid = None, versionumero = None, lähdejärjestelmänId = Some(lähdejärjestelmänId2))}) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
          result.lähdejärjestelmänId.map(_.id) should equal(Some(lähdejärjestelmänId2.id))
          result.versionumero should equal(Some(1))
        }
      }
    }

    "Käytettäessä vain oppilaitoksen tietoa ja opiskeluoikeuden tyyppiä" - {
      "Muokkaa olemassaolevaa opiskeluoikeutta, jos sama oppilaitos ja opiskeluoikeustyyppi (estää siis useamman luonnin)" in {
        resetFixtures
        val d: LocalDate = date(2020, 1, 1)
        verifyChange(change = {existing: AmmatillinenOpiskeluoikeus => existing.copy(oid = None, arvioituPäättymispäivä = Some(d))}) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
          result.arvioituPäättymispäivä should equal(Some(d))
          result.versionumero should equal(Some(2))
        }
      }

      "Jos olemassa olevassa opiskeluoikeudessa on lähdejärjestelmä-id, ei päivitetä" in {
        resetFixtures
        val lähdejärjestelmänId = LähdejärjestelmäId(Some("12345"), AmmatillinenExampleData.lähdeWinnova)
        verifyChange(original = defaultOpiskeluoikeus.copy(lähdejärjestelmänId = Some(lähdejärjestelmänId)), user = helsinginKaupunkiPalvelukäyttäjä, user2 = Some(kalle), change = { existing: AmmatillinenOpiskeluoikeus => existing.copy(oid = None, lähdejärjestelmänId = None)}) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
          result.versionumero should equal(Some(1))
        }
      }

      "Jos oppilaitos vaihtuu, tekee uuden opiskeluoikeuden" in {
        resetFixtures
        verifyChange(change = {existing: AmmatillinenOpiskeluoikeus => existing.copy(oid = None, versionumero = None, koulutustoimija = None, oppilaitos = Some(Oppilaitos(MockOrganisaatiot.omnia)))}) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
          result.getOppilaitos.oid should equal(MockOrganisaatiot.omnia)
          result.versionumero should equal(Some(1))
        }
      }

      "Jos tyyppi vaihtuu, tekee uuden opiskeluoikeuden" in {
        resetFixtures
        verifyChange(change = {existing: AmmatillinenOpiskeluoikeus => OpiskeluoikeusTestMethodsLukio.lukionOpiskeluoikeus.copy(oppilaitos = existing.oppilaitos)}) {
          verifyResponseStatus(200)
          val result: KoskeenTallennettavaOpiskeluoikeus = lastOpiskeluoikeusByHetu(oppija)
          result.tyyppi.koodiarvo should equal(OpiskeluoikeusTestMethodsLukio.lukionOpiskeluoikeus.tyyppi.koodiarvo)
          result.versionumero should equal(Some(1))
        }
      }
    }

    "Jos valmis päätason suoritus on poistunut" - {
      "Aiemmin tallennettu suoritus säilytetään" in {
        resetFixtures
        val vanhaValmisSuoritus = valmis(ammatillinenTutkintoSuoritus(autoalanPerustutkinto))
        val vanhaKeskeneräinenSuoritus = ammatillinenTutkintoSuoritus(puutarhuri)
        val uusiSuoritus = ammatillinenTutkintoSuoritus(parturikampaaja)
        val oo = defaultOpiskeluoikeus.copy(suoritukset = List(vanhaValmisSuoritus, vanhaKeskeneräinenSuoritus))
        def poistaSuoritukset(oo: AmmatillinenOpiskeluoikeus) = oo.copy(suoritukset = List(uusiSuoritus))
        verifyChange(original = oo, change = poistaSuoritukset) {
          verifyResponseStatus(200)
          val result: AmmatillinenOpiskeluoikeus = lastOpiskeluoikeusByHetu(oppija).asInstanceOf[AmmatillinenOpiskeluoikeus]
          result.suoritukset.map(_.koulutusmoduuli.tunniste.koodiarvo) should equal(List(vanhaValmisSuoritus.koulutusmoduuli.tunniste.koodiarvo, uusiSuoritus.koulutusmoduuli.tunniste.koodiarvo))
        }
      }
    }

    def valmis(suoritus: AmmatillisenTutkinnonSuoritus) = suoritus.copy(
      tila = tilaValmis,
      vahvistus = ExampleData.vahvistusPaikkakunnalla(päivä = date(2016, 10, 1))
    )

    def verifyChange[T <: Opiskeluoikeus](original: T = defaultOpiskeluoikeus, user: UserWithPassword = defaultUser, user2: Option[UserWithPassword] = None, change: T => KoskeenTallennettavaOpiskeluoikeus)(block: => Unit) = {
      putOppija(Oppija(oppija, List(original)), authHeaders(user) ++ jsonContent) {
        verifyResponseStatus(200)
        val existing = lastOpiskeluoikeusByHetu(oppija).asInstanceOf[T]
        val updated: KoskeenTallennettavaOpiskeluoikeus = change(existing)
        putOppija(Oppija(oppija, List(updated)), authHeaders(user2.getOrElse(user)) ++ jsonContent) {
          block
        }
      }
    }
  }
}
